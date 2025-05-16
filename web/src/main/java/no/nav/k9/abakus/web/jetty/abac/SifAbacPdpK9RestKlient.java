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

import no.nav.sif.abac.kontrakt.abac.resultat.Tilgangsbeslutning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

@Dependent
@ScopedRestIntegration(scopeKey = "sif.abac.pdp.k9.scope", defaultScope = "api://prod-fss.k9saksbehandling.sif-abac-pdp/.default")
public class SifAbacPdpK9RestKlient {

    private static final Logger LOG = LoggerFactory.getLogger(SifAbacPdpK9RestKlient.class);

    private OidcRestClient restClient;
    private URI uriTilgangskontrollSaksinformasjonK9;

    SifAbacPdpK9RestKlient() {
        // for CDI proxy
    }

    @Inject
    public SifAbacPdpK9RestKlient(OidcRestClient restClient,
                                  @KonfigVerdi(value = "sif.abac.pdp.k9.url", defaultVerdi = "http://sif-abac-pdp:8913/sif/sif-abac-pdp/api/tilgangskontroll/v2/k9") String urlSifAbacPdpk9) {
        this.restClient = restClient;
        this.uriTilgangskontrollSaksinformasjonK9 = tilUri(urlSifAbacPdpk9, "saksinformasjon");
    }

    @WithSpan
    public Tilgangsbeslutning sjekkTilgangForInnloggetBrukerK9(SaksinformasjonOgPersonerTilgangskontrollInputDto input) {
        if (Environment.current().isDev()) {
            LOG.info("POST k9: {}", uriTilgangskontrollSaksinformasjonK9);
        }
        return restClient.post(uriTilgangskontrollSaksinformasjonK9, input, Tilgangsbeslutning.class);
    }

    private static URI tilUri(String baseUrl, String path) {
        try {
            return new URI(baseUrl + "/" + path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Ugyldig konfigurasjon for sif.abac.pdp.url", e);
        }
    }

}
