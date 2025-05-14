package no.nav.k9.abakus.web.app.tjenester;

import static java.util.concurrent.CompletableFuture.runAsync;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.hotspot.DefaultExports;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import no.nav.k9.felles.apptjeneste.AppServiceHandler;

/**
 * Initialiserer applikasjontjenester som implementer AppServiceHandler
 */
@ApplicationScoped
public class ApplicationServiceStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceStarter.class);
    private Set<AppServiceHandler> services;

    ApplicationServiceStarter() {
        // CDI
    }

    @Inject
    public ApplicationServiceStarter(@Any Instance<AppServiceHandler> services) {
        this(services.stream().collect(Collectors.toSet()));
    }

    ApplicationServiceStarter(AppServiceHandler service) {
        this(Set.of(service));
    }

    ApplicationServiceStarter(Set<AppServiceHandler> services) {
        this.services = services;
    }

    public void startServices() {
        // Prometheus
        DefaultExports.initialize();

        // Services
        LOGGER.info("Starter {} services", services.size());
        CompletableFuture.allOf(services.stream().map(service -> runAsync(service::start)).toArray(CompletableFuture[]::new)).join();
        LOGGER.info("Startet {} services", services.size());
    }

    public void stopServices() {
        LOGGER.info("Stopper {} services", services.size());
        CompletableFuture.allOf(services.stream().map(service -> runAsync(service::stop)).toArray(CompletableFuture[]::new))
            .orTimeout(31, TimeUnit.SECONDS)
            .join();
        LOGGER.info("Stoppet {} services", services.size());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [services=" + services.stream()
            .map(Object::getClass)
            .map(Class::getSimpleName)
            .collect(Collectors.joining(", ")) + "]";
    }
}
