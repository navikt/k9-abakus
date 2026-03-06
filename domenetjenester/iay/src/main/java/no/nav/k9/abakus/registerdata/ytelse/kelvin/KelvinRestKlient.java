package no.nav.k9.abakus.registerdata.ytelse.kelvin;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import graphql.com.google.common.collect.ImmutableList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@ApplicationScoped
@ScopedRestIntegration(scopeKey = "kelvin.scope", defaultScope = "api://prod-gcp.aap.api-intern/.default")
public class KelvinRestKlient {


    private URI endpointHentAAP;
    private SystemUserOidcRestClient oidcRestClient;

    public KelvinRestKlient() {
        //for CDI proxy
    }

    @Inject
    public KelvinRestKlient(SystemUserOidcRestClient oidcRestClient,
                            @KonfigVerdi(value = "kelvin.url", defaultVerdi = "https://aap-api.intern.nav.no") String baseUrl) {
        this.oidcRestClient = oidcRestClient;
        this.endpointHentAAP = UriBuilder.fromUri(baseUrl).path("/maksimum").build();
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentAAP(PersonIdent ident, LocalDate fom, LocalDate tom, Saksnummer saksnummer) {
        var body = new KelvinRequest(ident.getIdent(), fom, tom);
        var result = oidcRestClient.post(endpointHentAAP, body, ArbeidsavklaringspengerResponse.class);

        var kelvinVedtak = result.vedtak().stream().filter(v -> ArbeidsavklaringspengerResponse.Kildesystem.KELVIN.equals(v.kildesystem())).toList();
        var arenaVedtak = result.vedtak().stream().filter(v -> ArbeidsavklaringspengerResponse.Kildesystem.ARENA.equals(v.kildesystem())).toList();
        var kelvinMapped = KelvinMapper.mapTilMeldekortAclKelvin(kelvinVedtak, saksnummer);
        var arenaMapped = ArenaMapper.mapTilMeldekortAclArena(arenaVedtak, fom);
        return ImmutableList.<MeldekortUtbetalingsgrunnlagSak>builder()
            .addAll(kelvinMapped)
            .addAll(arenaMapped)
            .build();
    }

    public record KelvinRequest(String personidentifikator, LocalDate fraOgMedDato, LocalDate tilOgMedDato) { }


}
