package no.nav.k9.abakus.registerdata;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;

import no.nav.k9.abakus.registerdata.inntekt.SystemuserThreadLogin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.k9.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.k9.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.FinnInntektRequest;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.Månedsinntekt;
import no.nav.k9.abakus.registerdata.ytelse.arena.FpwsproxyKlient;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.felles.konfigurasjon.env.Environment;

@ApplicationScoped
public class InnhentingSamletTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(InnhentingSamletTjeneste.class);
    private final boolean isDev = Environment.current().isDev();

    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private InntektTjeneste inntektTjeneste;
    private FpwsproxyKlient fpwsproxyKlient;
    private InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste;
    private SystemuserThreadLogin systemuserThreadLogin;

    InnhentingSamletTjeneste(SystemuserThreadLogin systemuserThreadLogin) {
        //CDI
        this.systemuserThreadLogin = systemuserThreadLogin;
    }

    @Inject
    public InnhentingSamletTjeneste(ArbeidsforholdTjeneste arbeidsforholdTjeneste,

                                    InntektTjeneste inntektTjeneste,
                                    InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste,
                                    FpwsproxyKlient fpwsproxyKlient,
                                    SystemuserThreadLogin systemuserThreadLogin) {
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.inntektTjeneste = inntektTjeneste;
        this.fpwsproxyKlient = fpwsproxyKlient;
        this.innhentingInfotrygdTjeneste = innhentingInfotrygdTjeneste;
        this.systemuserThreadLogin = systemuserThreadLogin;
    }

    public InntektsInformasjon getInntektsInformasjon(AktørId aktørId, IntervallEntitet periode, InntektskildeType kilde, YtelseType ytelse) {
        // inntektskomponenten splitter opp perioden i hele 12-månedersbolker og gjør sekvensielle kall bakover
        // vi splitter heller opp perioden her og gjør parallelle kall
        List<InntektsInformasjon> svarene = Collections.synchronizedList(new ArrayList<>());
        try (var scope = StructuredTaskScope.open()) {
            for (IntervallEntitet p : splittHver12Måned(periode)) {
                FinnInntektRequest.FinnInntektRequestBuilder builder = FinnInntektRequest.builder(YearMonth.from(p.getFomDato()), YearMonth.from(p.getTomDato()));
                builder.medAktørId(aktørId.getId());
                systemuserThreadLogin.fork(scope, () -> svarene.add(inntektTjeneste.finnInntekt(builder.build(), kilde, ytelse)));
            }
            try {
                scope.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        List<Månedsinntekt> alleMånedsinntekter = svarene.stream()
            .flatMap(it -> it.getMånedsinntekter().stream())
            .sorted(Comparator.comparing(Månedsinntekt::getMåned))
            .toList();
        return new InntektsInformasjon(alleMånedsinntekter, kilde);

    }

    static List<IntervallEntitet> splittHver12Måned(IntervallEntitet periode) {
        LocalDateInterval helePerioden = new LocalDateInterval(periode.getFomDato(), periode.getTomDato());
        LocalDate startFørsteMåned = helePerioden.getFomDato().withDayOfMonth(1);
        LocalDateInterval år = new LocalDateInterval(startFørsteMåned, startFørsteMåned.plusYears(1).minusDays(1));

        List<IntervallEntitet> splittedePerioder = new ArrayList<>();
        while (helePerioden.overlaps(år)) {
            LocalDateInterval overlapp = helePerioden.overlap(år).get();
            splittedePerioder.add(IntervallEntitet.fraOgMedTilOgMed(overlapp.getFomDato(), overlapp.getTomDato()));
            år = new LocalDateInterval(år.getFomDato().plusYears(1), år.getFomDato().plusYears(2).minusDays(1));
        }
        return splittedePerioder;
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforhold(AktørId aktørId,
                                                                                    PersonIdent ident,
                                                                                    IntervallEntitet opplysningsPeriode) {
        return arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(ident, aktørId, opplysningsPeriode);
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforholdFrilans(AktørId aktørId,
                                                                                           PersonIdent ident,
                                                                                           IntervallEntitet opplysningsPeriode) {
        return arbeidsforholdTjeneste.finnArbeidsforholdFrilansForIdentIPerioden(ident, aktørId, opplysningsPeriode);
    }

    public List<InfotrygdYtelseGrunnlag> innhentInfotrygdGrunnlag(PersonIdent ident, IntervallEntitet periode) {
        if (isDev) {
            return innhentingInfotrygdTjeneste.getInfotrygdYtelserFailSoft(ident, periode);
        }
        return innhentingInfotrygdTjeneste.getInfotrygdYtelser(ident, periode);
    }

    public List<InfotrygdYtelseGrunnlag> innhentSpokelseGrunnlag(PersonIdent ident, @SuppressWarnings("unused") IntervallEntitet periode) {
        return innhentingInfotrygdTjeneste.getSPøkelseYtelser(ident, periode.getFomDato());
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentDagpengerAAP(PersonIdent ident, IntervallEntitet opplysningsPeriode) {
        var fom = opplysningsPeriode.getFomDato();
        var tom = opplysningsPeriode.getTomDato();
        var saker = fpwsproxyKlient.hentDagpengerAAP(ident, fom, tom);
        return filtrerYtelserTjenester(saker);
    }

    private List<MeldekortUtbetalingsgrunnlagSak> filtrerYtelserTjenester(List<MeldekortUtbetalingsgrunnlagSak> saker) {
        List<MeldekortUtbetalingsgrunnlagSak> filtrert = new ArrayList<>();
        for (MeldekortUtbetalingsgrunnlagSak sak : saker) {
            if (sak.getKravMottattDato() == null) {
                if (sak.getVedtakStatus() == null) {
                    loggArenaIgnorert("vedtak", sak.getSaksnummer());
                } else {
                    loggArenaIgnorert("kravMottattDato", sak.getSaksnummer());
                }
            } else if (YtelseStatus.UNDER_BEHANDLING.equals(sak.getYtelseTilstand()) && sak.getMeldekortene().isEmpty()) {
                loggArenaIgnorert("meldekort", sak.getSaksnummer());
            } else if (sak.getVedtaksPeriodeFom() == null && sak.getMeldekortene().isEmpty()) {
                loggArenaIgnorert("vedtaksDato", sak.getSaksnummer());
            } else if (sak.getVedtaksPeriodeTom() != null && sak.getVedtaksPeriodeTom().isBefore(sak.getVedtaksPeriodeFom()) && sak.getMeldekortene()
                .isEmpty()) {
                loggArenaTomFørFom(sak.getSaksnummer());
            } else {
                filtrert.add(sak);
            }
        }
        return filtrert;
    }

    private void loggArenaIgnorert(String ignorert, Saksnummer saksnummer) {
        LOG.info("FP-112843 Ignorerer Arena-sak uten {}, saksnummer: {}", ignorert, saksnummer);
    }

    private void loggArenaTomFørFom(Saksnummer saksnummer) {
        LOG.info("FP-597341 Ignorerer Arena-sak med vedtakTom før vedtakFom, saksnummer: {}", saksnummer);
    }

}
