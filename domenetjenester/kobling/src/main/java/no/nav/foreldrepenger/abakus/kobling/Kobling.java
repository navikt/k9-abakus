package no.nav.foreldrepenger.abakus.kobling;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.NaturalId;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

@Entity(name = "Kobling")
@Table(name = "KOBLING")
public class Kobling extends BaseEntitet implements IndexKey {

    /**
     * Abakus intern kobling_id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KOBLING")
    private Long id;

    /**
     * Saksnummer (gruppererer alle koblinger under samme saksnummer - typisk generert av FPSAK, eller annet saksbehandlingsystem)
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer")))
    private Saksnummer saksnummer;

    /**
     * Ekstern Referanse (eks. behandlingUuid).
     */
    @NaturalId
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "referanse", column = @Column(name = "kobling_referanse", updatable = false, unique = true))
    })
    private KoblingReferanse koblingReferanse;

    @Convert(converter = YtelseTypeKodeverdiConverter.class)
    @Column(name = "ytelse_type", nullable = false)
    private YtelseType ytelseType = YtelseType.UDEFINERT;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "bruker_aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fomDato", column = @Column(name = "opplysning_periode_fom")),
            @AttributeOverride(name = "tomDato", column = @Column(name = "opplysning_periode_tom"))
    })
    private IntervallEntitet opplysningsperiode;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fomDato", column = @Column(name = "opptjening_periode_fom")),
            @AttributeOverride(name = "tomDato", column = @Column(name = "opptjening_periode_tom"))
    })
    private IntervallEntitet opptjeningsperiode;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public Kobling() {
    }

    public Kobling(YtelseType ytelseType, Saksnummer saksnummer, KoblingReferanse koblingReferanse, AktørId aktørId) {
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
        this.koblingReferanse = Objects.requireNonNull(koblingReferanse, "koblingReferanse");
        this.aktørId = Objects.requireNonNull(aktørId, "aktørId");
        this.ytelseType = ytelseType == null ? YtelseType.UDEFINERT : ytelseType;
    }

    @Override
    public String getIndexKey() {
        return String.valueOf(koblingReferanse);
    }

    public Long getId() {
        return id;
    }

    public KoblingReferanse getKoblingReferanse() {
        return koblingReferanse;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public LocalDate getSkjæringstidspunkt() {
        return opplysningsperiode.getFomDato().plusDays(1);
    }

    public IntervallEntitet getOpplysningsperiode() {
        return opplysningsperiode;
    }

    public void setOpplysningsperiode(IntervallEntitet opplysningsperiode) {
        this.opplysningsperiode = opplysningsperiode;
    }

    public IntervallEntitet getOpptjeningsperiode() {
        return opptjeningsperiode;
    }

    public void setOpptjeningsperiode(IntervallEntitet opptjeningsperiode) {
        this.opptjeningsperiode = opptjeningsperiode;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public void setYtelseType(YtelseType ytelseType) {
        this.ytelseType = ytelseType;
    }

    @Override
    public String toString() {
        return "Kobling{" +
            "KoblingReferanse=" + koblingReferanse +
            ", saksnummer = " + saksnummer +
            ", opplysningsperiode=" + opplysningsperiode +
            ", opptjeningsperiode=" + opptjeningsperiode +
            '}';
    }
}
