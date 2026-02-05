package no.nav.k9.abakus.registerdata.ytelse.dagpenger;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;

import java.time.LocalDate;

public class DagpengerBruttoUtbetaling {

    private LocalDate fraOgMedDato;
    private LocalDate tilOgMedDato;
    private int sats;
    private int utbetaltBeløp;
    private int gjenståendeDager;
    private Fagsystem kilde;

    private DagpengerBruttoUtbetaling() {
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

    public Fagsystem getKilde() {
        return kilde;
    }

    public static class DagpengerBruttoUtbetalingerBuilder {
        private final DagpengerBruttoUtbetaling bruttoUtbetaling;

        DagpengerBruttoUtbetalingerBuilder(DagpengerBruttoUtbetaling bruttoUtbetaling) {
            this.bruttoUtbetaling = bruttoUtbetaling;
        }

        public static DagpengerBruttoUtbetalingerBuilder ny() {
            return new DagpengerBruttoUtbetalingerBuilder(new DagpengerBruttoUtbetaling());
        }

        public DagpengerBruttoUtbetalingerBuilder medFraOgMedDato(LocalDate fraOgMedDato) {
            this.bruttoUtbetaling.fraOgMedDato = fraOgMedDato;
            return this;
        }

        public DagpengerBruttoUtbetalingerBuilder medTilOgMedDato(LocalDate tilOgMedDato) {
            this.bruttoUtbetaling.tilOgMedDato = tilOgMedDato;
            return this;
        }

        public DagpengerBruttoUtbetalingerBuilder medKilde(Fagsystem kilde) {
            this.bruttoUtbetaling.kilde = kilde;
            return this;
        }

        public DagpengerBruttoUtbetalingerBuilder medKilde(DagpengerKilde kilde) {
            this.bruttoUtbetaling.kilde = kilde.equals(DagpengerKilde.DP_SAK) ? Fagsystem.DP_SAK : Fagsystem.ARENA;
            return this;
        }

        public DagpengerBruttoUtbetalingerBuilder medSats(int sats) {
            this.bruttoUtbetaling.sats = sats;
            return this;
        }

        public DagpengerBruttoUtbetalingerBuilder medUtbetaltBeløp(int utbetaltBeløp) {
            this.bruttoUtbetaling.utbetaltBeløp = utbetaltBeløp;
            return this;
        }

        public DagpengerBruttoUtbetalingerBuilder medGjenståendeDager(int gjenståendeDager) {
            this.bruttoUtbetaling.gjenståendeDager = gjenståendeDager;
            return this;
        }

        public DagpengerBruttoUtbetaling build() {
            return this.bruttoUtbetaling;
        }

    }
}
