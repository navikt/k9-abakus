package no.nav.k9.abakus.registerdata.inntekt.komponenten;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.domene.iay.InntektFilter;
import no.nav.k9.abakus.felles.samtidighet.UncheckedInterruptException;
import no.nav.k9.felles.exception.IntegrasjonException;
import no.nav.k9.felles.exception.TekniskException;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektIdent;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektMaaned;
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.InntektType;
import no.nav.tjenester.aordningen.inntektsinformasjon.request.HentInntektListeBolkRequest;
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeBolkResponse;
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.Etterbetalingsperiode;
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.TilleggsinformasjonDetaljerType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static no.nav.k9.abakus.registerdata.inntekt.komponenten.UtledFormål.utledFormålFraYtelse;

@ApplicationScoped
@ScopedRestIntegration(scopeKey = "inntektskomponenten.scope", defaultScope = "api://prod-fss.team-inntekt.ikomp/.default")
public class InntektV2Tjeneste {

    // Dato for eldste request til inntk - det er av og til noen ES saker som spør lenger tilbake i tid
    private static final YearMonth INNTK_TIDLIGSTE_DATO = YearMonth.of(2015, 7);
    private static final Set<InntektskildeType> SKAL_PERIODISERE_INNTEKTSKILDE = Set.of(InntektskildeType.INNTEKT_SAMMENLIGNING,
        InntektskildeType.INNTEKT_BEREGNING);

    private static final Logger LOG = LoggerFactory.getLogger(InntektV2Tjeneste.class);

    private Map<InntektskildeType, InntektsFilter> kildeTilFilter;
    private Map<InntektsFilter, InntektskildeType> filterTilKilde;


    private SystemUserOidcRestClient oidcRestClient;
    private String url;

    //bruker semafor for å begrense antall samtidige kall til inntektskomponenten
    //regner ikke med å ha så mange i praksis pga bare 3 tråder brukes for å kjøre tasker, og hver av disse kaller inntil en håndfull ganger
    private Semaphore semaphore = new Semaphore(15);


    InntektV2Tjeneste() {
        // For CDI proxy
    }

    @Inject
    public InntektV2Tjeneste(SystemUserOidcRestClient oidcRestClient,
                             @KonfigVerdi(value = "inntektskomponenten.hentinntektbulk.url", defaultVerdi = "http://ikomp.team-inntekt/rest/v2/inntekt/bulk") String url) {
        this.oidcRestClient = oidcRestClient;
        this.url = url;
        this.kildeTilFilter = Map.of(
            InntektskildeType.INNTEKT_OPPTJENING, InntektsFilter.OPPTJENINGSGRUNNLAG,
            InntektskildeType.INNTEKT_BEREGNING, InntektsFilter.BEREGNINGSGRUNNLAG,
            InntektskildeType.INNTEKT_SAMMENLIGNING, InntektsFilter.SAMMENLIGNINGSGRUNNLAG,
            InntektskildeType.INNTEKT_UNGDOMSYTELSE, InntektsFilter.UNGDOMSYTELSEGRUNNLAG);
        this.filterTilKilde = kildeTilFilter.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public Map<InntektskildeType, InntektsInformasjon> finnInntekt(FinnInntektRequest finnInntektRequest, Set<InntektskildeType> kilder,
                                                                   YtelseType ytelse) {
        var request = lagRequest(finnInntektRequest, kilder, ytelse);
        InntektBulkApiUt response = finnInntektRaw(request);
        return oversettResponse(response, kilder, ytelse);
    }

    public InntektBulkApiUt finnInntektRaw(InntektBulkApiInn request) {
        try {
            boolean ledigPlass = semaphore.tryAcquire(30, TimeUnit.SECONDS);
            if (!ledigPlass) {
                throw new IllegalStateException("Fikk timeout på venting av semafor for kall til inntektskomponenten. Underøk om tjenesten er nede, vurder å øke antall tillatelser i semaforen.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedInterruptException("En tråd ble interrupted mens den ventet på å få semafor",e);
        }
        try {
            return oidcRestClient.post(URI.create(url), request, InntektBulkApiUt.class);
        } catch (RuntimeException e) {
            throw new IntegrasjonException("FP-824246", "Feil ved kall til inntektstjenesten. Meld til #team_registre og #produksjonshendelser hvis dette skjer over lengre tidsperiode.", e);
        } finally {
            semaphore.release();
        }
    }

    private InntektBulkApiInn lagRequest(FinnInntektRequest finnInntektRequest, Set<InntektskildeType> kilder, YtelseType ytelse) {

        var ident = Optional.ofNullable(finnInntektRequest.getAktørId()).orElseGet(finnInntektRequest::getFnr);
        var filtre = kilder.stream().map(kildeTilFilter::get).filter(Objects::nonNull).map(InntektsFilter::getKode).toList();

        return new InntektBulkApiInn(ident, filtre, utledFormålFraYtelse(ytelse).getKode(),
            brukDato(finnInntektRequest.getFom()), brukDato(finnInntektRequest.getTom()));
    }

    private static YearMonth brukDato(YearMonth dato) {
        return dato != null && dato.isAfter(INNTK_TIDLIGSTE_DATO) ? dato : INNTK_TIDLIGSTE_DATO;
    }


    private Map<InntektskildeType, InntektsInformasjon> oversettResponse(InntektBulkApiUt response, Set<InntektskildeType> kilder, YtelseType ytelse) {

        List<InntektBulk>  svar = Optional.ofNullable(response).map(InntektBulkApiUt::bulk).orElseGet(List::of);
        if (svar.isEmpty()) {
            return Map.of();
        }

        var oversatt = new ArrayList<InntektsInformasjon>();
        for (var i : svar) {
            var kilde = filterTilKilde.getOrDefault(getFilter(i), InntektskildeType.UDEFINERT);
            if (!kilder.contains(kilde)) continue;
            var data = Optional.ofNullable(i.data()).orElseGet(List::of);
            List<Månedsinntekt> månedsinntekter = new ArrayList<>();
            for (var d : data) {
                månedsinntekter.addAll(oversettInntekter(d, kilde));
            }
            oversatt.add(new InntektsInformasjon(månedsinntekter, kilde));
        }

        return oversatt.stream().collect(Collectors.toMap(InntektsInformasjon::getKilde, i -> i));
    }

    private List<Månedsinntekt> oversettInntekter(Inntektsinformasjon inntektsinformasjon, InntektskildeType kilde) {
        List<Månedsinntekt> månedsinntekter = new ArrayList<>();
        var inntekter = Optional.ofNullable(inntektsinformasjon.inntektListe()).orElseGet(List::of);
        for (var inntekt : inntekter) {
            var brukYM = inntektsinformasjon.maaned();
            var tilleggsinformasjon = inntekt.tilleggsinformasjon();
            if (tilleggsinformasjon != null
                && "YtelseFraOffentlige".equals(inntekt.type())
                && "Etterbetalingsperiode".equals(tilleggsinformasjon.type())
                && tilleggsinformasjon.startdato() != null
                && skalPeriodisereInntektsKilde(kilde)) {
                brukYM = YearMonth.from(tilleggsinformasjon.startdato().plusDays(1));
            }
            var månedsinntekt = new Månedsinntekt.Builder()
                .medBeløp(inntekt.beloep())
                .medSkatteOgAvgiftsregelType(inntekt.skatteOgAvgiftsregel());

            if (brukYM != null) {
                månedsinntekt.medMåned(brukYM);
            }
            utledOgSettUtbetalerOgYtelse(inntektsinformasjon, inntekt, månedsinntekt);

            månedsinntekter.add(månedsinntekt.build());

        }
        return månedsinntekter;
    }

    private boolean skalPeriodisereInntektsKilde(InntektskildeType kilde) {
        return SKAL_PERIODISERE_INNTEKTSKILDE.contains(kilde);
    }

    private void utledOgSettUtbetalerOgYtelse(Inntektsinformasjon inntektsinformasjon, Inntekt inntekt, Månedsinntekt.Builder månedsinntekt) {
        switch (inntekt.type()) {
            case "YtelseFraOffentlige" -> månedsinntekt.medYtelse(true).medYtelseKode(inntekt.beskrivelse());
            case "PensjonEllerTrygd" -> månedsinntekt.medYtelse(true).medPensjonEllerTrygdKode(inntekt.beskrivelse());
            case "Naeringsinntekt" -> månedsinntekt.medYtelse(true).medNæringsinntektKode(inntekt.beskrivelse());
            case "Loennsinntekt" -> {
                månedsinntekt.medYtelse(false);
                månedsinntekt.medArbeidsgiver(inntektsinformasjon.underenhet());
                månedsinntekt.medLønnsbeskrivelseKode(inntekt.beskrivelse());
            }
            case null, default -> throw new TekniskException("FP-711674", String.format("Kunne ikke mappe svar fra Inntektskomponenten: virksomhet=%s, inntektType=%s",
                inntektsinformasjon.underenhet(), inntekt.type()));
        }
    }

    private static InntektsFilter getFilter(InntektBulk bulk) {
        return Arrays.stream(InntektsFilter.values())
            .filter(f -> f.getKode().equals(bulk.filter()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(String.format("Ugyldig filter i ikomp-respons %s ", bulk.filter())));
    }

    public record InntektBulkApiInn(String personident, List<String> filter, String formaal,
                                    YearMonth maanedFom, YearMonth maanedTom) { }

    public record InntektBulkApiUt(List<InntektBulk> bulk) { }

    public record InntektBulk(String filter, List<Inntektsinformasjon> data) { }

    public record Inntektsinformasjon(YearMonth maaned, String opplysningspliktig, String underenhet, List<Inntekt> inntektListe) { }

    public record Inntekt(String type, BigDecimal beloep, String beskrivelse, String skatteOgAvgiftsregel, Tilleggsinformasjon tilleggsinformasjon) { }

    public record Tilleggsinformasjon(String type, LocalDate startdato, LocalDate sluttdato) { }
}
