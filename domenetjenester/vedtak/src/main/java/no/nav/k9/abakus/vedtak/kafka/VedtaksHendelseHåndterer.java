package no.nav.k9.abakus.vedtak.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import no.nav.foreldrepenger.konfig.KonfigVerdi;

import no.nav.vedtak.felles.integrasjon.kafka.KafkaMessageHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.k9.abakus.vedtak.LagreVedtakTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskDataBuilder;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ActivateRequestContext
@Transactional
public class VedtaksHendelseHåndterer implements KafkaMessageHandler.KafkaStringMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(VedtaksHendelseHåndterer.class);

    private static final String GROUP_ID = "k9-abakus"; // Hold konstant pga offset commit
    private String topicName;
    private ProsessTaskTjeneste taskTjeneste;

    public VedtaksHendelseHåndterer() {
        // CDI
    }

    @Inject
    public VedtaksHendelseHåndterer(@KonfigVerdi(value = "kafka.fattevedtak.topic", defaultVerdi = "teamforeldrepenger.familie-vedtakfattet-v1") String topicName,
                                    ProsessTaskTjeneste taskTjeneste) {
        this.topicName = topicName;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void handleRecord(String key, String value) {
        LOG.debug("Mottatt ytelse-vedtatt hendelse med key='{}', payload={}", key, value);
        var data = ProsessTaskDataBuilder.forProsessTask(LagreVedtakTask.class).medProperty(LagreVedtakTask.KEY, key).medPayload(value);
        taskTjeneste.lagre(data.build());
    }

    @Override
    public String topic() {
        return topicName;
    }

    @Override
    public String groupId() {
        return GROUP_ID;
    }
}
