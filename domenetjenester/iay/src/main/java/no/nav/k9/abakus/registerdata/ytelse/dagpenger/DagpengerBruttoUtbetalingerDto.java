package no.nav.k9.abakus.registerdata.ytelse.dagpenger;

import java.time.LocalDate;
import java.util.List;

public record DagpengerBruttoUtbetalingerDto(
    List<RettighetsperiodeDto> perioder) {

    public record RettighetsperiodeDto(
        LocalDate fraOgMedDato,
        LocalDate tilOgMedDato,
        int sats,
        int utbetaltBeløp,
        int gjenståendeDager,
        DagpengerKilde kilde) {
        public DagpengerBruttoUtbetaling tilDomeneModell() {
            return DagpengerBruttoUtbetaling.DagpengerBruttoUtbetalingerBuilder.ny()
                .medKilde(kilde)
                .medTilOgMedDato(tilOgMedDato)
                .medFraOgMedDato(fraOgMedDato)
                .medSats(sats)
                .medUtbetaltBeløp(utbetaltBeløp)
                .medGjenståendeDager(gjenståendeDager)
                .build();
        }
    }
}

