package no.nav.k9.abakus.registerdata;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.k9.abakus.domene.iay.VersjonType;
import no.nav.k9.abakus.registerdata.ytelse.dagpenger.DagpengerBruttoUtbetaling;

import no.nav.k9.abakus.typer.AktørId;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
class YtelseRegisterInnhentingTest {

    YtelseRegisterInnhenting ytelseRegisterInnhenting = new YtelseRegisterInnhenting(null, null);
    InntektArbeidYtelseAggregatBuilder iayBuilder = InntektArbeidYtelseAggregatBuilder.builderFor(Optional.empty(), UUID.randomUUID(),
        LocalDateTime.of(2026, 2,2, 0, 0, 0), VersjonType.SAKSBEHANDLET);


    @Test
    public void skal_oversette_dagpenger_fra_dp_sak_til_ytelse_riktig() {
        var dagpengerUtbetaling = DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medKilde(Fagsystem.DPSAK)
            .medFraOgMedDato(LocalDate.of(2026, 2, 2))
            .medTilOgMedDato(LocalDate.of(2026, 2, 6))
            .medSats(500)
            .medUtbetaltBeløp(375)
            .build();

         var aktørYtelseBuilder = iayBuilder.getAktørYtelseBuilder(AktørId.dummy());

         ytelseRegisterInnhenting.oversettDagpengerTilAktørYtelse(aktørYtelseBuilder, dagpengerUtbetaling);

         var aktørYtelse = aktørYtelseBuilder.build();

         assertThat(aktørYtelse.getAlleYtelser().size()).isEqualTo(1);
        var anvistYtlse = aktørYtelse.getAlleYtelser().stream().findFirst().get().getYtelseAnvist().stream().findFirst().get();
        assertThat(anvistYtlse.getAnvistFOM()).isEqualTo(dagpengerUtbetaling.getFraOgMedDato());
        assertThat(anvistYtlse.getAnvistTOM()).isEqualTo(dagpengerUtbetaling.getTilOgMedDato());
        assertThat(anvistYtlse.getBeløp().get().getVerdi().intValue()).isEqualTo(375);
        assertThat(anvistYtlse.getDagsats().get().getVerdi().intValue()).isEqualTo(100);
        assertThat(anvistYtlse.getUtbetalingsgradProsent().get().getVerdi().intValue()).isEqualTo(75);
    }

    @Test
    public void skal_oversette_dagpenger_fra_arena_til_ytelse_riktig() {
        var dagpengerUtbetaling = DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
            .medKilde(Fagsystem.ARENA)
            .medFraOgMedDato(LocalDate.of(2026, 2, 2))
            .medTilOgMedDato(LocalDate.of(2026, 2, 15))
            .medSats(100)
            .medUtbetaltBeløp(750)
            .build();

        var aktørYtelseBuilder = iayBuilder.getAktørYtelseBuilder(AktørId.dummy());

        ytelseRegisterInnhenting.oversettDagpengerTilAktørYtelse(aktørYtelseBuilder, dagpengerUtbetaling);

        var aktørYtelse = aktørYtelseBuilder.build();

        assertThat(aktørYtelse.getAlleYtelser().size()).isEqualTo(1);
        var anvistYtlse = aktørYtelse.getAlleYtelser().stream().findFirst().get().getYtelseAnvist().stream().findFirst().get();
        assertThat(anvistYtlse.getAnvistFOM()).isEqualTo(dagpengerUtbetaling.getFraOgMedDato());
        assertThat(anvistYtlse.getAnvistTOM()).isEqualTo(dagpengerUtbetaling.getTilOgMedDato());
        assertThat(anvistYtlse.getBeløp().get().getVerdi().intValue()).isEqualTo(750);
        assertThat(anvistYtlse.getDagsats().get().getVerdi().intValue()).isEqualTo(100);
        assertThat(anvistYtlse.getUtbetalingsgradProsent().get().getVerdi().intValue()).isEqualTo(75);
    }
}


