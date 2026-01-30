package no.nav.k9.abakus.registerdata.ytelse.dagpenger;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;

import java.time.LocalDate;
import java.util.List;

public record DagpengerRettighetsperioderDto(
    @NotNull @Pattern(regexp = "^\\d{11}$", message = "Fnr har ikke gyldig verdi (pattern '{regexp}')") String personIdent,
    List<RettighetsperiodeDto> perioder) {

    public record RettighetsperiodeDto(
        LocalDate fraOgMedDato,
        LocalDate tilOgMedDato,
        int sats,
        int utbetaltBeløp,
        int gjenståendeDager,
        DagpengerKilde kilde) {
        public DagpengerRettighetsperiode tilDomeneModell() {
            return DagpengerRettighetsperiode.DagpengerRettighetsperiodeBuilder.ny()
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

