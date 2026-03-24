package no.nav.k9.abakus.registerdata.ytelse.dpsak;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// https://navikt.github.io/dp-datadeling/openapi.html
@ApplicationScoped
@ScopedRestIntegration(scopeKey = "dagpenger.scope", defaultScope = "api://prod-gcp.teamdagpenger.dp-datadeling/.default/.default")
public class DpsakKlient {

    private static final Logger LOG = LoggerFactory.getLogger(DpsakKlient.class);

    private URI beregningerEndpoint;
    private URI rettighetsperioderEndpoint;
    private SystemUserOidcRestClient restClient;

    @Inject
    public DpsakKlient(SystemUserOidcRestClient restClient, @KonfigVerdi(value = "dagpengerdatadeling.base.url") URI dagpengerUrl) {
        beregningerEndpoint = UriBuilder.fromUri(dagpengerUrl).path("/dagpenger/datadeling/v1/beregninger").build();
        rettighetsperioderEndpoint = UriBuilder.fromUri(dagpengerUrl).path("/dagpenger/datadeling/v1/perioder").build();
        this.restClient = restClient;
    }

    public DpsakKlient() {
    }


    public record PersonRequest(String personIdent, LocalDate fraOgMedDato, LocalDate tilOgMedDato) {
    }


    public Map<Fagsystem, List<DpsakVedtak>> hentDagpenger(PersonIdent personIdent, LocalDate fom, LocalDate tom, Saksnummer sak,
                                                           int antallArenaVedtak, int antallArenaMeldekort) {
        try {
            var perioder = hentRettighetsperioder(personIdent, fom, tom);
            var utbetalinger = hentUtbetalinger(personIdent, fom, tom);
            var perioderArena = perioder.stream()
                .filter(p -> DagpengerRettighetsperioderDto.DagpengerKilde.ARENA.equals(p.kilde()))
                .toList();
            var perioderDpsak = perioder.stream()
                .filter(p -> DagpengerRettighetsperioderDto.DagpengerKilde.DP_SAK.equals(p.kilde()))
                .toList();
            var utbetalingerDpsak = utbetalinger.stream()
                .filter(u -> DagpengerKilde.DP_SAK.equals(u.kilde()))
                .toList();
            var utbetalingerArena = utbetalinger.stream()
                .filter(u -> DagpengerKilde.ARENA.equals(u.kilde()))
                .toList();
            if (!perioderArena.isEmpty() || !utbetalingerArena.isEmpty()) {
                LOG.info("DP-DATADELING ARENA fant {} perioder og {} utbetalinger mot {} vedtak og {} MK fra Arena",
                    perioderArena.size(), utbetalingerArena.size(), antallArenaVedtak, antallArenaMeldekort);
            }
            if (!perioderDpsak.isEmpty() || !utbetalingerDpsak.isEmpty()) {
                LOG.info("DP-DATADELING DPSAK fant {} perioder og {} utbetalinger. Perioder: {}. Utbetalinger {}",
                    perioderDpsak.size(), utbetalingerDpsak.size(), perioderDpsak, utbetalingerDpsak);
                LOG.warn("Merk Dem! Sak {} har nye dagpenger. Kontakt produkteier umiddelbart", sak.getVerdi());
            }
            var dpsakVedtak = DpsakMapper.fullMapping(perioderDpsak, utbetalingerDpsak);
            return Map.of(Fagsystem.DPSAK, dpsakVedtak);
        } catch (Exception e) {
            LOG.info("DP-DATADELING feil ", e);
            return Map.of();
        }
    }

    public List<DagpengerRettighetsperioderDto.Rettighetsperiode> hentRettighetsperioder(PersonIdent personIdent, LocalDate fom, LocalDate tom) {
        var prequest = new PersonRequest(personIdent.getIdent(), fom, tom);
        try {
            var result = restClient.post(rettighetsperioderEndpoint, prequest, DagpengerRettighetsperioderDto.class);
            return result.perioder();
        } catch (UriBuilderException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for hentDagpenger fra dp-sak");
        }
    }

    public List<DagpengerUtbetalingDto> hentUtbetalinger(PersonIdent personIdent, LocalDate fom, LocalDate tom) {
        var prequest = new PersonRequest(personIdent.getIdent(), fom, tom);
        try {
            var result = restClient.post(beregningerEndpoint, prequest, DagpengerUtbetalingDto[].class);
            return Arrays.stream(result).toList();
        } catch (UriBuilderException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for hentDagpenger fra dp-sak");
        }
    }

}
