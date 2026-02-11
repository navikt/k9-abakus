package no.nav.k9.abakus.registerdata.ytelse.kelvin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.k9.abakus.typer.Saksnummer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;

import static no.nav.k9.felles.konfigurasjon.konfig.Tid.TIDENES_BEGYNNELSE;
import static no.nav.k9.felles.konfigurasjon.konfig.Tid.TIDENES_ENDE;

public class ArenaMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ArenaMapper.class);

    private ArenaMapper() {
    }

    static List<no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak> mapTilMeldekortAclArena(
        List<ArbeidsavklaringspengerResponse.AAPVedtak> vedtak,
        LocalDate opplysningFom) {
        return vedtak.stream()
            .map(v -> mapTilMeldekortSakAclArena(v, opplysningFom))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagSak::getVedtaksPeriodeFom))
            .toList();
    }

    private static MeldekortUtbetalingsgrunnlagSak mapTilMeldekortSakAclArena(ArbeidsavklaringspengerResponse.AAPVedtak vedtak,
                                                                              LocalDate opplysningFom) {
        var mk = vedtak.utbetaling().stream()
            .map(ArenaMapper::mapTilMeldekortMKAclArena)
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom))
            .toList();
        if (mk.isEmpty() && vedtak.vedtaksdato().isBefore(opplysningFom) && // Kan hende det holder med sjekk på periode/tilOgMedDato / null
            (vedtak.periode() == null || vedtak.periode().tilOgMedDato() == null || vedtak.periode().fraOgMedDato() == null)) {
            return null;
        }
        var vedtaksdagsats = Optional.ofNullable(vedtak.dagsatsEtterUføreReduksjon()).or(() -> Optional.ofNullable(vedtak.dagsats())).orElse(0);
        var vedtaksdagsatsMedBarnetillegg = vedtaksdagsats + Optional.ofNullable(vedtak.barnetillegg()).orElse(0);
        return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .leggTilMeldekort(mk)
            .medType(YtelseType.ARBEIDSAVKLARINGSPENGER)
            .medTilstand(YtelseStatus.fra(vedtak.status()))
            .medKilde(Fagsystem.ARENA)
            .medSaksnummer(Optional.ofNullable(vedtak.saksnummer()).map(Saksnummer::new).orElse(null))
            .medKravMottattDato(vedtak.vedtaksdato())
            .medVedtattDato(vedtak.vedtaksdato())
            .medVedtaksPeriodeFom(vedtak.periode().fraOgMedDato() != null ? vedtak.periode().fraOgMedDato() : TIDENES_BEGYNNELSE)
            .medVedtaksPeriodeTom(vedtak.periode().tilOgMedDato() != null ? vedtak.periode().tilOgMedDato() : TIDENES_ENDE)
            .medVedtaksDagsats(BigDecimal.valueOf(vedtaksdagsatsMedBarnetillegg))
            .build();
    }

    private static MeldekortUtbetalingsgrunnlagMeldekort mapTilMeldekortMKAclArena(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling) {
        var beregnetUtbetalingsgrad = regnUtArenaUtbetalingsgrad(utbetaling);
        var utbetalingsgradFraUtbetaling = Optional.ofNullable(utbetaling.utbetalingsgrad()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        if (beregnetUtbetalingsgrad.compareTo(utbetalingsgradFraUtbetaling) != 0) {
            LOG.info("Kelvin-saker arena avvik utbetalingsgrad utbetaling {}: beregnet {}, oppgitt {}",
                utbetaling, beregnetUtbetalingsgrad, utbetaling.utbetalingsgrad());
        }
        return MeldekortUtbetalingsgrunnlagMeldekort.MeldekortMeldekortBuilder.ny()
            .medMeldekortFom(utbetaling.periode().fraOgMedDato() != null ? utbetaling.periode().fraOgMedDato() : TIDENES_BEGYNNELSE)
            .medMeldekortTom(utbetaling.periode().tilOgMedDato() != null ? utbetaling.periode().tilOgMedDato() : TIDENES_ENDE)
            .medBeløp(Optional.ofNullable(utbetaling.belop()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medDagsats(Optional.ofNullable(utbetaling.dagsats()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medUtbetalingsgrad(beregnetUtbetalingsgrad)
            .build();
    }

    private static BigDecimal regnUtArenaUtbetalingsgrad(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling) {
        var beløp = Optional.ofNullable(utbetaling.belop()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        var dagsats = Optional.ofNullable(utbetaling.dagsats()).map(BigDecimal::valueOf).orElse(BigDecimal.ONE);
        var virkedager = beregnVirkedager(utbetaling.periode().fraOgMedDato(), utbetaling.periode().tilOgMedDato());
        return beløp.multiply(BigDecimal.valueOf(200)).divide(dagsats.multiply(BigDecimal.valueOf(virkedager)), 1, RoundingMode.HALF_UP);
    }

    private static int beregnVirkedager(LocalDate fom, LocalDate tom) {
        try {
            var padBefore = fom.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            var padAfter = DayOfWeek.SUNDAY.getValue() - tom.getDayOfWeek().getValue();
            var virkedagerPadded = Math.toIntExact(
                ChronoUnit.WEEKS.between(fom.minusDays(padBefore), tom.plusDays(padAfter).plusDays(1L)) * 5L);
            var virkedagerPadding = Math.min(padBefore, 5) + Math.max(padAfter - 2, 0);
            return virkedagerPadded - virkedagerPadding;
        } catch (ArithmeticException var6) {
            throw new UnsupportedOperationException("Perioden er for lang til å beregne virkedager.", var6);
        }
    }
}
