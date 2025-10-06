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
@ProsessTask(value = "partition.cleanBucket", maxFailedRuns = 1)
public class CleanNextBucketBatchTask implements BatchProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CleanNextBucketBatchTask.class);
    private ProsessTaskTjeneste taskTjeneste;

    @Inject
    public CleanNextBucketBatchTask(ProsessTaskTjeneste taskTjeneste) {
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public CronExpression getCron() {
        return new CronExpression("0 0 7 1 * *");
    }

    @WithSpan("TASK partition.cleanBucket")
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        int antallSlettet = taskTjeneste.tømNestePartisjon();
        LOG.info("Tømmer neste partisjon med ferdige tasks, slettet {}", antallSlettet);
    }
}
