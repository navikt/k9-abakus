package no.nav.k9.abakus.registerdata.ytelse.dpsak;

import java.util.List;

import no.nav.fpsak.tidsserie.LocalDateInterval;

public record DpsakVedtak(LocalDateInterval periode, Integer dagsats, List<DpsakUtbetaling> utbetalinger) {

    public record DpsakUtbetaling(LocalDateInterval periode, Integer dagsats, Integer utbetaltBeløp, Integer sumUtbetalt) {}

}
