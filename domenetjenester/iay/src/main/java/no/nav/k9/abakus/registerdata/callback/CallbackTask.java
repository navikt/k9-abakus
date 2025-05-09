package no.nav.k9.abakus.registerdata.callback;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("registerdata.callback")
public class CallbackTask implements ProsessTaskHandler {

    public static final String EKSISTERENDE_GRUNNLAG_REF = "grunnlag.ref.old";

    private static final Map<String, RestConfig> CALLBACK_MAP = new LinkedHashMap<>(2);

    private RestClient restClient;
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    CallbackTask() {
    }

    @Inject
    public CallbackTask(KoblingTjeneste koblingTjeneste, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.restClient = RestClient.client();
        this.koblingTjeneste = koblingTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    @WithSpan("TASK registerdata.callback")
    @Override
    public void doTask(ProsessTaskData data) {
        String callbackUrl = data.getPropertyValue(TaskConstants.CALLBACK_URL);
        String callbackScope = data.getPropertyValue(TaskConstants.CALLBACK_SCOPE);
        String nyKoblingId = data.getPropertyValue(TaskConstants.NY_KOBLING_ID);
        Long koblingId = nyKoblingId != null ? Long.valueOf(nyKoblingId) : data.getBehandlingIdAsLong();
        Kobling kobling = koblingTjeneste.hent(koblingId);

        CallbackDto callbackDto = new CallbackDto();
        callbackDto.setGrunnlagType(Grunnlag.IAY);

        setInformasjonOmAvsenderRef(kobling, callbackDto);
        setInformasjonOmEksisterendeGrunnlag(data, callbackDto);
        setInformasjonOmNyttGrunnlag(kobling, data, callbackDto);

        var restConfig = getRestConfigFor(callbackUrl, callbackScope);
        restClient.sendReturnOptional(RestRequest.newPOSTJson(callbackDto, restConfig.endpoint(), restConfig), String.class);
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

    private RestConfig getRestConfigFor(String url, String scope) {
        var key = url + scope;
        if (CALLBACK_MAP.get(key) == null) {
            try {
                var uri = new URI(url);
                var restConfig = new RestConfig(TokenFlow.ADAPTIVE, uri, scope, null);
                CALLBACK_MAP.put(key, restConfig);
            } catch (URISyntaxException e) {
                throw new TekniskException("FP-349977", String.format("Ugyldig callback url ved callback etter registerinnhenting: %s", url));
            }
        }
        return CALLBACK_MAP.get(key);
    }
}
