package no.nav.k9.abakus.web.jetty.abac;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;
import no.nav.k9.felles.sikkerhet.abac.Decision;
import no.nav.k9.felles.sikkerhet.abac.PdpKlient;
import no.nav.k9.felles.sikkerhet.abac.PdpRequest;
import no.nav.k9.felles.sikkerhet.abac.TilgangType;
import no.nav.k9.felles.sikkerhet.abac.Tilgangsbeslutning;
import no.nav.k9.felles.sikkerhet.pdp.PdpKlientImpl;
import no.nav.sif.abac.kontrakt.abac.dto.SaksinformasjonOgPersonerTilgangskontrollInputDto;

@Dependent
@Alternative
@Priority(1)
public class AppPdpKlient implements PdpKlient {

    private final static Logger LOGGER = LoggerFactory.getLogger(AppPdpKlient.class);

    private final PdpKlientImpl klientAbacK9;
    private final String konfigurasjon;
    private final SifAbacPdpK9RestKlient sifAbacPdpK9RestKlient;
    private final SifAbacPdpUngRestKlient sifAbacPdpUngRestKlient;

    @Inject
    public AppPdpKlient(SifAbacPdpK9RestKlient sifAbacPdpK9RestKlient,
                        SifAbacPdpUngRestKlient sifAbacPdpUngRestKlient,
                        PdpKlientImpl abacK9Klient,
                        @KonfigVerdi(value = "VALGT_PDP", required = false, defaultVerdi = "sif-abac-pdp") String konfigurasjon) {
        this.sifAbacPdpK9RestKlient = sifAbacPdpK9RestKlient;
        this.sifAbacPdpUngRestKlient = sifAbacPdpUngRestKlient;
        this.klientAbacK9 = abacK9Klient;
        this.konfigurasjon = konfigurasjon;
    }

    @Override
    public Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest) {
        return switch (konfigurasjon) {
            case "abac-k9" -> forespørTilgangGammel(pdpRequest);
            case "sif-abac-pdp" -> {
                no.nav.sif.abac.kontrakt.abac.resultat.Tilgangsbeslutning resultat = forespørTilgangNy(pdpRequest);
                yield  new Tilgangsbeslutning(resultat.harTilgang(), Set.of(), pdpRequest, TilgangType.INTERNBRUKER);
            }
            case "begge" -> {
                Tilgangsbeslutning resultat = forespørTilgangGammel(pdpRequest);
                try {
                    no.nav.sif.abac.kontrakt.abac.resultat.Tilgangsbeslutning resultatNy = forespørTilgangNy(pdpRequest);
                    if (resultatNy.harTilgang() != resultat.fikkTilgang()) {
                        LOGGER.warn("Ulikt resultat for ny/gammel tilgangskontroll. Ny {} årsaker {}, gammel {}",
                            resultatNy.harTilgang(),
                            resultatNy.årsakerForIkkeTilgang(),
                            resultat.fikkTilgang());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Ny tilgangskontroll feilet, bruker resultat fra gammel tilgangskontroll", e);
                }
                yield resultat;
            }
            default -> throw new IllegalStateException("Ugyldig konfigurasjon for VALGT_PDP: " + konfigurasjon);
        };
    }

    @WithSpan
    private Tilgangsbeslutning forespørTilgangGammel(PdpRequest pdpRequest) {
        return klientAbacK9.forespørTilgang(pdpRequest);
    }

    @WithSpan
    public no.nav.sif.abac.kontrakt.abac.resultat.Tilgangsbeslutning forespørTilgangNy(PdpRequest pdpRequest) {
        //TODO bør gjøre kall som k9/ung isdf å alltid kalle som k9 her. Det har ingenting å si i praksis p.t.
        //men dersom vi går over til å kalle med behandlingUuid/saksnummer må vi kalle med riktig domene
        //se k9-oppdrag for hvordan det gjøres der

        SaksinformasjonOgPersonerTilgangskontrollInputDto tilgangskontrollInput = PdpRequestMapper.map(pdpRequest);
        return sifAbacPdpK9RestKlient.sjekkTilgangForInnloggetBrukerK9(tilgangskontrollInput);
    }
}
