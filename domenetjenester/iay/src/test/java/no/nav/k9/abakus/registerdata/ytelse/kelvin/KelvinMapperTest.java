package no.nav.k9.abakus.registerdata.ytelse.kelvin;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.k9.abakus.typer.Saksnummer;

// Alle tester er generert av Copilot, selve KelvinMapper er kopiert fra fp-abakus
class KelvinMapperTest {

    @Test
    void mapSingleVedtak_happyPath() {
        var meldekortFom = LocalDate.of(2026, 1, 1);
        var meldekortTom = LocalDate.of(2026, 1, 31);

        var utbetaling = new ArbeidsavklaringspengerResponse.AAPUtbetaling(
            new ArbeidsavklaringspengerResponse.AAPPeriode(meldekortFom, meldekortTom),
            1000,
            100,
            0,
            null,
            50
        );

        var vedtaksPeriodeFom = LocalDate.of(2026, 1, 1);
        var vedtaksPeriodeTom = LocalDate.of(2026, 12, 31);

        var vedtak = new ArbeidsavklaringspengerResponse.AAPVedtak(
            null, // barnMedStonad
            10,   // barnetillegg at vedtak-level
            null, // beregningsgrunnlag
            100,  // dagsats
            null, // dagsatsEtterUføreReduksjon
            ArbeidsavklaringspengerResponse.Kildesystem.KELVIN,
            new ArbeidsavklaringspengerResponse.AAPPeriode(vedtaksPeriodeFom, vedtaksPeriodeTom),
            "123",
            "AVSLUTTET",
            "v1",
            LocalDate.of(2026, 2, 1),
            List.of(utbetaling)
        );

        var mapped = KelvinMapper.mapTilMeldekortAclKelvin(List.of(vedtak), new Saksnummer("123"));

        assertThat(mapped).hasSize(1);
        MeldekortUtbetalingsgrunnlagSak sak = mapped.get(0);

        assertThat(sak.getYtelseType()).isEqualTo(YtelseType.ARBEIDSAVKLARINGSPENGER);
        assertThat(sak.getKilde()).isEqualTo(Fagsystem.KELVIN);
        assertThat(sak.getSaksnummer()).isNotNull();
        assertThat(sak.getSaksnummer().getVerdi()).isEqualTo("123");

        // vedtaksdagsats = aktuellDagsats (100) + barnetillegg (10) => 110
        assertThat(sak.getVedtaksDagsats().getVerdi()).isEqualByComparingTo(BigDecimal.valueOf(110));

        assertThat(sak.getMeldekortene()).hasSize(1);
        MeldekortUtbetalingsgrunnlagMeldekort mk = sak.getMeldekortene().get(0);
        assertThat(mk.getMeldekortFom()).isEqualTo(meldekortFom);
        assertThat(mk.getMeldekortTom()).isEqualTo(meldekortTom);

        // utbetaling.dagsats (100) + barnetillegg (0) => 100
        assertThat(mk.getDagsats()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(mk.getBeløp()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        // utbetalingsgrad should be preserved here (50)
        assertThat(mk.getUtbetalingsgrad()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void mapMultipleVedtak_sortedByVedtaksPeriodeFom() {
        var earlierFrom = LocalDate.of(2021, 1, 1);
        var earlierTo = LocalDate.of(2021, 1, 31);
        var laterFrom = LocalDate.of(2026, 1, 1);
        var laterTo = LocalDate.of(2026, 1, 31);

        var utbetaling = new ArbeidsavklaringspengerResponse.AAPUtbetaling(
            new ArbeidsavklaringspengerResponse.AAPPeriode(earlierFrom, earlierTo),
            500,
            50,
            0,
            null,
            100
        );

        var vedtakEarlier = new ArbeidsavklaringspengerResponse.AAPVedtak(
            null, 0, null, 50, null, ArbeidsavklaringspengerResponse.Kildesystem.KELVIN,
            new ArbeidsavklaringspengerResponse.AAPPeriode(earlierFrom, earlierTo), null, "OPEN", "e1", LocalDate.of(2021, 2, 1), List.of(utbetaling)
        );

        var vedtakLater = new ArbeidsavklaringspengerResponse.AAPVedtak(
            null, 0, null, 50, null, ArbeidsavklaringspengerResponse.Kildesystem.KELVIN,
            new ArbeidsavklaringspengerResponse.AAPPeriode(laterFrom, laterTo), null, "OPEN", "e2", LocalDate.of(2026, 2, 1), List.of(utbetaling)
        );

        // provide in reverse order to ensure mapper sorts by vedtaksPeriodeFom
        var mapped = KelvinMapper.mapTilMeldekortAclKelvin(List.of(vedtakLater, vedtakEarlier), new Saksnummer("X"));

        assertThat(mapped).hasSize(2);
        // first should be the earlier vedtak
        assertThat(mapped.get(0).getVedtaksPeriodeFom()).isEqualTo(earlierFrom);
        assertThat(mapped.get(1).getVedtaksPeriodeFom()).isEqualTo(laterFrom);
    }
}

