package no.nav.k9.abakus.domene.iay;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.k9.abakus.felles.diff.ChangeTracked;
import no.nav.k9.abakus.felles.diff.IndexKeyComposer;
import no.nav.k9.abakus.felles.jpa.BaseEntitet;
import no.nav.k9.abakus.typer.AktørId;

@Table(name = "IAY_AKTOER_ARBEID")
@Entity(name = "AktørArbeid")
public class AktørArbeid extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKTOER_ARBEID")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false)))
    private AktørId aktørId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntekt_arbeid_ytelser_id", nullable = false, updatable = false)
    private InntektArbeidYtelseAggregat inntektArbeidYtelser;

    @OneToMany(mappedBy = "aktørArbeid")
    @ChangeTracked
    private Set<Yrkesaktivitet> yrkesaktiviter = new LinkedHashSet<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    AktørArbeid() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktørArbeid(AktørArbeid aktørArbeid) {
        this.aktørId = aktørArbeid.getAktørId();
        this.yrkesaktiviter = aktørArbeid.yrkesaktiviter.stream().map(yrkesaktivitet -> {
            Yrkesaktivitet yrkes = new Yrkesaktivitet(yrkesaktivitet);
            yrkes.setAktørArbeid(this);
            return yrkes;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        return IndexKeyComposer.createKey(getAktørId());
    }

    /**
     * Aktøren som avtalene gjelder for
     *
     * @return aktørId
     */
    public AktørId getAktørId() {
        return aktørId;
    }

    void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    /**
     * Alle yrkesaktiviteter (ufiltret ifht skjæringstidspunkt vurdering. )
     */
    public Collection<Yrkesaktivitet> hentAlleYrkesaktiviteter() {
        return Set.copyOf(yrkesaktiviter);
    }

    void setYrkesaktiviter() {
        this.yrkesaktiviter = new LinkedHashSet<>();
    }

    void setInntektArbeidYtelser(InntektArbeidYtelseAggregat inntektArbeidYtelser) {
        this.inntektArbeidYtelser = inntektArbeidYtelser;
    }

    boolean hasValues() {
        return aktørId != null || yrkesaktiviter != null;
    }

    YrkesaktivitetBuilder getYrkesaktivitetBuilderForNøkkel(Opptjeningsnøkkel identifikator, ArbeidType arbeidType) {
        Optional<Yrkesaktivitet> yrkesaktivitet = yrkesaktiviter.stream()
            .filter(ya -> ya.getArbeidType().equals(arbeidType) && new Opptjeningsnøkkel(ya).equals(identifikator))
            .findFirst();
        final YrkesaktivitetBuilder oppdatere = YrkesaktivitetBuilder.oppdatere(yrkesaktivitet);
        oppdatere.medArbeidType(arbeidType);
        return oppdatere;
    }

    YrkesaktivitetBuilder getYrkesaktivitetBuilderForNøkkel(Opptjeningsnøkkel identifikator, Set<ArbeidType> arbeidTyper) {
        Optional<Yrkesaktivitet> yrkesaktivitet = yrkesaktiviter.stream()
            .filter(ya -> arbeidTyper.contains(ya.getArbeidType()) && new Opptjeningsnøkkel(ya).equals(identifikator))
            .findFirst();
        final YrkesaktivitetBuilder oppdatere = YrkesaktivitetBuilder.oppdatere(yrkesaktivitet);
        if (!oppdatere.getErOppdatering()) {
            // Defaulter til ordinert arbeidsforhold hvis saksbehandler har lagt til fra GUI
            oppdatere.medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        }
        return oppdatere;
    }

    YrkesaktivitetBuilder getYrkesaktivitetBuilderForType(ArbeidType type) {
        Optional<Yrkesaktivitet> yrkesaktivitet = yrkesaktiviter.stream().filter(ya -> ya.getArbeidType().equals(type)).findFirst();
        final YrkesaktivitetBuilder oppdatere = YrkesaktivitetBuilder.oppdatere(yrkesaktivitet);
        oppdatere.medArbeidType(type);
        return oppdatere;
    }

    void leggTilYrkesaktivitet(Yrkesaktivitet yrkesaktivitet) {
        this.yrkesaktiviter.add(yrkesaktivitet);
        yrkesaktivitet.setAktørArbeid(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørArbeid)) {
            return false;
        }
        AktørArbeid other = (AktørArbeid) obj;
        return Objects.equals(this.getAktørId(), other.getAktørId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "aktørId=" + aktørId + ", yrkesaktiviteter=" + yrkesaktiviter + '>';
    }

    void tilbakestillYrkesaktiviteter() {
        this.yrkesaktiviter = yrkesaktiviter.stream().filter(Yrkesaktivitet::erYrkesaktivitetMedLegacyInnhold).collect(Collectors.toSet());
    }

}
