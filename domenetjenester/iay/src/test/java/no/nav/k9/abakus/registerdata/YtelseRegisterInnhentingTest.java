package no.nav.k9.abakus.registerdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.k9.abakus.domene.iay.Ytelse;
import no.nav.k9.abakus.domene.iay.YtelseAnvist;
import no.nav.k9.abakus.registerdata.ytelse.dpsak.DpsakVedtak;

// Tester er generert av nav-pilot
class YtelseRegisterInnhentingTest {

    private final YtelseRegisterInnhenting ytelseRegisterInnhenting = new YtelseRegisterInnhenting(null, null);

    @Test
    void skal_oversette_dpsak_vedtak_med_løpende_status() {
        // Arrange
        LocalDate idag = LocalDate.now();
        LocalDate fom = idag.minusMonths(2);
        LocalDate tom = idag.plusMonths(1);

        var utbetalinger = List.of(
            new DpsakVedtak.DpsakUtbetaling(
                new LocalDateInterval(fom, tom),
                100, // dagsats
                100, // dagutbetalt
                5000 // sumUtbetalt
            )
        );
        var vedtak = new DpsakVedtak(new LocalDateInterval(fom, tom), 100, utbetalinger);

        var builder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());

        // Act
        ytelseRegisterInnhenting.oversettDpsakTilYtelse(builder, vedtak);

        // Assert
        var ytelser = builder.build().getAlleYtelser();
        assertThat(ytelser).hasSize(1);

        Ytelse ytelse = ytelser.iterator().next();
        assertThat(ytelse.getRelatertYtelseType()).isEqualTo(YtelseType.DAGPENGER);
        assertThat(ytelse.getKilde()).isEqualTo(Fagsystem.DPSAK);
        assertThat(ytelse.getPeriode().getFomDato()).isEqualTo(fom);
        assertThat(ytelse.getPeriode().getTomDato()).isEqualTo(tom);
        assertThat(ytelse.getStatus()).isEqualTo(YtelseStatus.LØPENDE);

        assertThat(ytelse.getYtelseAnvist()).hasSize(1);
        YtelseAnvist anvist = ytelse.getYtelseAnvist().iterator().next();
        assertThat(anvist.getBeløp().get().getVerdi().intValue()).isEqualTo(5000);
        assertThat(anvist.getDagsats().get().getVerdi().intValue()).isEqualTo(100);
        assertThat(anvist.getUtbetalingsgradProsent().get().getVerdi().intValue()).isEqualTo(100);
    }

    @Test
    void skal_oversette_dpsak_vedtak_med_avsluttet_status() {
        // Arrange
        LocalDate fom = LocalDate.now().minusMonths(3);
        LocalDate tom = LocalDate.now().minusMonths(1);

        var utbetalinger = List.of(
            new DpsakVedtak.DpsakUtbetaling(
                new LocalDateInterval(fom, tom),
                100,
                100,
                5000
            )
        );
        var vedtak = new DpsakVedtak(new LocalDateInterval(fom, tom), 100, utbetalinger);

        var builder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());

        // Act
        ytelseRegisterInnhenting.oversettDpsakTilYtelse(builder, vedtak);

        // Assert
        Ytelse ytelse = builder.build().getAlleYtelser().iterator().next();
        assertThat(ytelse.getStatus()).isEqualTo(YtelseStatus.AVSLUTTET);
        YtelseAnvist anvist = ytelse.getYtelseAnvist().iterator().next();
        assertThat(anvist.getBeløp().get().getVerdi().intValue()).isEqualTo(5000);
        assertThat(anvist.getDagsats().get().getVerdi().intValue()).isEqualTo(100);
        assertThat(anvist.getUtbetalingsgradProsent().get().getVerdi().intValue()).isEqualTo(100);
    }

    @Test
    void skal_beregne_utbetalingsgrad_korrekt() {
        // Arrange
        LocalDate fom = LocalDate.now().minusMonths(1);
        LocalDate tom = LocalDate.now();

        var utbetalinger = List.of(
            new DpsakVedtak.DpsakUtbetaling(
                new LocalDateInterval(fom, tom),
                1000, // dagsats
                600,  // dagutbetalt
                18000 // sumUtbetalt
            )
        );
        var vedtak = new DpsakVedtak(new LocalDateInterval(fom, tom), 1000, utbetalinger);

        var builder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());

        // Act
        ytelseRegisterInnhenting.oversettDpsakTilYtelse(builder, vedtak);

        // Assert
        Ytelse ytelse = builder.build().getAlleYtelser().iterator().next();
        YtelseAnvist anvist = ytelse.getYtelseAnvist().iterator().next();

        // 600 * 100 / 1000 = 60%
        assertThat(anvist.getBeløp().get().getVerdi().intValue()).isEqualTo(18000);
        assertThat(anvist.getDagsats().get().getVerdi().intValue()).isEqualTo(1000);
        assertThat(anvist.getUtbetalingsgradProsent().get().getVerdi().intValue()).isEqualTo(60);
    }

    @Test
    void skal_oversette_flere_utbetalinger() {
        // Arrange
        LocalDate fom1 = LocalDate.now().minusMonths(2);
        LocalDate tom1 = LocalDate.now().minusMonths(1).minusDays(15);
        LocalDate fom2 = LocalDate.now().minusDays(14);
        LocalDate tom2 = LocalDate.now();

        var utbetalinger = List.of(
            new DpsakVedtak.DpsakUtbetaling(
                new LocalDateInterval(fom1, tom1),
                100,
                100,
                2000
            ),
            new DpsakVedtak.DpsakUtbetaling(
                new LocalDateInterval(fom2, tom2),
                100,
                80,
                1600
            )
        );
        var vedtak = new DpsakVedtak(new LocalDateInterval(fom1, tom2), 100, utbetalinger);

        var builder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());

        // Act
        ytelseRegisterInnhenting.oversettDpsakTilYtelse(builder, vedtak);

        // Assert
        Ytelse ytelse = builder.build().getAlleYtelser().iterator().next();
        Collection<YtelseAnvist> anvisninger = ytelse.getYtelseAnvist();
        assertThat(anvisninger).hasSize(2);

        var anvisteList = anvisninger.stream().toList();
        assertThat(anvisteList.get(0).getBeløp().get().getVerdi().intValue()).isEqualTo(2000);
        assertThat(anvisteList.get(0).getDagsats().get().getVerdi().intValue()).isEqualTo(100);
        assertThat(anvisteList.get(0).getUtbetalingsgradProsent().get().getVerdi().intValue()).isEqualTo(100);
        assertThat(anvisteList.get(1).getBeløp().get().getVerdi().intValue()).isEqualTo(1600);
        assertThat(anvisteList.get(1).getDagsats().get().getVerdi().intValue()).isEqualTo(100);
        assertThat(anvisteList.get(1).getUtbetalingsgradProsent().get().getVerdi().intValue()).isEqualTo(80);
    }

    @Test
    void skal_sette_vedtaksDagsats() {
        // Arrange
        LocalDate fom = LocalDate.now().minusMonths(1);
        LocalDate tom = LocalDate.now();

        var utbetalinger = List.of(
            new DpsakVedtak.DpsakUtbetaling(
                new LocalDateInterval(fom, tom),
                250,
                250,
                5000
            )
        );
        var vedtak = new DpsakVedtak(new LocalDateInterval(fom, tom), 250, utbetalinger);

        var builder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());

        // Act
        ytelseRegisterInnhenting.oversettDpsakTilYtelse(builder, vedtak);

        // Assert
        Ytelse ytelse = builder.build().getAlleYtelser().iterator().next();
        assertThat(ytelse.getYtelseGrunnlag().get().getVedtaksDagsats().get().getVerdi()).isEqualTo(new BigDecimal("250"));
    }

}
