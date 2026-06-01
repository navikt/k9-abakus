package no.nav.k9.abakus.domene;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public final class Hjelpetidslinjer {

    private Hjelpetidslinjer() {
    }

    /**
     * Lager en tidslinje der man kun har med helger ut fra tidslinjen som er oppgitt.
     *
     * @param tidslinje Tidslinjen som brukes for å hente helger for.
     * @return En tidslinje som av lørdager og søndager fra opprinnelig tidsserie, og ingenting annet
     */
    public static LocalDateTimeline<Boolean> lagTidslinjeMedKunHelger(LocalDateTimeline<?> tidslinje) {
        List<LocalDateSegment<Boolean>> helger = new ArrayList<>();
        for (LocalDateInterval intervall : tidslinje.getLocalDateIntervals()) {
            LocalDate d = intervall.getFomDato();
            while (!d.isAfter(intervall.getTomDato())) {
                if (d.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    helger.add(new LocalDateSegment<>(d, d, true));
                    d = d.plusDays(6);
                } else if (d.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    helger.add(new LocalDateSegment<>(d, d.isBefore(intervall.getTomDato()) ? d.plusDays(1) : d, true));
                    d = d.plusWeeks(1);
                } else {
                    d = d.plusDays(DayOfWeek.SATURDAY.getValue() - d.getDayOfWeek().getValue());
                }
            }
        }
        return new LocalDateTimeline<>(helger).compress();
    }

}
