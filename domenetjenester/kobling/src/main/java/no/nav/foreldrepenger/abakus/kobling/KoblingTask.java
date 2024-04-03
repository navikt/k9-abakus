package no.nav.k9.abakus.kobling;

import no.nav.k9.abakus.kobling.repository.LåsRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

public abstract class KoblingTask implements ProsessTaskHandler {

    private LåsRepository låsRepository;

    public KoblingTask() {
    }

    public KoblingTask(LåsRepository låsRepository) {
        this.låsRepository = låsRepository;
    }

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
