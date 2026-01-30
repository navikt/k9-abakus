package no.nav.k9.abakus.registerdata.ytelse.dagpenger;

import java.time.LocalDate;

public class DagpengerRettighetsperiode {

    private LocalDate fraOgMedDato;
    private LocalDate tilOgMedDato;
    private int sats;
    private int utbetaltBeløp;
    private int gjenståendeDager;
    private DagpengerKilde kilde;

    public DagpengerRettighetsperiode(LocalDate fraOgMedDato, LocalDate tilOgMedDato, DagpengerKilde kilde) {
        this.fraOgMedDato = fraOgMedDato;
        this.tilOgMedDato = tilOgMedDato;
        this.kilde = kilde;
    }

    public DagpengerRettighetsperiode() {
    }


    public LocalDate getFraOgMedDato() {
        return fraOgMedDato;
    }

    public LocalDate getTilOgMedDato() {
        return tilOgMedDato;
    }

    public int getSats() {
        return sats;
    }

    public int getUtbetaltBeløp() {
        return utbetaltBeløp;
    }

    public int getGjenståendeDager() {
        return gjenståendeDager;
    }

    public DagpengerKilde getKilde() {
        return kilde;
    }


    public static class DagpengerRettighetsperiodeBuilder {
        private final DagpengerRettighetsperiode dagpengerRettighetsperiode;

        DagpengerRettighetsperiodeBuilder(DagpengerRettighetsperiode dagpengerRettighetsperiode) {
            this.dagpengerRettighetsperiode = dagpengerRettighetsperiode;
        }

        public static DagpengerRettighetsperiode.DagpengerRettighetsperiodeBuilder ny() {
            return new DagpengerRettighetsperiode.DagpengerRettighetsperiodeBuilder(new DagpengerRettighetsperiode());
        }

        public DagpengerRettighetsperiode.DagpengerRettighetsperiodeBuilder medFraOgMedDato(LocalDate fraOgMedDato) {
            this.dagpengerRettighetsperiode.fraOgMedDato = fraOgMedDato;
            return this;
        }

        public DagpengerRettighetsperiode.DagpengerRettighetsperiodeBuilder medTilOgMedDato(LocalDate tilOgMedDato) {
            this.dagpengerRettighetsperiode.tilOgMedDato = tilOgMedDato;
            return this;
        }

        public DagpengerRettighetsperiode.DagpengerRettighetsperiodeBuilder medKilde(DagpengerKilde kilde) {
            this.dagpengerRettighetsperiode.kilde = kilde;
            return this;
        }

        public DagpengerRettighetsperiodeBuilder medSats(int sats) {
            this.dagpengerRettighetsperiode.sats = sats;
            return this;
        }

        public DagpengerRettighetsperiodeBuilder medUtbetaltBeløp(int utbetaltBeløp) {
            this.dagpengerRettighetsperiode.utbetaltBeløp = utbetaltBeløp;
            return this;
        }

        public DagpengerRettighetsperiodeBuilder medGjenståendeDager(int gjenståendeDager) {
            this.dagpengerRettighetsperiode.gjenståendeDager = gjenståendeDager;
            return this;
        }

        public DagpengerRettighetsperiode build() {
            return this.dagpengerRettighetsperiode;
        }

    }
}
