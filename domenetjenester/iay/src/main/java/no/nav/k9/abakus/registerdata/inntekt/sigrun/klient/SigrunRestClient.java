package no.nav.k9.abakus.registerdata.inntekt.sigrun.klient;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Year;
import java.util.Optional;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.k9.felles.exception.HttpStatuskodeException;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@ApplicationScoped
@ScopedRestIntegration(scopeKey = "sigrunpgi.scope", defaultScope = "api://prod-fss.team-inntekt.sigrun/.default/.default")
public class SigrunRestClient {

    private static final String INNTEKTSAAR = "inntektsaar";
    private static final String RETTIGHETSPAKKE = "rettighetspakke";
    private static final String PLEIE_OG_OMSORGSPENGER = "navPleieOgOmsorgspenger";

    private static final Year FØRSTE_PGI = Year.of(2017);
    private static final Logger LOG = LoggerFactory.getLogger(SigrunRestClient.class);

    private SystemUserOidcRestClient oidcRestClient;
    private String url;

    SigrunRestClient() {
        //for CDI proxy
    }

    @Inject
    public SigrunRestClient(SystemUserOidcRestClient oidcRestClient,
                            @KonfigVerdi(value = "sigrunpgi.rs.url", defaultVerdi = "http://sigrun.team-inntekt/api/v1/pensjonsgivendeinntektforfolketrygden") String url) {
        this.oidcRestClient = oidcRestClient;
        this.url = url;
    }

    //api/v1/pensjonsgivendeinntektforfolketrygden
    public Optional<PgiFolketrygdenResponse> hentPensjonsgivendeInntektForFolketrygden(String fnr, Year år) {
        if (år.isBefore(FØRSTE_PGI)) {
            return Optional.empty();
        }

        Set<Header> headere = Set.of(
            new BasicHeader("Nav-Personident", fnr),
            new BasicHeader(RETTIGHETSPAKKE, PLEIE_OG_OMSORGSPENGER),
            new BasicHeader(INNTEKTSAAR, år.toString()));

        try {
            return oidcRestClient.getReturnsOptional(URI.create(url), headere, Set.of(), PgiFolketrygdenResponse.class);
        } catch (HttpStatuskodeException statuskodeException){
            if (statuskodeException.getHttpStatuskode() == HttpURLConnection.HTTP_NOT_FOUND) {
                // Håndtere konvensjon om 404 for tilfelle som ikke finnes hos SKE.
                return Optional.empty();
            }
            throw statuskodeException;
        }

    }


}
