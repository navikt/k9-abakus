package no.nav.k9.abakus.domene.iay.søknad;

import jakarta.persistence.*;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.k9.abakus.felles.diff.ChangeTracked;
import no.nav.k9.abakus.felles.diff.IndexKeyComposer;
import no.nav.k9.abakus.felles.jpa.BaseEntitet;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

/**
 * Entitetsklasse for oppgitt ytelse.
 * <p>
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 * <p>
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */
@Table(name = "IAY_OPPGITT_YTELSE")
@Entity(name = "OppgittYtelse")
public class OppgittYtelse extends BaseEntitet implements IndexKey, Comparable<OppgittYtelse> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OPPGITT_YTELSE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjening oppgittOpptjening;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @Column(name = "ytelse")
    private BigDecimal ytelse;

    public OppgittYtelse() {
        // hibernate
    }

    /* copy ctor */
    public OppgittYtelse(OppgittYtelse oppgittYtelse) {
        periode = oppgittYtelse.getPeriode();
        ytelse = oppgittYtelse.getYtelse();
    }

    @Override
    public String getIndexKey() {
        return IndexKeyComposer.createKey(periode);
    }

    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(IntervallEntitet periode) {
        this.periode = periode;
    }

    public BigDecimal getYtelse() {
        return ytelse;
    }

    void setYtelse(BigDecimal inntekt) {
        this.ytelse = inntekt;
    }

    void setOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof OppgittYtelse)) {
            return false;
        }

        var that = (OppgittYtelse) o;

        return Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(periode);
    }

    @Override
    public int compareTo(OppgittYtelse o) {
        return Comparator.comparing(OppgittYtelse::getFraOgMed)
                .thenComparing(OppgittYtelse::getTilOgMed)
                .compare(this, o);
    }
}
