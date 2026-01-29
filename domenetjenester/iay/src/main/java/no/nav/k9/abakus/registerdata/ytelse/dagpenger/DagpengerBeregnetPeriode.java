package no.nav.k9.abakus.registerdata.ytelse.dagpenger;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.BiPredicate;

public class DagpengerBeregnetPeriode {

    private LocalDate fraOgMedDato;
    private LocalDate tilOgMedDato;
    private int sats;
    private int utbetaltBeløp;
    private int gjenståendeDager;
    private double utbetalingsgrad;
    private Fagsystem kilde;

    private DagpengerBeregnetPeriode() {
    }


    public LocalDate getFraOgMedDato() {
        return fraOgMedDato;
    }

    public LocalDate getTilOgMedDato() {
        return tilOgMedDato;
    }

    public int getsats() {
        return sats;
    }

    public int getUtbetaltBeløp() {
        return utbetaltBeløp;
    }

    public int getGjenståendeDager() {
        return gjenståendeDager;
    }

    public double getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public Fagsystem getKilde() {
        return kilde;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DagpengerBeregnetPeriode that)) return false;
        return sats == that.sats && utbetaltBeløp == that.utbetaltBeløp && kilde == that.kilde;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sats, utbetaltBeløp, kilde);
    }

    public static class DagpengerBeregnetPeriodeBuilder {
        private final DagpengerBeregnetPeriode bruttoUtbetaling;

        DagpengerBeregnetPeriodeBuilder(DagpengerBeregnetPeriode bruttoUtbetaling) {
            this.bruttoUtbetaling = bruttoUtbetaling;
        }

        public static DagpengerBeregnetPeriodeBuilder ny() {
            return new DagpengerBeregnetPeriodeBuilder(new DagpengerBeregnetPeriode());
        }

        public DagpengerBeregnetPeriodeBuilder medFraOgMedDato(LocalDate fraOgMedDato) {
            this.bruttoUtbetaling.fraOgMedDato = fraOgMedDato;
            return this;
        }

        public DagpengerBeregnetPeriodeBuilder medTilOgMedDato(LocalDate tilOgMedDato) {
            this.bruttoUtbetaling.tilOgMedDato = tilOgMedDato;
            return this;
        }

        public DagpengerBeregnetPeriodeBuilder medKilde(Fagsystem kilde) {
            this.bruttoUtbetaling.kilde = kilde;
            return this;
        }

        public DagpengerBeregnetPeriodeBuilder medKilde(DagpengerKilde kilde) {
            this.bruttoUtbetaling.kilde = kilde.equals(DagpengerKilde.DP_SAK) ? Fagsystem.DPSAK : Fagsystem.ARENA;
            return this;
        }

        public DagpengerBeregnetPeriodeBuilder medSats(int sats) {
            this.bruttoUtbetaling.sats = sats;
            return this;
        }

        public DagpengerBeregnetPeriodeBuilder medUtbetaltBeløp(int utbetaltBeløp) {
            this.bruttoUtbetaling.utbetaltBeløp = utbetaltBeløp;
            return this;
        }

        public DagpengerBeregnetPeriodeBuilder medUtbetalingsgrad(double utbetalingsgrad) {
            this.bruttoUtbetaling.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public DagpengerBeregnetPeriodeBuilder medGjenståendeDager(int gjenståendeDager) {
            this.bruttoUtbetaling.gjenståendeDager = gjenståendeDager;
            return this;
        }

        public DagpengerBeregnetPeriode build() {
            return this.bruttoUtbetaling;
        }

    }

    public static BiPredicate<DagpengerBeregnetPeriode, DagpengerBeregnetPeriode> getSammenligner() {
        // "Perioder" fra dp-sak består av bare 1 dag, så vi slår de sammen, det er opphold for helg, så periodene blir stort
        // sett fem dager lange. Arenadataene er allerede 14 dager, så de trengs ikke å slås mer sammen.
        return (lhs, rhs) ->
            lhs.getKilde().equals(Fagsystem.DPSAK) && lhs.getKilde().equals(rhs.getKilde())
                && lhs.getsats() == rhs.getsats() && lhs.getUtbetaltBeløp() == rhs.getUtbetaltBeløp();
    }

    public static LocalDateSegmentCombinator<DagpengerBeregnetPeriode, DagpengerBeregnetPeriode, DagpengerBeregnetPeriode> getKombinator() {
        return (datoInterval, lhs, rhs) ->
        {
            var kombinertUtbetaling = DagpengerBeregnetPeriodeBuilder.ny()
                .medFraOgMedDato(lhs.getValue().getFraOgMedDato())
                .medTilOgMedDato(rhs.getValue().getTilOgMedDato())
                .medKilde(lhs.getValue().getKilde())
                .medSats(lhs.getValue().getsats())
                .medGjenståendeDager(rhs.getValue().getGjenståendeDager())
                .medUtbetaltBeløp(lhs.getValue().getUtbetaltBeløp())
                .medUtbetalingsgrad(lhs.getValue().getUtbetalingsgrad())
                .build();
            return new LocalDateSegment<>(datoInterval, kombinertUtbetaling);
        };
    }

}
