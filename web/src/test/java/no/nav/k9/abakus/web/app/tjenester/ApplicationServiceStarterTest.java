package no.nav.k9.abakus.web.app.tjenester;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.k9.felles.apptjeneste.AppServiceHandler;


@ExtendWith(MockitoExtension.class)
class ApplicationServiceStarterTest {

    private ApplicationServiceStarter serviceStarter;

    @Mock
    private AppServiceHandler service;

    @BeforeEach
    public void setup() {
        serviceStarter = new ApplicationServiceStarter(service);
    }

    @Test
    void test_skal_kalle_Controllable_start_og_stop() {
        serviceStarter.startServices();
        serviceStarter.stopServices();
        verify(service).start();
        verify(service).stop();
    }
}
