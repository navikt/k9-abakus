package no.nav.k9.abakus.domene.iay.søknad;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import no.nav.abakus.iaygrunnlag.kodeverk.Landkode;
import no.nav.k9.abakus.felles.diff.ChangeTracked;
import no.nav.k9.abakus.felles.diff.IndexKeyComposer;
import no.nav.k9.abakus.felles.jpa.BaseEntitet;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.iay.jpa.ArbeidTypeKodeverdiConverter;
import no.nav.k9.abakus.iay.jpa.LandKodeKodeverdiConverter;
import no.nav.k9.felles.jpa.converters.BooleanToStringConverter;

/**
 * Entitetsklasse for oppgitte arbeidsforhold.
 * <p>
 * Implementert iht. builder pattern (ref. "Effective Java, 2. ed." J.Bloch).
 * Non-public constructors og setters, dvs. immutable.
 * <p>
 * OBS: Legger man til nye felter så skal dette oppdateres mange steder:
 * builder, equals, hashcode etc.
 */
@Table(name = "IAY_OPPGITT_ARBEIDSFORHOLD")
@Entity(name = "OppgittArbeidsforhold")
public class OppgittArbeidsforhold extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OPPGITT_ARBEIDSFORHOLD")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjening oppgittOpptjening;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "utenlandsk_inntekt", nullable = false)
    private boolean erUtenlandskInntekt;

    @Convert(converter = ArbeidTypeKodeverdiConverter.class)
    @Column(name = "arbeid_type", nullable = false, updatable = false)
    @ChangeTracked
    private ArbeidType arbeidType;

    @Convert(converter = LandKodeKodeverdiConverter.class)
    @Column(name = "land", nullable = false)
    private Landkode landkode;

    @Column(name = "utenlandsk_virksomhet_navn")
    private String utenlandskVirksomhetNavn;

    @Column(name = "inntekt")
    private BigDecimal inntekt;

    public OppgittArbeidsforhold() {
        // hibernate
    }

    /* copy ctor */
    public OppgittArbeidsforhold(OppgittArbeidsforhold oppgittArbeidsforhold) {
        periode = oppgittArbeidsforhold.getPeriode();
        erUtenlandskInntekt = oppgittArbeidsforhold.erUtenlandskInntekt();
        arbeidType = oppgittArbeidsforhold.getArbeidType();
        landkode = oppgittArbeidsforhold.getLandkode();
        utenlandskVirksomhetNavn = oppgittArbeidsforhold.getUtenlandskVirksomhetNavn();
        inntekt = oppgittArbeidsforhold.getInntekt();
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {periode, landkode, utenlandskVirksomhetNavn, arbeidType};
        return IndexKeyComposer.createKey(keyParts);
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

    public Boolean erUtenlandskInntekt() {
        return erUtenlandskInntekt;
    }

    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    void setArbeidType(ArbeidType arbeidType) {
        this.arbeidType = arbeidType;
    }

    public Landkode getLandkode() {
        return landkode;
    }

    void setLandkode(Landkode landkode) {
        this.landkode = Objects.requireNonNull(landkode, "landkode");
    }

    public String getUtenlandskVirksomhetNavn() {
        return utenlandskVirksomhetNavn;
    }

    void setUtenlandskVirksomhetNavn(String utenlandskVirksomhetNavn) {
        this.utenlandskVirksomhetNavn = utenlandskVirksomhetNavn;
    }

    void setOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    void setErUtenlandskInntekt(Boolean erUtenlandskInntekt) {
        this.erUtenlandskInntekt = erUtenlandskInntekt;
    }

    public BigDecimal getInntekt() {
        return inntekt;
    }

    void setInntekt(BigDecimal inntekt) {
        this.inntekt = inntekt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof OppgittArbeidsforhold)) {
            return false;
        }

        var that = (OppgittArbeidsforhold) o;

        return Objects.equals(periode, that.periode) && Objects.equals(arbeidType, that.arbeidType) && Objects.equals(utenlandskVirksomhetNavn,
            that.utenlandskVirksomhetNavn) && Objects.equals(landkode, that.landkode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidType, landkode, utenlandskVirksomhetNavn);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "id=" + id + ", periode=" + periode + ", erUtenlandskInntekt=" + erUtenlandskInntekt
            + ", arbeidType=" + arbeidType + ", landkode=" + landkode + ", utenlandskVirksomhetNavn=" + utenlandskVirksomhetNavn + '>';
    }
}
