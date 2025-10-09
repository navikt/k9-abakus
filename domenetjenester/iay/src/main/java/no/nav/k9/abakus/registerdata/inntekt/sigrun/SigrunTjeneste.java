package no.nav.k9.abakus.registerdata.inntekt.sigrun;

import static java.time.temporal.ChronoUnit.YEARS;

import java.math.BigDecimal;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.felles.samtidighet.SystemuserThreadLogin;
import no.nav.k9.abakus.felles.samtidighet.UncheckedInterruptException;
import no.nav.k9.abakus.registerdata.inntekt.sigrun.klient.PgiFolketrygdenResponse;
import no.nav.k9.abakus.registerdata.inntekt.sigrun.klient.SigrunPgiFolketrygdenMapper;
import no.nav.k9.abakus.registerdata.inntekt.sigrun.klient.SigrunRestClient;
import no.nav.k9.abakus.typer.PersonIdent;


@ApplicationScoped
public class SigrunTjeneste {
    private static final MonthDay TIDLIGSTE_SJEKK_FJOR = MonthDay.of(Month.MAY, 1);

    private static final Year FØRSTE_PGI = Year.of(2017);
    private SigrunRestClient sigrunConsumer;
    private SystemuserThreadLogin systemuserThreadLogin;

    SigrunTjeneste() {
        //CDI
    }

    @Inject
    public SigrunTjeneste(SigrunRestClient sigrunConsumer, SystemuserThreadLogin systemuserThreadLogin) {
        this.sigrunConsumer = sigrunConsumer;
        this.systemuserThreadLogin = systemuserThreadLogin;
    }


    public Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> hentPensjonsgivende(PersonIdent fnr,
                                                                                        IntervallEntitet opplysningsperiodeSkattegrunnlag) {
        var svarene = pensjonsgivendeInntektForFolketrygden(fnr.getIdent(), opplysningsperiodeSkattegrunnlag);
        return SigrunPgiFolketrygdenMapper.mapFraPgiResponseTilIntern(svarene)
            .entrySet()
            .stream()
            .filter(e -> !e.getValue().isEmpty())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<PgiFolketrygdenResponse> pensjonsgivendeInntektForFolketrygden(String fnr, IntervallEntitet opplysningsperiode) {
        var senesteÅr = utledSenesteÅr(opplysningsperiode);
        List<PgiFolketrygdenResponse> svarene = Collections.synchronizedList(new ArrayList<>());

        var svarSenesteÅr = svarForSenesteÅr(fnr, senesteÅr);
        svarSenesteÅr.ifPresent(it -> svarene.add(svarSenesteÅr.get()));

        try (var scope = StructuredTaskScope.open() ) {
            utledTidligereÅr(opplysningsperiode, senesteÅr, svarSenesteÅr.isPresent())
                .forEach(år -> systemuserThreadLogin.fork(scope,
                    () -> sigrunConsumer.hentPensjonsgivendeInntektForFolketrygden(fnr, år).ifPresent(svarene::add)));
            try {
                scope.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UncheckedInterruptException("En tråd ble interrupted mens den hentet PGI fra Sigrun",e);
            }
        }
        svarene.sort(Comparator.comparing(PgiFolketrygdenResponse::inntektsaar).reversed());
        return svarene;
    }

    private Year utledSenesteÅr(IntervallEntitet opplysningsperiode) {
        var ifjor = Year.now().minusYears(1);
        var oppgitt = Year.from(opplysningsperiode.getTomDato());
        return oppgitt.isAfter(ifjor) ? ifjor : oppgitt;
    }

    public Optional<PgiFolketrygdenResponse> svarForSenesteÅr(String fnr, Year senesteÅr) {
        if (Year.now().minusYears(1).equals(senesteÅr) && MonthDay.now().isBefore(TIDLIGSTE_SJEKK_FJOR)) {
            return Optional.empty();
        }
        try {
            return sigrunConsumer.hentPensjonsgivendeInntektForFolketrygden(fnr, senesteÅr);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private List<Year> utledTidligereÅr(IntervallEntitet opplysningsperiode, Year senesteÅr, boolean harDataSenesteÅr) {
        long periodeLengde = YEARS.between(opplysningsperiode.getFomDato(), opplysningsperiode.getTomDato());
        var tidligsteÅr = Year.from(opplysningsperiode.getFomDato());
        var fraTidligsteÅr = harDataSenesteÅr || periodeLengde > 2L ? tidligsteÅr : tidligsteÅr.minusYears(1);
        if (fraTidligsteÅr.isBefore(FØRSTE_PGI)) {
            fraTidligsteÅr = FØRSTE_PGI;
        }
        List<Year> årene = new ArrayList<>();
        while (fraTidligsteÅr.isBefore(senesteÅr)) {
            årene.add(fraTidligsteÅr);
            fraTidligsteÅr = fraTidligsteÅr.plusYears(1);
        }
        return årene.stream().sorted(Comparator.reverseOrder()).toList();
    }

}
