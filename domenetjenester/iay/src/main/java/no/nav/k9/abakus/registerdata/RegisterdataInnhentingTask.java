package no.nav.k9.abakus.registerdata;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Set;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import no.nav.k9.prosesstask.api.ProsessTaskRepository;

import no.nav.k9.prosesstask.api.ProsessTaskStatus;

import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.kobling.Kobling;
import no.nav.k9.abakus.kobling.KoblingTask;
import no.nav.k9.abakus.kobling.KoblingTjeneste;
import no.nav.k9.abakus.kobling.TaskConstants;
import no.nav.k9.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.k9.abakus.kobling.repository.LåsRepository;
import no.nav.k9.abakus.registerdata.tjeneste.InnhentRegisterdataTjeneste;
import no.nav.k9.abakus.registerdata.tjeneste.RegisterdataElement;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask("registerdata.innhent")
public class RegisterdataInnhentingTask extends KoblingTask {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterdataInnhentingTask.class);
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private Instance<IAYRegisterInnhentingTjeneste> innhentTjenester;
    private ProsessTaskRepository prosessTaskRepository;
private ProsessTaskTjeneste prosessTaskTjeneste;
    RegisterdataInnhentingTask() {
    }

    @Inject
    public RegisterdataInnhentingTask(LåsRepository låsRepository,
                                      KoblingTjeneste koblingTjeneste,
                                      InntektArbeidYtelseTjeneste iayTjeneste,
                                      @Any Instance<IAYRegisterInnhentingTjeneste> innhentingTjeneste,
                                      ProsessTaskRepository prosessTaskRepository) {
        super(låsRepository);
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.innhentTjenester = innhentingTjeneste;
        this.prosessTaskRepository = prosessTaskRepository;
    }

    private IAYRegisterInnhentingTjeneste finnInnhenter(YtelseType ytelseType) {
        return YtelseTypeRef.Lookup.find(innhentTjenester, ytelseType).orElseThrow();
    }

    @WithSpan("TASK registerdata.innhent")
    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        String nyKoblingId = prosessTaskData.getPropertyValue(TaskConstants.NY_KOBLING_ID);
        Long koblingId = nyKoblingId != null ? Long.valueOf(nyKoblingId) : Long.valueOf(prosessTaskData.getBehandlingId());
        Kobling kobling = koblingTjeneste.hent(koblingId);
        LOG.info("Starter registerinnhenting for sak=[{}, {}] med behandling='{}'", kobling.getSaksnummer(), kobling.getYtelseType(),
            kobling.getKoblingReferanse());

        Set<RegisterdataElement> informasjonsElementer;
        var payloadAsString = prosessTaskData.getPayloadAsString();
        if (payloadAsString != null && !payloadAsString.isEmpty()) {
            try {
                var request = JsonObjectMapper.getMapper().readValue(payloadAsString, InnhentRegisterdataRequest.class);
                informasjonsElementer = InnhentRegisterdataTjeneste.hentUtInformasjonsElementer(request);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Feilet i deserialisering av innhent request", e);
            }
        } else {
            informasjonsElementer = Set.of(RegisterdataElement.values());
        }
        LOG.info("Registerdataelementer for sak=[{}, {}] med behandling='{}' er: {} ", kobling.getSaksnummer(), kobling.getYtelseType(),
            kobling.getKoblingReferanse(), informasjonsElementer);
        InntektArbeidYtelseGrunnlagBuilder builder = finnInnhenter(kobling.getYtelseType()).innhentRegisterdata(kobling, informasjonsElementer);
        iayTjeneste.lagre(kobling.getKoblingReferanse(), builder);

        //dersom vi har kommet hit, betyr det at registerdata kjørte uten exceptions
        //vi oppretter en RestartTaskerTask som vil trigge restart av eventuelt feilede registerdata-tasker
        //dette gjøres for at saker (i k9-sak, ung-sak) ikke skal måtte vente til periodisk rekjøring på faste tidspunkt
        prosessTaskTjeneste.lagre(ProsessTaskData.forProsessTask(RestartTaskerTask.class)
            .medGruppe("restart")
            .medSekvens(prosessTaskData.getId().toString())
        );
    }

}
