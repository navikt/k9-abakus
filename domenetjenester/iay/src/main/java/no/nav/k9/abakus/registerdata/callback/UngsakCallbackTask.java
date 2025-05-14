package no.nav.k9.abakus.registerdata.callback;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.callback.registerdata.CallbackDto;
import no.nav.abakus.callback.registerdata.Grunnlag;
import no.nav.abakus.callback.registerdata.ReferanseDto;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.kobling.Kobling;
import no.nav.k9.abakus.kobling.KoblingTjeneste;
import no.nav.k9.abakus.kobling.TaskConstants;
import no.nav.k9.felles.integrasjon.rest.OidcRestClient;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskHandler;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@ProsessTask("registerdata.ungsak.callback")
@ScopedRestIntegration(scopeKey = "ungsak.scope", defaultScope = "api://prod-fss.ung-sak.k9saksbehandling/.default")
public class UngsakCallbackTask implements ProsessTaskHandler {

    public static final String EKSISTERENDE_GRUNNLAG_REF = "grunnlag.ref.old";

    private OidcRestClient restClient;
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    UngsakCallbackTask() {
    }

    @Inject
    public UngsakCallbackTask(OidcRestClient restClient, KoblingTjeneste koblingTjeneste, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.restClient = restClient;
        this.koblingTjeneste = koblingTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    @WithSpan("TASK registerdata.callback")
    @Override
    public void doTask(ProsessTaskData data) {
        String callbackUrl = data.getPropertyValue(TaskConstants.CALLBACK_URL);

        String nyKoblingId = data.getPropertyValue(TaskConstants.NY_KOBLING_ID);
        Long koblingId = nyKoblingId != null ? Long.valueOf(nyKoblingId) : Long.valueOf(data.getBehandlingId());
        Kobling kobling = koblingTjeneste.hent(koblingId);

        CallbackDto callbackDto = new CallbackDto();
        callbackDto.setGrunnlagType(Grunnlag.IAY);

        setInformasjonOmAvsenderRef(kobling, callbackDto);
        setInformasjonOmEksisterendeGrunnlag(data, callbackDto);
        setInformasjonOmNyttGrunnlag(kobling, data, callbackDto);

        restClient.postReturnsOptional(URI.create(callbackUrl), callbackDto, String.class);
    }

    private void setInformasjonOmAvsenderRef(Kobling kobling, CallbackDto callbackDto) {
        UUID koblingReferanse = kobling.getKoblingReferanse().getReferanse();
        ReferanseDto avsenderRef = new ReferanseDto();
        avsenderRef.setReferanse(koblingReferanse);
        callbackDto.setAvsenderRef(avsenderRef);
    }

    private void setInformasjonOmEksisterendeGrunnlag(ProsessTaskData data, CallbackDto callbackDto) {
        String eksisterendeGrunnlagRef = data.getPropertyValue(EKSISTERENDE_GRUNNLAG_REF);
        if (eksisterendeGrunnlagRef != null && !eksisterendeGrunnlagRef.isEmpty()) {
            ReferanseDto eksisterendeRef = new ReferanseDto();
            eksisterendeRef.setReferanse(UUID.fromString(eksisterendeGrunnlagRef));
            callbackDto.setOpprinneligGrunnlagRef(eksisterendeRef);
        }
    }

    private void setInformasjonOmNyttGrunnlag(Kobling kobling, ProsessTaskData data, CallbackDto callbackDto) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse());
        grunnlag.ifPresent(gr -> {
            ReferanseDto grunnlagRef = new ReferanseDto();
            grunnlagRef.setReferanse(gr.getGrunnlagReferanse().getReferanse());
            callbackDto.setOppdatertGrunnlagRef(grunnlagRef);
            callbackDto.setOpprettetTidspunkt(gr.getOpprettetTidspunkt());
        });
        if (grunnlag.isEmpty()) {
            callbackDto.setOpprettetTidspunkt(data.getSistKjørt());
        }
    }
}
