package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;

@Entity(name = "Inntekt")
@Table(name = "IAY_INNTEKT")
public class InntektEntitet extends BaseEntitet implements Inntekt, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKT")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aktoer_inntekt_id", nullable = false, updatable = false)
    private AktørInntektEntitet aktørInntekt;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "kilde", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + InntektsKilde.DISCRIMINATOR + "'"))})
    private InntektsKilde inntektsKilde;

    /* TODO: splitt inntektspostentitet klasse ? inntektspostentitet varierer med kilde. */
    @OneToMany(mappedBy = "inntekt")
    @ChangeTracked
    private Set<InntektspostEntitet> inntektspost = new LinkedHashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    InntektEntitet() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    InntektEntitet(Inntekt inntektMal) {
        this.inntektsKilde = inntektMal.getInntektsKilde();
        this.arbeidsgiver = inntektMal.getArbeidsgiver();
        this.inntektspost = inntektMal.getAlleInntektsposter().stream().map(ip -> {
            InntektspostEntitet inntektspostEntitet = new InntektspostEntitet(ip);
            inntektspostEntitet.setInntekt(this);
            return inntektspostEntitet;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { getArbeidsgiver(), getInntektsKilde() };
        return IndexKeyComposer.createKey(keyParts);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof InntektEntitet)) {
            return false;
        }
        InntektEntitet other = (InntektEntitet) obj;
        return Objects.equals(this.getInntektsKilde(), other.getInntektsKilde())
            && Objects.equals(this.getArbeidsgiver(), other.getArbeidsgiver());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInntektsKilde(), getArbeidsgiver());
    }

    @Override
    public InntektsKilde getInntektsKilde() {
        return inntektsKilde;
    }

    void setInntektsKilde(InntektsKilde inntektsKilde) {
        this.inntektsKilde = inntektsKilde;
    }

    @Override
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    @Override
    public Collection<Inntektspost> getAlleInntektsposter() {
        return Collections.unmodifiableSet(inntektspost);
    }

    void leggTilInntektspost(Inntektspost inntektspost) {
        InntektspostEntitet inntektspostEntitet = (InntektspostEntitet) inntektspost;
        inntektspostEntitet.setInntekt(this);
        this.inntektspost.add(inntektspostEntitet);
    }

    public AktørInntektEntitet getAktørInntekt() {
        return aktørInntekt;
    }

    void setAktørInntekt(AktørInntektEntitet aktørInntekt) {
        this.aktørInntekt = aktørInntekt;
    }

    public InntektspostBuilder getInntektspostBuilder() {
        return InntektspostBuilder.ny();
    }

    public boolean hasValues() {
        return arbeidsgiver != null || inntektsKilde != null || inntektspost != null;
    }
}
