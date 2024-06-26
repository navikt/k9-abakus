package no.nav.k9.abakus.domene.iay;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.abakus.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType;
import no.nav.k9.abakus.typer.Stillingsprosent;

public class PermisjonBuilder {
    private final Permisjon permisjon;

    PermisjonBuilder(Permisjon permisjon) {
        this.permisjon = permisjon;
    }

    static PermisjonBuilder ny() {
        return new PermisjonBuilder(new Permisjon());
    }

    public PermisjonBuilder medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
        this.permisjon.setPermisjonsbeskrivelseType(permisjonsbeskrivelseType);
        return this;
    }

    public PermisjonBuilder medProsentsats(BigDecimal prosentsats) {
        this.permisjon.setProsentsats(Stillingsprosent.arbeid(prosentsats));
        return this;
    }

    public PermisjonBuilder medPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
        this.permisjon.setPeriode(fraOgMed, tilOgMed);
        return this;
    }

    public Permisjon build() {
        if (permisjon.hasValues()) {
            return permisjon;
        }
        throw new IllegalStateException();
    }

}
