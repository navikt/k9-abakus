package no.nav.k9.abakus.registerdata.inntekt.komponenten;

import static no.nav.k9.abakus.registerdata.inntekt.komponenten.UtledFormål.utledFormålFraYtelse;

import java.net.URI;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.felles.exception.IntegrasjonException;
import no.nav.k9.felles.exception.TekniskException;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektIdent;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektMaaned;
import no.nav.tjenester.aordningen.inntektsinformasjon.Tilleggsinformasjon;
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.Inntekt;
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.InntektType;
import no.nav.tjenester.aordningen.inntektsinformasjon.request.HentInntektListeBolkRequest;
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeBolkResponse;
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.Etterbetalingsperiode;
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.TilleggsinformasjonDetaljerType;

@ApplicationScoped
@ScopedRestIntegration(scopeKey = "hentinntektlistebolk.scope", defaultScope = "api://prod-fss.team-inntekt.ikomp/.default")
public class InntektTjeneste {

    // Dato for eldste request til inntk - det er av og til noen ES saker som spør lenger tilbake i tid
    private static final YearMonth INNTK_TIDLIGSTE_DATO = YearMonth.of(2015, 7);
    private static final Set<InntektskildeType> SKAL_PERIODISERE_INNTEKTSKILDE = Set.of(InntektskildeType.INNTEKT_SAMMENLIGNING,
        InntektskildeType.INNTEKT_BEREGNING);

    private static final Logger LOG = LoggerFactory.getLogger(InntektTjeneste.class);

    private Map<InntektskildeType, InntektsFilter> kildeTilFilter;


    private SystemUserOidcRestClient oidcRestClient;
    private String url;


    InntektTjeneste() {
        // For CDI proxy
    }

    @Inject
    public InntektTjeneste(SystemUserOidcRestClient oidcRestClient,
                           @KonfigVerdi(value = "hentinntektlistebolk.url", defaultVerdi = "http://ikomp.team-inntekt/rs/api/v1/hentinntektlistebolk") String url) {
        this.oidcRestClient = oidcRestClient;
        this.url = url;
        this.kildeTilFilter = Map.of(InntektskildeType.INNTEKT_OPPTJENING, InntektsFilter.OPPTJENINGSGRUNNLAG, InntektskildeType.INNTEKT_BEREGNING,
            InntektsFilter.BEREGNINGSGRUNNLAG, InntektskildeType.INNTEKT_SAMMENLIGNING, InntektsFilter.SAMMENLIGNINGSGRUNNLAG,
            InntektskildeType.INNTEKT_UNGDOMSYTELSE, InntektsFilter.UNGDOMSYTELSEGRUNNLAG);
    }

    public InntektsInformasjon finnInntekt(FinnInntektRequest finnInntektRequest, InntektskildeType kilde, YtelseType ytelse) {
        HentInntektListeBolkResponse response = finnInntektRaw(finnInntektRequest, kilde, ytelse);
        return oversettResponse(response, kilde, ytelse);

    }

    public HentInntektListeBolkResponse finnInntektRaw(FinnInntektRequest finnInntektRequest, InntektskildeType kilde, YtelseType ytelse) {
        var request = lagRequest(finnInntektRequest, kilde, ytelse);

        HentInntektListeBolkResponse response;
        try {
            response = oidcRestClient.post(URI.create(url), request, HentInntektListeBolkResponse.class);
        } catch (RuntimeException e) {
            throw new IntegrasjonException("FP-824246",
                "Feil ved kall til inntektstjenesten. Meld til #team_registre og #produksjonshendelser hvis dette skjer over lengre tidsperiode.", e);
        }
        return response;
    }

    private HentInntektListeBolkRequest lagRequest(FinnInntektRequest finnInntektRequest, InntektskildeType kilde, YtelseType ytelse) {
        var request = new HentInntektListeBolkRequest();

        if (finnInntektRequest.getFnr() != null) {
            request.setIdentListe(Collections.singletonList(Aktoer.newNaturligIdent(finnInntektRequest.getFnr())));
        } else {
            request.setIdentListe(Collections.singletonList(Aktoer.newAktoerId(finnInntektRequest.getAktørId())));
        }

        InntektsFilter filter = getFilter(kilde);
        if (filter != null) {
            request.setAinntektsfilter(filter.getKode());
            request.setFormaal(filter.getFormål().orElse(utledFormålFraYtelse(ytelse)).getKode());
        }
        request.setMaanedFom(finnInntektRequest.getFom().isAfter(INNTK_TIDLIGSTE_DATO) ? finnInntektRequest.getFom() : INNTK_TIDLIGSTE_DATO);
        request.setMaanedTom(finnInntektRequest.getTom().isAfter(INNTK_TIDLIGSTE_DATO) ? finnInntektRequest.getTom() : INNTK_TIDLIGSTE_DATO);
        return request;
    }


    private InntektsFilter getFilter(InntektskildeType kilde) {
        // Skal bare få en verdi.
        return kildeTilFilter.getOrDefault(kilde, null);
    }

    private InntektsInformasjon oversettResponse(HentInntektListeBolkResponse response, InntektskildeType kilde, YtelseType ytelse) {
        if (response.getSikkerhetsavvikListe() != null && !response.getSikkerhetsavvikListe().isEmpty()) {
            throw new IntegrasjonException("FP-535194",
                String.format("Fikk følgende sikkerhetsavvik ved kall til inntektstjenesten: %s.", byggSikkerhetsavvikString(response)));
        }

        List<Månedsinntekt> månedsinntekter = new ArrayList<>();

        List<ArbeidsInntektIdent> arbeidsInntektIdentListe = response.getArbeidsInntektIdentListe();
        if (response.getArbeidsInntektIdentListe() != null) {
            for (var arbeidsInntektIdent : arbeidsInntektIdentListe) {
                if (arbeidsInntektIdent.getArbeidsInntektMaaned() != null) {
                    for (ArbeidsInntektMaaned arbeidsInntektMaaned : arbeidsInntektIdent.getArbeidsInntektMaaned()) {
                        oversettInntekter(månedsinntekter, arbeidsInntektMaaned, kilde);
                    }
                }
            }
        }
        return new InntektsInformasjon(månedsinntekter, kilde);
    }

    private void oversettInntekter(List<Månedsinntekt> månedsinntekter, ArbeidsInntektMaaned arbeidsInntektMaaned, InntektskildeType kilde) {
        var arbeidsInntektInformasjon = arbeidsInntektMaaned.getArbeidsInntektInformasjon();

        if (arbeidsInntektInformasjon != null && arbeidsInntektInformasjon.getInntektListe() != null) {
            for (var inntekt : arbeidsInntektInformasjon.getInntektListe()) {
                var brukYM = inntekt.getUtbetaltIMaaned();
                var tilleggsinformasjon = inntekt.getTilleggsinformasjon();
                if (erYtelseFraOffentlig(inntekt) && erEtterbetaling(tilleggsinformasjon) && skalPeriodisereInntektsKilde(kilde)) {
                    brukYM = YearMonth.from(
                        ((Etterbetalingsperiode) tilleggsinformasjon.getTilleggsinformasjonDetaljer()).getEtterbetalingsperiodeFom().plusDays(1));
                }
                var månedsinntekt = new Månedsinntekt.Builder().medBeløp(inntekt.getBeloep())
                    .medSkatteOgAvgiftsregelType(inntekt.getSkatteOgAvgiftsregel());

                if (brukYM != null) {
                    månedsinntekt.medMåned(brukYM);
                }
                utledOgSettUtbetalerOgYtelse(inntekt, månedsinntekt);

                månedsinntekter.add(månedsinntekt.build());
            }
        }
    }

    private boolean skalPeriodisereInntektsKilde(InntektskildeType kilde) {
        return SKAL_PERIODISERE_INNTEKTSKILDE.contains(kilde);
    }

    private boolean erEtterbetaling(Tilleggsinformasjon tilleggsinformasjon) {
        return tilleggsinformasjon != null && TilleggsinformasjonDetaljerType.ETTERBETALINGSPERIODE.equals(
            tilleggsinformasjon.getTilleggsinformasjonDetaljer().getDetaljerType());
    }

    private void utledOgSettUtbetalerOgYtelse(Inntekt inntekt, Månedsinntekt.Builder månedsinntekt) {
        if (erYtelseFraOffentlig(inntekt)) {
            månedsinntekt.medYtelse(true).medYtelseKode(inntekt.getBeskrivelse());
        } else if (erPensjonEllerTrygd(inntekt)) {
            månedsinntekt.medYtelse(true).medPensjonEllerTrygdKode(inntekt.getBeskrivelse());
        } else if (erNæringsinntekt(inntekt)) {
            månedsinntekt.medYtelse(true).medNæringsinntektKode(inntekt.getBeskrivelse());
        } else if (erLønn(inntekt)) {
            månedsinntekt.medYtelse(false);
            månedsinntekt.medArbeidsgiver(inntekt.getVirksomhet().getIdentifikator()); // OK med NPE hvis inntekt.getArbeidsgiver() er null
            månedsinntekt.medArbeidsforholdRef(inntekt.getArbeidsforholdREF());
            månedsinntekt.medLønnsbeskrivelseKode(inntekt.getBeskrivelse());
        } else {
            throw new TekniskException("FP-711674", String.format("Kunne ikke mappe svar fra Inntektskomponenten: virksomhet=%s, inntektType=%s",
                inntekt.getVirksomhet().getIdentifikator(), inntekt.getInntektType()));
        }
    }

    private boolean erLønn(Inntekt inntekt) {
        return InntektType.LOENNSINNTEKT.equals(inntekt.getInntektType());
    }

    private boolean erYtelseFraOffentlig(Inntekt inntekt) {
        return InntektType.YTELSE_FRA_OFFENTLIGE.equals(inntekt.getInntektType());
    }

    private boolean erPensjonEllerTrygd(Inntekt inntekt) {
        return InntektType.PENSJON_ELLER_TRYGD.equals(inntekt.getInntektType());
    }

    private boolean erNæringsinntekt(Inntekt inntekt) {
        return InntektType.NAERINGSINNTEKT.equals(inntekt.getInntektType());
    }

    private String byggSikkerhetsavvikString(HentInntektListeBolkResponse response) {
        var stringBuilder = new StringBuilder();
        var sikkerhetsavvikListe = response.getSikkerhetsavvikListe();
        if (sikkerhetsavvikListe != null && !sikkerhetsavvikListe.isEmpty()) {
            stringBuilder.append(sikkerhetsavvikListe.get(0).getTekst());
            for (int i = 1; i < sikkerhetsavvikListe.size(); i++) {
                stringBuilder.append(", ");
                stringBuilder.append(sikkerhetsavvikListe.get(i).getTekst());
            }
        }
        return stringBuilder.toString();
    }
}
