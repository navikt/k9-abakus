package no.nav.k9.abakus.registerdata.ytelse.dagpenger;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;

import java.time.LocalDate;
import java.util.List;

public record DagpengerRettighetsperioder(
    @NotNull @Pattern(regexp = "^\\d{11}$", message = "Fnr har ikke gyldig verdi (pattern '{regexp}')") String personIdent,
    List<Rettighetsperiode> perioder) {

    public record Rettighetsperiode(LocalDate fraOgMedDato, LocalDate tilOgMedDato, DagpengerKilde kilde) {
        public MeldekortUtbetalingsgrunnlagSak tilDomeneModell() {
            var kilde = this.kilde.equals(DagpengerKilde.DP_SAK) ? Fagsystem.DP_SAK : Fagsystem.ARENA;
            return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
                .medKilde(kilde)
                .medVedtaksPeriodeFom(fraOgMedDato)
                .medVedtaksPeriodeTom(tilOgMedDato)
                .medType(YtelseType.DAGPENGER)
                .build();
        }
    }

    public enum DagpengerKilde {
        DP_SAK,
        ARENA
    }
}
