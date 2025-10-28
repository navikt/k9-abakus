package no.nav.k9.abakus.vedtak.domene.feil;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.felles.diff.DiffIgnore;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.abakus.vedtak.domene.YtelseTypeKodeverdiConverter;

@Entity(name = "VedtakYtelseFeilDump")
@Table(name = "VEDTAK_YTELSE_FEIL_DUMP")
public class VedtakYtelseFeilDump {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAK_YTELSE_FEIL_DUMP")
    private Long id;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Convert(converter = YtelseTypeKodeverdiConverter.class)
    @Column(name = "ytelse_type", nullable = false)
    private YtelseType ytelseType;

    @DiffIgnore
    @Column(name = "vedtatt_tidspunkt")
    private LocalDateTime vedtattTidspunkt;

    @NaturalId
    @Column(name = "vedtak_referanse")
    private UUID vedtakReferanse;

    @Embedded
    private IntervallEntitet periode;

    @Column(name = "har_sykepenger")
    private boolean harSykepenger;

    @Column(name = "har_foreldrepenger")
    private boolean harForeldrepenger;

    /**
     * Saksnummer (fra Arena, Infotrygd, ..).
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer")))
    private Saksnummer saksnummer;


    public VedtakYtelseFeilDump() {
        // hibernate
    }


    public VedtakYtelseFeilDump(AktørId aktørId,
                                YtelseType ytelseType,
                                LocalDateTime vedtattTidspunkt,
                                UUID vedtakReferanse,
                                IntervallEntitet periode,
                                boolean harSykepenger, boolean harForeldrepenger,
                                Saksnummer saksnummer) {
        this.aktørId = aktørId;
        this.ytelseType = ytelseType;
        this.vedtattTidspunkt = vedtattTidspunkt;
        this.vedtakReferanse = vedtakReferanse;
        this.periode = periode;
        this.harSykepenger = harSykepenger;
        this.harForeldrepenger = harForeldrepenger;
        this.saksnummer = saksnummer;
    }

}
