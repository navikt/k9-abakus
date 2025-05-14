package no.nav.k9.abakus.web.jetty.abac;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.k9.felles.integrasjon.rest.OidcRestClient;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.konfigurasjon.env.Environment;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.k9.felles.sikkerhet.abac.Decision;
import no.nav.sif.abac.kontrakt.abac.dto.SaksinformasjonOgPersonerTilgangskontrollInputDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

@Dependent
@ScopedRestIntegration(scopeKey = "sif.abac.pdp.ung.scope", defaultScope = "api://prod-gcp.k9saksbehandling.sif-abac-pdp/.default")
public class SifAbacPdpUngRestKlient {

    private static final Logger LOG = LoggerFactory.getLogger(SifAbacPdpUngRestKlient.class);

    private OidcRestClient restClient;
    private URI uriTilgangskontrollSaksinformasjonUng;

    SifAbacPdpUngRestKlient() {
        // for CDI proxy
    }

    @Inject
    public SifAbacPdpUngRestKlient(OidcRestClient restClient,
                                   @KonfigVerdi(value = "sif.abac.pdp.ung.url", defaultVerdi = "http://sif-abac-pdp:8913/sif/sif-abac-pdp/api/tilgangskontroll/ung") String urlSifAbacPdpUng) {
        this.restClient = restClient;
        this.uriTilgangskontrollSaksinformasjonUng = tilUri(urlSifAbacPdpUng, "saksinformasjon");
    }

    @WithSpan
    public Decision sjekkTilgangForInnloggetBrukerUng(SaksinformasjonOgPersonerTilgangskontrollInputDto input) {
        if (Environment.current().isDev()) {
            LOG.info("POST ung: {}", uriTilgangskontrollSaksinformasjonUng);
        }
        return restClient.post(uriTilgangskontrollSaksinformasjonUng, input, Decision.class);
    }

    private static URI tilUri(String baseUrl, String path) {
        try {
            return new URI(baseUrl + "/" + path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Ugyldig konfigurasjon for sif.abac.pdp.url", e);
        }
    }

}
