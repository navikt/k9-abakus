package no.nav.k9.abakus.kobling;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import no.nav.k9.abakus.kobling.repository.LåsRepository;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskHandler;

public abstract class KoblingTask implements ProsessTaskHandler {

    private LåsRepository låsRepository;

    public KoblingTask() {
    }

    public KoblingTask(LåsRepository låsRepository) {
        this.låsRepository = låsRepository;
    }

    @WithSpan("TASK (koblingtask)")
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        String nyKoblingId = prosessTaskData.getPropertyValue(TaskConstants.NY_KOBLING_ID);
        Long koblingId = nyKoblingId != null ? Long.valueOf(nyKoblingId) : Long.valueOf(prosessTaskData.getBehandlingId());

        KoblingLås koblingLås = låsRepository.taLås(koblingId);

        prosesser(prosessTaskData);

        låsRepository.oppdaterLåsVersjon(koblingLås);
    }

    protected abstract void prosesser(ProsessTaskData prosessTaskData);
}
