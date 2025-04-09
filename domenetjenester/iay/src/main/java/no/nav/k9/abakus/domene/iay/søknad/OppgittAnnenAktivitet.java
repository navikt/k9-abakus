package no.nav.k9.abakus.domene.iay.søknad;

import java.util.Comparator;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittAnnenAktivitetDto;
import no.nav.k9.abakus.felles.diff.ChangeTracked;
import no.nav.k9.abakus.felles.diff.IndexKeyComposer;
import no.nav.k9.abakus.felles.jpa.BaseEntitet;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.iay.jpa.ArbeidTypeKodeverdiConverter;


@Table(name = "IAY_ANNEN_AKTIVITET")
@Entity(name = "AnnenAktivitet")
public class OppgittAnnenAktivitet extends BaseEntitet implements IndexKey, Comparable<OppgittAnnenAktivitet> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ANNEN_AKTIVITET")
    private Long id;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjening oppgittOpptjening;

    @ChangeTracked
    @Convert(converter = ArbeidTypeKodeverdiConverter.class)
    @Column(name = "arbeid_type", nullable = false, updatable = false)
    private ArbeidType arbeidType;

    public OppgittAnnenAktivitet(IntervallEntitet periode, ArbeidType arbeidType) {
        this.periode = periode;
        this.arbeidType = arbeidType;
    }

    public OppgittAnnenAktivitet() {
        // hibernate
    }

    /* copy ctor*/
    public OppgittAnnenAktivitet(OppgittAnnenAktivitet orginal) {
        periode = orginal.getPeriode();
        arbeidType = orginal.getArbeidType();
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {periode, arbeidType};
        return IndexKeyComposer.createKey(keyParts);
    }

    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    public void setOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof OppgittAnnenAktivitet)) {
            return false;
        }
        var that = (OppgittAnnenAktivitet) o;
        return Objects.equals(periode, that.periode) && Objects.equals(arbeidType, that.arbeidType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidType);
    }

    @Override
    public String toString() {
        return "AnnenAktivitetEntitet{" + "id=" + id + ", periode=" + periode + ", arbeidType=" + arbeidType + '}';
    }

    @Override
    public int compareTo(OppgittAnnenAktivitet o) {
        Comparator<OppgittAnnenAktivitet> comparator = Comparator.comparing(
                (OppgittAnnenAktivitet dto) -> dto.getArbeidType() == null ? null : dto.getArbeidType().getKode(),
                Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(dto -> dto.getPeriode().getFomDato(), Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(dto -> dto.getPeriode().getTomDato(), Comparator.nullsLast(Comparator.naturalOrder()));

        return comparator.compare(this, o);
    }
}
