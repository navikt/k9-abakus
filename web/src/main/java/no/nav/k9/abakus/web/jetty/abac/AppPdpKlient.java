package no.nav.k9.abakus.web.jetty.abac;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.felles.konfigurasjon.env.Environment;
import no.nav.k9.felles.sikkerhet.abac.PdpKlient;
import no.nav.k9.felles.sikkerhet.abac.PdpRequest;
import no.nav.k9.felles.sikkerhet.abac.TilgangType;
import no.nav.k9.felles.sikkerhet.abac.Tilgangsbeslutning;
import no.nav.sif.abac.kontrakt.abac.dto.SaksinformasjonOgPersonerTilgangskontrollInputDto;

@Dependent
@Alternative
@Priority(1)
public class AppPdpKlient implements PdpKlient {

    private final static Logger LOGGER = LoggerFactory.getLogger(AppPdpKlient.class);

    private final SifAbacPdpK9RestKlient sifAbacPdpK9RestKlient;
    private final SifAbacPdpUngRestKlient sifAbacPdpUngRestKlient;

    @Inject
    public AppPdpKlient(SifAbacPdpK9RestKlient sifAbacPdpK9RestKlient, SifAbacPdpUngRestKlient sifAbacPdpUngRestKlient) {
        this.sifAbacPdpK9RestKlient = sifAbacPdpK9RestKlient;
        this.sifAbacPdpUngRestKlient = sifAbacPdpUngRestKlient;
    }

    @Override
    public Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest) {
        no.nav.sif.abac.kontrakt.abac.resultat.Tilgangsbeslutning resultat = forespørTilgangNy(pdpRequest);
        return new Tilgangsbeslutning(resultat.harTilgang(), Set.of(), pdpRequest, TilgangType.INTERNBRUKER);
    }

    @WithSpan
    public no.nav.sif.abac.kontrakt.abac.resultat.Tilgangsbeslutning forespørTilgangNy(PdpRequest pdpRequest) {
        List<String> aktuelleYtelser = pdpRequest.getListOfString(FellesAbacAttributter.YTELSE_TYPE);

        sjekkYtelsetyper(aktuelleYtelser);

        if (aktuelleYtelser.size() == 1 && aktuelleYtelser.contains(YtelseType.UNGDOMSYTELSE.getKode())) {
            LOGGER.info("Aktuell ytelse er ungdomsprogramytelse");
            SaksinformasjonOgPersonerTilgangskontrollInputDto tilgangskontrollInput = PdpRequestMapper.map(pdpRequest);
            return sifAbacPdpUngRestKlient.sjekkTilgangForInnloggetBrukerUng(tilgangskontrollInput);
        } else {
            if (Environment.current().isDev() || Environment.current().isLocal()) {
                LOGGER.info("Aktuelle ytelser er {}", aktuelleYtelser);
            }
            SaksinformasjonOgPersonerTilgangskontrollInputDto tilgangskontrollInput = PdpRequestMapper.map(pdpRequest);
            return sifAbacPdpK9RestKlient.sjekkTilgangForInnloggetBrukerK9(tilgangskontrollInput);
        }
    }

    private void sjekkYtelsetyper(List<String> aktuelleYtelser) {
        //funksjonen finnes hovedsaklig for å oppdage evt. endepunkt som burde ha ytelsetype som del av input
        if (aktuelleYtelser.isEmpty()) {
            LOGGER.warn("Ytelsetype er ikke satt i abac-attributter, defaulter til å bruke K9 som domene");
        } else if (aktuelleYtelser.size() > 1) {
            LOGGER.warn("Flere ytelsestyper {} er satt i abac-attributter, defaulter til å bruke K9 som domene", aktuelleYtelser);
        }
        List<String> ugyldigeYtelser = aktuelleYtelser.stream()
            .filter(input -> Arrays.stream(YtelseType.values()).noneMatch(it -> it.getKode().equals(input)))
            .toList();
        if (!ugyldigeYtelser.isEmpty()) {
            LOGGER.warn("Ukjente ytelsestyper {}, defaulter til å bruke K9 som domene", ugyldigeYtelser);
        }
    }
}
