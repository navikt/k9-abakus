package no.nav.foreldrepenger.abakus.vedtak.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import no.nav.foreldrepenger.konfig.KonfigVerdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.vedtak.LagreVedtakTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskDataBuilder;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ActivateRequestContext
@Transactional
public class VedtaksHendelseHåndterer {

    private static final Logger LOG = LoggerFactory.getLogger(VedtaksHendelseHåndterer.class);
    private ProsessTaskTjeneste taskTjeneste;
    private boolean lagreVedtak;

    public VedtaksHendelseHåndterer() {
        // CDI
    }

    @Inject
    public VedtaksHendelseHåndterer(@KonfigVerdi(value = "kafka.lagre.vedtak", defaultVerdi = "true") Boolean lagreVedtak,
                                    ProsessTaskTjeneste taskTjeneste) {
        this.taskTjeneste = taskTjeneste;
        this.lagreVedtak = lagreVedtak;
    }

    void handleMessage(String key, String payload) {
        LOG.debug("Mottatt ytelse-vedtatt hendelse med key='{}', payload={}", key, payload);
        if (lagreVedtak) {
            var data = ProsessTaskDataBuilder.forProsessTask(LagreVedtakTask.class).medProperty(LagreVedtakTask.KEY, key).medPayload(payload);
            taskTjeneste.lagre(data.build());
        } else {
            LOG.info("Lagring av vedtak er slått av. Husk å slå det på igjen samtidig med k9-abakus flyttes til egen database.");
        }
    }
}
