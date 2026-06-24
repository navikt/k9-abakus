package no.nav.k9.abakus.registerdata.ytelse.dagpenger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public record DagpengerBeregnetDagDto(
    List<RettighetsperiodeDto> perioder) {

    public record RettighetsperiodeDto(
        LocalDate fraOgMedDato,
        LocalDate tilOgMedDato,
        int sats,
        int utbetaltBeløp,
        int gjenståendeDager,
        DagpengerKilde kilde) {
        public DagpengerBeregnetPeriode tilDomeneModell() {
            return DagpengerBeregnetPeriode.DagpengerBeregnetPeriodeBuilder.ny()
                .medKilde(kilde)
                .medTilOgMedDato(tilOgMedDato)
                .medFraOgMedDato(fraOgMedDato)
                .medSats(sats)
                .medUtbetaltBeløp(utbetaltBeløp)
                .medGjenståendeDager(gjenståendeDager)
                .medUtbetalingsgrad((double) sats / (double)  utbetaltBeløp) // beholder desimaler
                .build();
        }
    }
}

