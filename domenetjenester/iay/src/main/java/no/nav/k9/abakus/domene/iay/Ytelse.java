package no.nav.k9.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.felles.diff.ChangeTracked;
import no.nav.k9.abakus.felles.diff.IndexKeyComposer;
import no.nav.k9.abakus.felles.jpa.BaseEntitet;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.iay.jpa.YtelseStatusKodeverdiConverter;
import no.nav.k9.abakus.iay.jpa.YtelseTypeKodeverdiConverter;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.abakus.vedtak.domene.FagsystemKodeverdiConverter;

@Entity(name = "YtelseEntitet")
@Table(name = "IAY_RELATERT_YTELSE")
public class Ytelse extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE")
    private Long id;

    @OneToOne(mappedBy = "ytelse")
    private YtelseGrunnlag ytelseGrunnlag;

    @Convert(converter = YtelseTypeKodeverdiConverter.class)
    @Column(name = "ytelse_type", nullable = false)
    private YtelseType relatertYtelseType;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @ChangeTracked
    @Column(name = "vedtatt_tidspunkt")
    private LocalDateTime vedtattTidspunkt;

    @ChangeTracked
    @Convert(converter = YtelseStatusKodeverdiConverter.class)
    @Column(name = "status", nullable = false)
    private YtelseStatus status;

    /**
     * Saksnummer (fra Arena, Infotrygd, VLFP, K9, VLSP..).
     */
    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksreferanse")))
    private Saksnummer saksreferanse;

    @ChangeTracked
    @Convert(converter = FagsystemKodeverdiConverter.class)
    @Column(name = "kilde", nullable = false)
    private Fagsystem kilde;

    @OneToMany(mappedBy = "ytelse")
    @ChangeTracked
    private Set<YtelseAnvist> ytelseAnvist = new LinkedHashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "aktoer_ytelse_id", nullable = false, updatable = false)
    private AktørYtelse aktørYtelse;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Ytelse() {
        // hibernate
    }

    public Ytelse(Ytelse ytelse) {
        this.relatertYtelseType = ytelse.getRelatertYtelseType();
        this.status = ytelse.getStatus();
        this.vedtattTidspunkt = ytelse.getVedtattTidspunkt();
        this.periode = ytelse.getPeriode();
        this.saksreferanse = ytelse.getSaksreferanse();
        this.kilde = ytelse.getKilde();
        ytelse.getYtelseGrunnlag().ifPresent(yg -> {
            YtelseGrunnlag ygn = new YtelseGrunnlag(yg);
            ygn.setYtelse(this);
            this.ytelseGrunnlag = ygn;
        });
        this.ytelseAnvist = ytelse.getYtelseAnvist()
            .stream()
            .map(YtelseAnvist::new)
            .peek(it -> it.setYtelse(this))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {periode, relatertYtelseType, saksreferanse};
        return IndexKeyComposer.createKey(keyParts);
    }

    void setAktørYtelse(AktørYtelse aktørYtelse) {
        this.aktørYtelse = aktørYtelse;
    }

    public YtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    void setRelatertYtelseType(YtelseType relatertYtelseType) {
        this.relatertYtelseType = relatertYtelseType;
    }

    public YtelseStatus getStatus() {
        return status;
    }

    void setStatus(YtelseStatus status) {
        this.status = status;
    }

    public LocalDateTime getVedtattTidspunkt() {
        return vedtattTidspunkt;
    }

    public void setVedtattTidspunkt(LocalDateTime vedtattTidspunkt) {
        this.vedtattTidspunkt = vedtattTidspunkt;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(IntervallEntitet periode) {
        this.periode = periode;
    }

    public Saksnummer getSaksreferanse() {
        return saksreferanse;
    }

    void setSaksreferanse(Saksnummer saksnummer) {
        this.saksreferanse = saksnummer;
    }

    public Fagsystem getKilde() {
        return kilde;
    }

    void setKilde(Fagsystem kilde) {
        this.kilde = kilde;
    }

    public Optional<YtelseGrunnlag> getYtelseGrunnlag() {
        return Optional.ofNullable(ytelseGrunnlag);
    }

    void setYtelseGrunnlag(YtelseGrunnlag ytelseGrunnlag) {
        if (ytelseGrunnlag != null) {
            ytelseGrunnlag.setYtelse(this);
            this.ytelseGrunnlag = ytelseGrunnlag;
        }
    }

    public Collection<YtelseAnvist> getYtelseAnvist() {
        return Collections.unmodifiableCollection(ytelseAnvist);
    }

    void leggTilYtelseAnvist(YtelseAnvist ytelseAnvist) {
        ytelseAnvist.setYtelse(this);
        this.ytelseAnvist.add(ytelseAnvist);

    }

    void tilbakestillAnvisteYtelser() {
        ytelseAnvist.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Ytelse)) {
            return false;
        }
        var that = (Ytelse) o;
        return Objects.equals(relatertYtelseType, that.relatertYtelseType) && Objects.equals(saksreferanse, that.saksreferanse) &&
            (Objects.equals(periode, that.periode) || Objects.equals(periode.getFomDato(), that.periode.getFomDato()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(relatertYtelseType, periode, saksreferanse);
    }

    @Override
    public String toString() {
        return "YtelseEntitet{" +
            "relatertYtelseType=" + relatertYtelseType +
            ", periode=" + periode +
            ", relatertYtelseStatus=" + status +
            ", vedtattTidspunkt=" + vedtattTidspunkt +
            ", saksReferanse='" + saksreferanse + '\'' +
            '}';
    }
}
