package no.nav.k9.abakus.registerdata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.k9.abakus.domene.iay.YtelseBuilder;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.kobling.Kobling;
import no.nav.k9.abakus.registerdata.infotrygd.InfotrygdgrunnlagYtelseMapper;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.k9.abakus.registerdata.ytelse.dpsak.DpsakVedtak;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.Beløp;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.abakus.typer.Saksnummer;

public class YtelseRegisterInnhenting {
    private final InnhentingSamletTjeneste innhentingSamletTjeneste;
    private final VedtattYtelseInnhentingTjeneste vedtattYtelseInnhentingTjeneste;

    YtelseRegisterInnhenting(InnhentingSamletTjeneste innhentingSamletTjeneste, VedtattYtelseInnhentingTjeneste vedtattYtelseInnhentingTjeneste) {
        this.innhentingSamletTjeneste = innhentingSamletTjeneste;
        this.vedtattYtelseInnhentingTjeneste = vedtattYtelseInnhentingTjeneste;
    }

    void byggYtelser(Kobling behandling,
                     AktørId aktørId,
                     PersonIdent ident,
                     IntervallEntitet opplysningsPeriode,
                     InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder,
                     boolean medGrunnlag) {

        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(aktørId);
        aktørYtelseBuilder.tilbakestillYtelser();

        vedtattYtelseInnhentingTjeneste.innhentFraYtelsesRegister(aktørId, behandling, aktørYtelseBuilder);

        if (!medGrunnlag) {
            // Ikke lenger relevant å hente eksternt for 2part eller engangsstønad
            inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
            return;
        }

        List<InfotrygdYtelseGrunnlag> alleGrunnlag = innhentingSamletTjeneste.innhentInfotrygdGrunnlag(ident, opplysningsPeriode);
        alleGrunnlag.forEach(grunnlag -> InfotrygdgrunnlagYtelseMapper.oversettInfotrygdYtelseGrunnlagTilYtelse(aktørYtelseBuilder, grunnlag));

        List<InfotrygdYtelseGrunnlag> ghosts = innhentingSamletTjeneste.innhentSpokelseGrunnlag(ident, opplysningsPeriode);
        ghosts.forEach(grunnlag -> oversettSpokelseYtelseGrunnlagTilYtelse(aktørYtelseBuilder, grunnlag));

        List<MeldekortUtbetalingsgrunnlagSak> arena = innhentingSamletTjeneste.hentDagpengerAAPFraArena(ident, opplysningsPeriode);
        for (MeldekortUtbetalingsgrunnlagSak sak : arena) {
            oversettMeldekortUtbetalingsgrunnlagTilYtelse(aktørYtelseBuilder, sak);
        }

        var dagsaker = arena.stream().filter(s -> YtelseType.DAGPENGER.equals(s.getYtelseType())).toList();
        var dagpenger = innhentingSamletTjeneste.innhentDagpengerDpSak(ident, opplysningsPeriode, behandling.getSaksnummer(), dagsaker);

        for (var dpsakvedtak : dagpenger.getOrDefault(Fagsystem.DPSAK, List.of())) {
            oversettDpsakTilYtelse(aktørYtelseBuilder, dpsakvedtak);
        }

        var aapFraArena = arena.stream().filter(s -> YtelseType.ARBEIDSAVKLARINGSPENGER.equals(s.getYtelseType())).toList();
        List<MeldekortUtbetalingsgrunnlagSak> aapFraKelvin = innhentingSamletTjeneste.hentAapFraKelvin(ident, opplysningsPeriode, behandling.getSaksnummer(), aapFraArena);

        for (MeldekortUtbetalingsgrunnlagSak sak : aapFraKelvin) {
            oversettMeldekortUtbetalingsgrunnlagTilYtelse(aktørYtelseBuilder, sak);
        }

        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(aktørYtelseBuilder);
    }

    private void oversettSpokelseYtelseGrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                                         InfotrygdYtelseGrunnlag grunnlag) {
        IntervallEntitet periode = utledPeriodeNårTomMuligFørFom(grunnlag.getVedtaksPeriodeFom(), grunnlag.getVedtaksPeriodeTom());
        var saksnummer = new Saksnummer(grunnlag.getVedtaksreferanse());
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.VLSP, grunnlag.getYtelseType(), saksnummer)
            .medVedtattTidspunkt(grunnlag.getVedtattTidspunkt())
            .medPeriode(periode)
            .medStatus(grunnlag.getYtelseStatus());
        grunnlag.getUtbetaltePerioder().forEach(vedtak -> {
            final IntervallEntitet intervall = utledPeriodeNårTomMuligFørFom(vedtak.getUtbetaltFom(), vedtak.getUtbetaltTom());
            ytelseBuilder.leggtilYtelseAnvist(
                ytelseBuilder.getAnvistBuilder().medAnvistPeriode(intervall).medUtbetalingsgradProsent(vedtak.getUtbetalingsgrad()).build());
        });
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    // Tolker bare dagpenger fra DP-sak. Dapenger fra Arena via dp-datadeling blir en egen vurdering senere.
    // Forutsetter: Utbetaling har lik dagsats og lik utbetaltBeløp for alle dager i perioden (vilkårlig lengde). sumUtbetalt = utbetaltBeløp * virkedager.
    private void oversettDpsakTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder, DpsakVedtak dagpengerVedtak) {
        var førsteUtbetalingFom = dagpengerVedtak.utbetalinger().stream()
            .map(DpsakVedtak.DpsakUtbetaling::periode)
            .map(LocalDateInterval::getFomDato)
            .min(Comparator.naturalOrder());
        var periode = IntervallEntitet.fraOgMedTilOgMed(dagpengerVedtak.periode().getFomDato(), dagpengerVedtak.periode().getTomDato());
        var status = LocalDate.now().isBefore(dagpengerVedtak.periode().getTomDato()) ? YtelseStatus.LØPENDE : YtelseStatus.AVSLUTTET;
        var ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.DPSAK, YtelseType.DAGPENGER, periode, førsteUtbetalingFom);
        ytelseBuilder.medPeriode(periode)
            .medStatus(status)
            .medYtelseGrunnlag(ytelseBuilder.getGrunnlagBuilder()
                .medVedtaksDagsats(new Beløp(BigDecimal.valueOf(dagpengerVedtak.dagsats())))
                .build());
        for (var utbetaling : dagpengerVedtak.utbetalinger()) {
            var dagsats = BigDecimal.valueOf(utbetaling.dagsats());
            var utbetalingsgrad = BigDecimal.valueOf(utbetaling.dagutbetalt()).multiply(BigDecimal.valueOf(100))
                .divide(dagsats, 1, RoundingMode.HALF_UP);
            ytelseBuilder.leggtilYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                .medAnvistPeriode(IntervallEntitet.fraOgMedTilOgMed(utbetaling.periode().getFomDato(), utbetaling.periode().getTomDato()))
                .medBeløp(BigDecimal.valueOf(utbetaling.sumUtbetalt()))
                .medDagsats(dagsats)
                .medUtbetalingsgradProsent(utbetalingsgrad)
                .build());
        }
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private void oversettMeldekortUtbetalingsgrunnlagTilYtelse(InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder,
                                                               MeldekortUtbetalingsgrunnlagSak ytelse) {
        Optional<LocalDate> førsteMeldekortFom = finnFørsteMeldekortFom(ytelse);
        IntervallEntitet periode = utledMeldekortVedtaksPeriode(ytelse, førsteMeldekortFom);
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(ytelse.getKilde(), ytelse.getYtelseType(), ytelse.getSaksnummer(),
            periode, førsteMeldekortFom);
        ytelseBuilder.medPeriode(periode)
            .medStatus(ytelse.getYtelseTilstand())
            .medVedtattTidspunkt(ytelse.getVedtattDato().atStartOfDay())
            .medYtelseGrunnlag(ytelseBuilder.getGrunnlagBuilder()
                .medOpprinneligIdentdato(ytelse.getKravMottattDato())
                .medVedtaksDagsats(ytelse.getVedtaksDagsats())
                .build());
        for (MeldekortUtbetalingsgrunnlagMeldekort meldekort : ytelse.getMeldekortene()) {
            ytelseBuilder.leggtilYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                .medAnvistPeriode(utledPeriodeNårTomMuligFørFom(meldekort.getMeldekortFom(), meldekort.getMeldekortTom()))
                .medBeløp(meldekort.getBeløp())
                .medDagsats(meldekort.getDagsats())
                .medUtbetalingsgradProsent(meldekort.getUtbetalingsgrad())
                .build());
        }
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
    }

    private IntervallEntitet utledMeldekortVedtaksPeriode(MeldekortUtbetalingsgrunnlagSak sak, Optional<LocalDate> førsteMeldekortFom) {
        LocalDate fomFraSakMK = utledFomFraSakEllerMeldekortene(sak, førsteMeldekortFom);
        return utledPeriodeNårTomMuligFørFom(fomFraSakMK, sak.getVedtaksPeriodeTom());
    }

    private LocalDate utledFomFraSakEllerMeldekortene(MeldekortUtbetalingsgrunnlagSak sak, Optional<LocalDate> førsteMeldekortFom) {
        if (sak.getVedtaksPeriodeFom() != null) {
            return sak.getVedtaksPeriodeFom();
        }
        return førsteMeldekortFom.orElseGet(() -> sak.getVedtattDato() != null ? sak.getVedtattDato() : sak.getKravMottattDato());
    }

    private IntervallEntitet utledPeriodeNårTomMuligFørFom(LocalDate fom, LocalDate tom) {
        if (tom == null) {
            return IntervallEntitet.fraOgMed(fom);
        }
        if (tom.isBefore(fom)) {
            return IntervallEntitet.fraOgMedTilOgMed(fom, fom);
        }
        return IntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    private Optional<LocalDate> finnFørsteMeldekortFom(MeldekortUtbetalingsgrunnlagSak sak) {
        return sak.getMeldekortene().stream().map(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom).min(LocalDate::compareTo);
    }

}
