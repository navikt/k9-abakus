package no.nav.k9.abakus.vedtak.kafka;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.abakus.felles.kafka.KafkaConsumerManager;
import no.nav.k9.abakus.felles.server.LiveAndReadinessAware;
import no.nav.k9.felles.apptjeneste.AppServiceHandler;


@ApplicationScoped
public class VedtakConsumer implements LiveAndReadinessAware, AppServiceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(VedtakConsumer.class);

    private KafkaConsumerManager<String, String> kcm;

    VedtakConsumer() {
    }

    @Inject
    public VedtakConsumer(VedtaksHendelseHåndterer vedtaksHendelseHåndterer) {
        this.kcm = new KafkaConsumerManager<>(List.of(vedtaksHendelseHåndterer));
    }

    @Override
    public boolean isAlive() {
        return kcm.allRunning();
    }

    @Override
    public boolean isReady() {
        return isAlive();
    }

    @Override
    public void start() {
        LOG.info("Starter konsumering av topics={}", kcm.topicNames());
        kcm.start((t, e) -> LOG.error("{} :: Caught exception in stream, exiting", t, e));
    }

    @Override
    public void stop() {
        LOG.info("Starter shutdown av topics={} med 10 sekunder timeout", kcm.topicNames());
        kcm.stop();
    }
}
