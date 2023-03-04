package no.nav.foreldrepenger.abakus.app.selftest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.abakus.app.konfig.ApplicationServiceStarter;
import no.nav.foreldrepenger.abakus.app.selftest.checks.DatabaseHealthCheck;

@Path("/")
@Produces(TEXT_PLAIN)
@RequestScoped
public class HealthCheckRestService {

    private static final String RESPONSE_CACHE_KEY = "Cache-Control";
    private static final String RESPONSE_CACHE_VAL = "must-revalidate,no-cache,no-store";
    private static final String RESPONSE_OK = "OK";

    private ApplicationServiceStarter starterService;
    private DatabaseHealthCheck databaseHealthCheck;

    public HealthCheckRestService() {
        // CDI
    }

    @Inject
    public HealthCheckRestService(ApplicationServiceStarter starterService, DatabaseHealthCheck databaseHealthCheck) {
        this.starterService = starterService;
        this.databaseHealthCheck = databaseHealthCheck;
    }

    @GET
    @Path("/health/isAlive")
    @Operation(description = "sjekker om poden lever", tags = "nais", hidden = true)
    public Response isAlive() {
        if (starterService.isKafkaAlive()) {
            return Response.ok(RESPONSE_OK).header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL).build();
        } else {
            return Response.serverError().header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL).build();
        }
    }

    /**
     * @deprecated Både k9-verdikjede og fpsak-autotest trenger å bytte til det nye endepunkt /internal/health/isAlive
     */
    @Deprecated(since = "03.2023", forRemoval = true)
    @GET
    @Path("/isAlive")
    @Operation(description = "sjekker om poden lever", tags = "nais", hidden = true)
    public Response isAliveDeprecated() {
        return isAlive();
    }

    @GET
    @Path("/health/isReady")
    @Operation(description = "sjekker om poden er klar", tags = "nais", hidden = true)
    public Response isReady() {
        if (databaseHealthCheck.isReady()) {
            return Response.ok(RESPONSE_OK).header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL).build();
        }
    }

    /**
     * @deprecated Både k9-verdikjede og fpsak-autotest trenger å bytte til det nye endepunkt /internal/health/isReady
     */
    @Deprecated(since = "03.2023", forRemoval = true)
    @GET
    @Path("/isReady")
    @Operation(description = "sjekker om poden er klar", tags = "nais", hidden = true)
    public Response isReadyDeprecated() {
        return isReady();
    }

    @GET
    @Path("/health/preStop")
    @Operation(description = "kalles på før stopp", tags = "nais", hidden = true)
    public Response preStop() {
        starterService.stopServices();
        return Response.ok(RESPONSE_OK).build();
    }

}
