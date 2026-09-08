package no.nav.k9.abakus.registerdata;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.k9.abakus.registerdata.ytelse.dagpenger.DagpengerBruttoUtbetaling;
import graphql.com.google.common.collect.ImmutableList;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.k9.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.k9.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.k9.abakus.registerdata.ytelse.arena.FpwsproxyKlient;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.k9.abakus.registerdata.ytelse.dagpenger.DpSakRestKlient;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.k9.abakus.registerdata.ytelse.kelvin.KelvinRestKlient;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.felles.konfigurasjon.env.Environment;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@ApplicationScoped
public class InnhentingSamletTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(InnhentingSamletTjeneste.class);
    private final boolean isDev = Environment.current().isDev();

    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private InntektTjeneste inntektTjeneste;
    private FpwsproxyKlient fpwsproxyKlient;
    private DpSakRestKlient dpSakRestKlient;
    private boolean skalHenteDagpengerFraDpSak;
    private InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste;
    private KelvinRestKlient kelvinRestKlient;

    InnhentingSamletTjeneste() {
        //CDI
    }

    @Inject
    public InnhentingSamletTjeneste(ArbeidsforholdTjeneste arbeidsforholdTjeneste,
                                    InntektTjeneste inntektTjeneste,
                                    InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste,
                                    FpwsproxyKlient fpwsproxyKlient,
                                    KelvinRestKlient kelvinRestKlient,
                                    DpSakRestKlient dpSakRestKlient,
                                    @KonfigVerdi(value = "SKAL_HENTE_DAGPEMGER_FRA_DPSAK", defaultVerdi = "false") boolean skalHenteDagpengerFraDpSak) {
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.inntektTjeneste = inntektTjeneste;
        this.fpwsproxyKlient = fpwsproxyKlient;
        this.innhentingInfotrygdTjeneste = innhentingInfotrygdTjeneste;
        this.kelvinRestKlient = kelvinRestKlient;
        this.dpSakRestKlient = dpSakRestKlient;

        this.skalHenteDagpengerFraDpSak = skalHenteDagpengerFraDpSak;
    }


    public Map<InntektskildeType, InntektsInformasjon> getInntektsInformasjon(AktørId aktørId,
                                                                              IntervallEntitet periode,
                                                                              Set<InntektskildeType> kilder,
                                                                              YtelseType ytelseType) {
        InntektTjeneste.YearMonthPeriode månedsperiode = new InntektTjeneste.YearMonthPeriode(
            YearMonth.from(periode.getFomDato()),
            YearMonth.from(periode.getTomDato()));
        return inntektTjeneste.finnInntekt(aktørId, månedsperiode, kilder, ytelseType);
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

    public List<MeldekortUtbetalingsgrunnlagSak> innhentMaksimumAAP(
        PersonIdent ident,
        IntervallEntitet opplysningsPeriode,
        Saksnummer saksnummer, List<MeldekortUtbetalingsgrunnlagSak> aapFraArena,
        LocalDate skjæringstidspunkt) {

        try {
            var aapGrunnlag = kelvinRestKlient.hentAAP(ident, opplysningsPeriode.getFomDato(), opplysningsPeriode.getTomDato(), saksnummer);
            var grunnlagFraKelvin = aapGrunnlag.get(Fagsystem.KELVIN);
            var arenaGrunnlagFraKelvin = aapGrunnlag.get(Fagsystem.ARENA);

            var overlappStp = grunnlagFraKelvin.stream().anyMatch(v -> v.getVedtaksPeriodeFom().isBefore(skjæringstidspunkt) && v.getVedtaksPeriodeTom().isAfter(skjæringstidspunkt));
            if (overlappStp) {
                var saksnumreAAP = grunnlagFraKelvin.stream()
                    .map(MeldekortUtbetalingsgrunnlagSak::getSaksnummer)
                    .map(Saksnummer::getVerdi)
                    .collect(Collectors.joining(", "));
                LOG.warn("Sak {} har innhentet nye Arbeidsavklaringspenger fra Kelvin saker {}. Kontakt produkteier for validering", saksnummer.getVerdi(), saksnumreAAP);
            }
            return ImmutableList.<MeldekortUtbetalingsgrunnlagSak>builder()
                .addAll(grunnlagFraKelvin)
                .addAll(arenaGrunnlagFraKelvin)
                .build();
        } catch (Exception e) {
            LOG.error("Sammenligning med Kelvin feilet.", e);
        }
        return Collections.emptyList();
    }

    public List<DagpengerBruttoUtbetaling> hentDagpengerRettighetsperioder(PersonIdent personIdent, IntervallEntitet opplysningsPeriode) {
        if (!skalHenteDagpengerFraDpSak) {
            return Collections.emptyList();
        }
        var utbetalinger = dpSakRestKlient.hentBruttoUtbetalinger(personIdent, opplysningsPeriode.getFomDato(), opplysningsPeriode.getTomDato());
        var utbetalingTidslinjeSegmenter = utbetalinger.stream().map(bruttoUtbetaling ->
            new LocalDateSegment<>(bruttoUtbetaling.getFraOgMedDato(), bruttoUtbetaling.getTilOgMedDato(), bruttoUtbetaling)).toList();
        var utbetalingTidslinje = new LocalDateTimeline<>(utbetalingTidslinjeSegmenter);

        return utbetalingTidslinje.compress(getBruttoUtbetalingSammenligner(), getBruttoUtbetalingKombinator())
            .stream().map(LocalDateSegment::getValue).collect(Collectors.toList());
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentAAP(PersonIdent ident, IntervallEntitet opplysningsPeriode) {
        var fom = opplysningsPeriode.getFomDato();
        var tom = opplysningsPeriode.getTomDato();
        var dagpengerOgAAP = fpwsproxyKlient.hentDagpengerAAP(ident, fom, tom);

        if (!skalHenteDagpengerFraDpSak) {
            return filtrerYtelserTjenester(dagpengerOgAAP);
        }
        var aap = dagpengerOgAAP.stream().filter(sak -> !sak.getYtelseType().equals(YtelseType.DAGPENGER)).collect(Collectors.toList());
        return filtrerYtelserTjenester(aap);
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

    private static LocalDateSegmentCombinator<DagpengerBruttoUtbetaling, DagpengerBruttoUtbetaling, DagpengerBruttoUtbetaling> getBruttoUtbetalingKombinator() {
        return (datoInterval, lhs, rhs) ->
        {
            var kombinertUtbetaling = DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
                .medFraOgMedDato(lhs.getValue().getFraOgMedDato())
                .medTilOgMedDato(rhs.getValue().getTilOgMedDato())
                .medKilde(lhs.getValue().getKilde())
                .medSats(lhs.getValue().getsats() + rhs.getValue().getsats())
                .medGjenståendeDager(rhs.getValue().getGjenståendeDager())
                .medUtbetaltBeløp(lhs.getValue().getUtbetaltBeløp() + rhs.getValue().getUtbetaltBeløp())
                .build();
            return new LocalDateSegment<>(datoInterval, kombinertUtbetaling);
        };
    }

    private static BiPredicate<DagpengerBruttoUtbetaling, DagpengerBruttoUtbetaling>    getBruttoUtbetalingSammenligner() {
        // "Perioder" fra dp-sak består av bare 1 dag, så vi slår de sammen, det er opphold for helg, så periodene blir stort
        // sett fem dager lange. Arenadataene er allerede 14 dager, så de trengs ikke å slås mer sammen.
        return (lhs, rhs) ->
            lhs.getKilde().equals(Fagsystem.DPSAK) && lhs.getKilde().equals(rhs.getKilde());
    }

}
