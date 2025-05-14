package no.nav.k9.abakus.registerdata.ytelse.arena;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.request.ArenaRequestDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.MeldekortUtbetalingsgrunnlagSakDto;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@ApplicationScoped
@ScopedRestIntegration(scopeKey = "fpwsproxy.scope", defaultScope = "api://prod-fss.teamforeldrepenger.fpwsproxy/.default")
public class FpwsproxyKlient {

    private static final Logger LOG = LoggerFactory.getLogger(FpwsproxyKlient.class);

    private SystemUserOidcRestClient oidcRestClient;
    private URI endpointHentDagpengerAAP;

    public FpwsproxyKlient() {
        //for CDI proxy
    }

    @Inject
    public FpwsproxyKlient(SystemUserOidcRestClient oidcRestClient,
                           @KonfigVerdi(value = "fpwsproxy.override.url", defaultVerdi = "http://fpwsproxy.teamforeldrepenger/fpwsproxy") String baseUrl) {
        this.oidcRestClient = oidcRestClient;
        this.endpointHentDagpengerAAP = UriBuilder.fromUri(baseUrl).path("/arena").build();
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentDagpengerAAP(PersonIdent ident, LocalDate fom, LocalDate tom) {
        try {
            LOG.info("Henter dagpenger/AAP for {} i periode fom {} tom {}", ident, fom, tom);
            var body = new ArenaRequestDto(ident.getIdent(), fom, tom);
            var result = oidcRestClient.post(endpointHentDagpengerAAP, body, MeldekortUtbetalingsgrunnlagSakDto[].class);
            LOG.info("Dagpenger/AAP hentet OK");
            return Arrays.stream(result).map(MedlemskortUtbetalingsgrunnlagSakMapper::tilDomeneModell).toList();
        } catch (UriBuilderException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for hentDagpengerAAP");
        }
    }

}
