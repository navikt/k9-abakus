package no.nav.k9.abakus.registerdata.ytelse.dagpenger;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@ApplicationScoped
@ScopedRestIntegration(scopeKey = "dagpenger.scope", defaultScope = "api://prod-gcp.teamdagpenger.dp-datadeling/.default/.default")
public class DpSakRestKlient {

    private  URI perioderEndpoint;

    private  SystemUserOidcRestClient restClient;

    @Inject
    public DpSakRestKlient(SystemUserOidcRestClient restClient, @KonfigVerdi(value = "dagpengerdatadeling.base.url") URI dagpengerUrl) {
        perioderEndpoint = UriBuilder.fromUri(dagpengerUrl).path("/dagpenger/datadeling/v1/perioder").build();
        this.restClient = restClient;
    }

    public DpSakRestKlient() {}


    public List<MeldekortUtbetalingsgrunnlagSak> hentRettighetsperioder(PersonIdent personIdent, LocalDate fom, LocalDate tom) {
        var prequest = new PersonRequest(personIdent.getIdent(), fom, tom);
        try {
            var result = restClient.post(perioderEndpoint, prequest, DagpengerRettighetsperioder.class);
            return result.perioder().stream().map(DagpengerRettighetsperioder.Rettighetsperiode::tilDomeneModell).toList();
        } catch (UriBuilderException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for hentDagpenger fra dp-sak");
        }
    }

    public record PersonRequest(String personIdent, LocalDate fraOgMedDato, LocalDate tilOgMedDato) {
    }


}
