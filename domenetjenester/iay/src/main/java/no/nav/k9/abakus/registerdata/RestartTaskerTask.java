package no.nav.k9.abakus.registerdata;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskHandler;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask("restart.feilede.tasker.etter.nedetid")
public class RestartTaskerTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestartTaskerTask.class);

    private static LocalDateTime sistRestartetTaskerTidspunkt = LocalDateTime.now();
    private static final Duration HYPPIGSTE_RESTARTING = Duration.ofHours(1);

    private ProsessTaskTjeneste prosessTaskTjeneste;

    RestartTaskerTask() {
        //for CDI proxy
    }

    @Inject
    RestartTaskerTask(ProsessTaskTjeneste prosessTaskTjeneste) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        if (sistRestartetTaskerTidspunkt.plus(HYPPIGSTE_RESTARTING).isBefore(LocalDateTime.now())) {
            //fail-safe for å ikke trigge kontinuerlig restart av tasker
            LOG.info("Gjør ingenting siden det ikke er gått lenge nok siden forrige restarting av tasker");
            return;
        }

        int antallRestartede = prosessTaskTjeneste.restartAlleFeiledeTasks("registerdata.innhent");
        if (antallRestartede > 0) {
            LOG.info("Restartet {} registerdata.innhent-tasker. Denne noden vil ikke restarte tasker før om {}", antallRestartede, HYPPIGSTE_RESTARTING);
            sistRestartetTaskerTidspunkt = LocalDateTime.now();
        } else {
            LOG.info("Ingen registerdata.innhent-tasker å restarte");
        }
    }
}
