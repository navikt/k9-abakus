package no.nav.abakus.callback.registerdata;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Metadata for callback
 * Inneholder:
 * - opprettetTidspunkt: Tidspunktet hvor det oppdaterte grunnlaget ble commitet til database
 * - avsenderRef: avsenderRef til behandlingen
 * - opprinneligGrunnlagRef: tilstand før registerinnhenting, referanse til opprinnelig grunnlag
 * - oppdatertGrunnlagRef: resultatet av registerinnhenting, referanse til nytt grunnlag
 * - grunnlagType: datastrukturen callbacket omhandler
 */
public class CallbackDto {

    @NotNull
    @Valid
    private LocalDateTime opprettetTidspunkt;

    @NotNull
    @Valid
    private ReferanseDto avsenderRef;

    @Valid
    private ReferanseDto opprinneligGrunnlagRef;

    @Valid
    private ReferanseDto oppdatertGrunnlagRef;

    @NotNull
    @Valid
    private Grunnlag grunnlagType;

    public ReferanseDto getAvsenderRef() {
        return avsenderRef;
    }

    public void setAvsenderRef(ReferanseDto avsenderRef) {
        this.avsenderRef = avsenderRef;
    }

    public ReferanseDto getOppdatertGrunnlagRef() {
        return oppdatertGrunnlagRef;
    }

    public void setOppdatertGrunnlagRef(ReferanseDto oppdatertGrunnlagRef) {
        this.oppdatertGrunnlagRef = oppdatertGrunnlagRef;
    }

    public Grunnlag getGrunnlagType() {
        return grunnlagType;
    }

    public void setGrunnlagType(Grunnlag grunnlagType) {
        this.grunnlagType = grunnlagType;
    }

    public ReferanseDto getOpprinneligGrunnlagRef() {
        return opprinneligGrunnlagRef;
    }

    public void setOpprinneligGrunnlagRef(ReferanseDto opprinneligGrunnlagRef) {
        this.opprinneligGrunnlagRef = opprinneligGrunnlagRef;
    }

    public LocalDateTime getOpprettetTidspunkt() {
        return opprettetTidspunkt;
    }

    public void setOpprettetTidspunkt(LocalDateTime opprettetTidspunkt) {
        this.opprettetTidspunkt = opprettetTidspunkt;
    }
}
