package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Objects;
import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.diff.IndexKey;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
import no.nav.foreldrepenger.abakus.domene.virksomhet.VirksomhetEntitet;
import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "YtelseStørrelseEntitet")
@Table(name = "YTELSE_STOERRELSE")
public class YtelseStørrelseEntitet extends BaseEntitet implements YtelseStørrelse, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE_STOERRELSE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ytelse_grunnlag_id", nullable = false, updatable = false, unique = true)
    private YtelseGrunnlagEntitet ytelseGrunnlag;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "hyppighet", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + InntektPeriodeType.DISCRIMINATOR + "'"))
    @ChangeTracked
    private InntektPeriodeType hyppighet = InntektPeriodeType.UDEFINERT;

    @ManyToOne
    @JoinColumn(name = "virksomhet_id", updatable = false)
    @ChangeTracked
    private VirksomhetEntitet virksomhet;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep", nullable = false)))
    @ChangeTracked
    private Beløp beløp;

    public YtelseStørrelseEntitet() {
        // hibernate
    }

    public YtelseStørrelseEntitet(YtelseStørrelse ytelseStørrelse) {
        ytelseStørrelse.getVirksomhet().ifPresent(tidligereVirksomhet ->
            this.virksomhet = (VirksomhetEntitet) tidligereVirksomhet
        );
        this.beløp = ytelseStørrelse.getBeløp();
        this.hyppighet = ytelseStørrelse.getHyppighet();
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(virksomhet);
    }

    @Override
    public Optional<Virksomhet> getVirksomhet() {
        return Optional.ofNullable(virksomhet);
    }

    public void setVirksomhet(Virksomhet virksomhet) {
        this.virksomhet = (VirksomhetEntitet) virksomhet;
    }

    @Override
    public Beløp getBeløp() {
        return beløp;
    }

    public void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    @Override
    public InntektPeriodeType getHyppighet() {
        return hyppighet;
    }

    public void setHyppighet(InntektPeriodeType hyppighet) {
        this.hyppighet = hyppighet;
    }

    public void setYtelseGrunnlag(YtelseGrunnlagEntitet ytelseGrunnlag) {
        this.ytelseGrunnlag = ytelseGrunnlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YtelseStørrelseEntitet that = (YtelseStørrelseEntitet) o;
        return Objects.equals(virksomhet, that.virksomhet) &&
            Objects.equals(beløp, that.beløp) &&
            Objects.equals(hyppighet, that.hyppighet);
    }

    @Override
    public int hashCode() {

        return Objects.hash(virksomhet, beløp, hyppighet);
    }

    @Override
    public String toString() {
        return "YtelseStørrelseEntitet{" +
            "virksomhet=" + virksomhet +
            ", beløp=" + beløp +
            ", hyppighet=" + hyppighet +
            '}';
    }

    boolean hasValues() {
        return beløp != null || hyppighet != null || virksomhet != null;
    }
}
