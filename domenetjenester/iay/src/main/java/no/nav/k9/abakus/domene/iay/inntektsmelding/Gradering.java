package no.nav.k9.abakus.domene.iay.inntektsmelding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.k9.abakus.felles.diff.ChangeTracked;
import no.nav.k9.abakus.felles.diff.IndexKeyComposer;
import no.nav.k9.abakus.felles.jpa.BaseEntitet;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.typer.Stillingsprosent;

@Entity(name = "Gradering")
@Table(name = "IAY_GRADERING")
public class Gradering extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GRADERING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmelding_id", nullable = false, updatable = false)
    private Inntektsmelding inntektsmelding;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "arbeidstid_prosent", updatable = false, nullable = false)))
    @ChangeTracked
    private Stillingsprosent arbeidstidProsent;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Gradering() {
    }

    private Gradering(IntervallEntitet periode, Stillingsprosent arbeidstidProsent) {
        this.arbeidstidProsent = arbeidstidProsent;
        this.periode = periode;
    }

    public Gradering(LocalDate fom, LocalDate tom, BigDecimal arbeidstidProsent) {
        this(tom == null ? IntervallEntitet.fraOgMed(fom) : IntervallEntitet.fraOgMedTilOgMed(fom, tom), Stillingsprosent.arbeid(arbeidstidProsent));
    }

    Gradering(Gradering gradering) {
        this(gradering.getPeriode(), gradering.getArbeidstidProsent());
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {periode};
        return IndexKeyComposer.createKey(keyParts);
    }

    void setInntektsmelding(Inntektsmelding inntektsmelding) {
        this.inntektsmelding = inntektsmelding;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    /**
     * En arbeidstaker kan kombinere foreldrepenger med deltidsarbeid.
     * <p>
     * Når arbeidstakeren jobber deltid, utgjør foreldrepengene differansen mellom deltidsarbeidet og en 100 prosent stilling.
     * Det er ingen nedre eller øvre grense for hvor mye eller lite arbeidstakeren kan arbeide.
     * <p>
     * Eksempel
     * Arbeidstaker A har en 100 % stilling og arbeider fem dager i uken. Arbeidstakeren ønsker å arbeide to dager i uken i foreldrepengeperioden.
     * Arbeidstids- prosenten blir da 40 %.
     * <p>
     * Arbeidstaker B har en 80 % stilling og arbeider fire dager i uken. Arbeidstakeren ønsker å arbeide to dager i uken i foreldrepengeperioden.
     * Arbeidstidprosenten blir også her 40 %.
     *
     * @return prosentsats
     */
    public Stillingsprosent getArbeidstidProsent() {
        return arbeidstidProsent;
    }

    @Override
    public String toString() {
        return "GraderingEntitet{" + "id=" + id + ", periode=" + periode + ", arbeidstidProsent=" + arbeidstidProsent + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Gradering)) {
            return false;
        }
        var that = (Gradering) o;
        return Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }
}
