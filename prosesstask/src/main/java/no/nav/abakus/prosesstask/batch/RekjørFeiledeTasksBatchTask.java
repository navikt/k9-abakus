package no.nav.abakus.prosesstask.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.prosesstask.api.BatchProsessTaskHandler;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;
import no.nav.k9.prosesstask.impl.cron.CronExpression;

@ApplicationScoped
@ProsessTask(value = "retry.feilendeTasks", maxFailedRuns = 1)
public class RekjørFeiledeTasksBatchTask implements BatchProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RekjørFeiledeTasksBatchTask.class);
    private ProsessTaskTjeneste taskTjeneste;

    @Inject
    public RekjørFeiledeTasksBatchTask(ProsessTaskTjeneste taskTjeneste) {
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public CronExpression getCron() {
        return new CronExpression("0 30 6,8,10,12,14,16,18 * * *");
    }

    @WithSpan(value = "TASK retry.feilendeTasks")
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var rekjørAlleFeiledeTasks = taskTjeneste.restartAlleFeiledeTasks();
        LOG.info("Rekjører alle feilende tasks, oppdaterte {} tasks", rekjørAlleFeiledeTasks);
    }
}
