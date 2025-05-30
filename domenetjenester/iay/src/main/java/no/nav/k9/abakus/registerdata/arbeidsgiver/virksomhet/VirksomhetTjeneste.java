package no.nav.k9.abakus.registerdata.arbeidsgiver.virksomhet;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.OrganisasjonType;
import no.nav.k9.abakus.domene.virksomhet.Virksomhet;
import no.nav.k9.abakus.typer.OrgNummer;
import no.nav.k9.felles.exception.TekniskException;
import no.nav.k9.felles.integrasjon.organisasjon.OrganisasjonEReg;
import no.nav.k9.felles.integrasjon.organisasjon.OrganisasjonRestKlient;
import no.nav.k9.felles.integrasjon.organisasjon.OrganisasjonstypeEReg;
import no.nav.k9.felles.integrasjon.organisasjon.UtvidetOrganisasjonEReg;
import no.nav.k9.felles.util.LRUCache;


@ApplicationScoped
public class VirksomhetTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(VirksomhetTjeneste.class);
    private static final long CACHE_ELEMENT_LIVE_TIME_MS = TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES);


    private final Virksomhet KUNSTIG_VIRKSOMHET = new Virksomhet.Builder().medNavn("Kunstig virksomhet")
        .medOrganisasjonstype(OrganisasjonType.KUNSTIG)
        .medOrgnr(OrgNummer.KUNSTIG_ORG)
        .medRegistrert(LocalDate.of(1978, 01, 01))
        .medOppstart(LocalDate.of(1978, 01, 01))
        .build();

    private LRUCache<String, OrganisasjonEReg> cacheEREG = new LRUCache<>(1000, CACHE_ELEMENT_LIVE_TIME_MS);
    private LRUCache<String, UtvidetOrganisasjonEReg> cacheJuridiskEREG = new LRUCache<>(100, CACHE_ELEMENT_LIVE_TIME_MS);

    private OrganisasjonRestKlient eregRestKlient;

    public VirksomhetTjeneste() {
        // CDI
    }

    @Inject
    public VirksomhetTjeneste(OrganisasjonRestKlient eregRestKlient) {
        this.eregRestKlient = eregRestKlient;
    }

    /**
     * Henter informasjon fra Enhetsregisteret hvis applikasjonen ikke kjenner til orgnr
     *
     * @param orgNummer orgnummeret
     * @return relevant informasjon om virksomheten.
     */
    public Virksomhet hentOrganisasjon(String orgNummer) {
        if (Objects.equals(KUNSTIG_VIRKSOMHET.getOrgnr(), orgNummer)) {
            return KUNSTIG_VIRKSOMHET;
        }
        return mapVirksomhet(hentResponseOrganisasjon(orgNummer));
    }

    /**
     * Henter informasjon fra Enhetsregisteret
     *
     * @param orgNummer orgnummeret
     * @return true (når virksomheten er orgledd)
     */
    public boolean sjekkOmOrganisasjonErOrgledd(String orgNummer) {
        return OrganisasjonstypeEReg.ORGLEDD.equals(hentResponseOrganisasjon(orgNummer).getType());
    }

    /**
     * Henter informasjon fra Enhetsregisteret hvis applikasjonen ikke kjenner til orgnr eller har data som er eldre enn 24 timer.
     * Prøver og utlede faktisk virksomhet hvis oppgitt orgNummer refererer til et juridiskOrgnummer (gjelder 1 til 1 forhold)
     * Vil lagre ned virksomheten med juridiskOrgNummer hvis det ikke er mulig og utlede faktisk orgNummer (gjelder 1 til mange forhold)
     *
     * @param orgNummer orgnummeret
     * @return relevant informasjon om virksomheten.
     */
    public Virksomhet hentOrganisasjonMedHensynTilJuridisk(String orgNummer, LocalDate hentedato) {
        final Virksomhet virksomhet = hentOrganisasjon(orgNummer);

        if (OrganisasjonType.JURIDISK_ENHET.equals(virksomhet.getOrganisasjonstype())) {
            Optional<Virksomhet> unikVirksomhetForJuridiskEnhet = hentUnikVirksomhetForJuridiskEnhet(orgNummer, hentedato);
            LOG.info("ABAKUS EREG fant {} unik virksomhet for juridisk {}", unikVirksomhetForJuridiskEnhet.isPresent() ? "en" : "ikke",
                getIdentifikatorString(orgNummer));
            return unikVirksomhetForJuridiskEnhet.orElse(virksomhet);
        }
        if (OrganisasjonType.ORGLEDD.equals(virksomhet.getOrganisasjonstype())) {
            throw new TekniskException("FP-36379", "Organisasjon er Orgledd");
        }
        return virksomhet;
    }

    private String getIdentifikatorString(String arbeidsgiverIdentifikator) {
        if (arbeidsgiverIdentifikator == null) {
            return null;
        }
        int length = arbeidsgiverIdentifikator.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 4) + arbeidsgiverIdentifikator.substring(length - 4);
    }

    private Optional<Virksomhet> hentUnikVirksomhetForJuridiskEnhet(String orgNummer, LocalDate hentedato) {
        var jenhet = hentResponseJuridisk(orgNummer);

        List<OrganisasjonEReg> aktiveOrgnr = jenhet.getEksaktVirksomhetForDato(hentedato)
            .stream()
            .map(this::hentResponseOrganisasjon)
            .filter(o -> o.getOpphørsdato() == null || hentedato.isBefore(o.getOpphørsdato()))
            .collect(Collectors.toList());
        return aktiveOrgnr.size() == 1 ? Optional.of(mapVirksomhet(aktiveOrgnr.get(0))) : Optional.empty();
    }

    private Virksomhet mapVirksomhet(OrganisasjonEReg org) {
        var builder = new Virksomhet.Builder().medNavn(org.getNavn()).medRegistrert(org.getRegistreringsdato()).medOrgnr(org.getOrganisasjonsnummer());
        if (OrganisasjonstypeEReg.VIRKSOMHET.equals(org.getType())) {
            builder.medOrganisasjonstype(OrganisasjonType.VIRKSOMHET).medOppstart(org.getOppstartsdato()).medAvsluttet(org.getNedleggelsesdato());
        } else if (OrganisasjonstypeEReg.JURIDISK_ENHET.equals(org.getType())) {
            builder.medOrganisasjonstype(OrganisasjonType.JURIDISK_ENHET);
        } else if (OrganisasjonstypeEReg.ORGLEDD.equals(org.getType())) {
            builder.medOrganisasjonstype(OrganisasjonType.ORGLEDD);
        }
        return builder.build();
    }

    private OrganisasjonEReg hentResponseOrganisasjon(String orgnr) {
        var response = Optional.ofNullable(cacheEREG.get(orgnr)).orElseGet(() -> eregRestKlient.hentOrganisasjon(orgnr));
        cacheEREG.put(orgnr, response);
        return response;
    }

    private UtvidetOrganisasjonEReg hentResponseJuridisk(String orgnr) {
        var response = Optional.ofNullable(cacheJuridiskEREG.get(orgnr)).orElseGet(() -> eregRestKlient.hentUtvidetOrganisasjon(orgnr));
        cacheJuridiskEREG.put(orgnr, response);
        return response;
    }

}
