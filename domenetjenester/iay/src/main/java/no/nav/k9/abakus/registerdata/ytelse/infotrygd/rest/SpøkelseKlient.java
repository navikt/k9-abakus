package no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.InfotrygdSPGrunnlagRestKlient;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.spokelse.SykepengeVedtak;
import no.nav.k9.felles.exception.TekniskException;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@Dependent
@ScopedRestIntegration(scopeKey = "spokelse.grunnlag.scopes", defaultScope = "api://prod-gcp.tbd.spokelse/.default")
public class SpøkelseKlient {

    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdSPGrunnlagRestKlient.class);

    private SystemUserOidcRestClient oidcRestClient;
    private String url;

    @Inject
    public SpøkelseKlient(SystemUserOidcRestClient oidcRestClient,
                                         @KonfigVerdi(value = "spokelse.grunnlag.url", defaultVerdi = "api://prod-gcp.tbd.spokelse/.default") String url) {
        this.oidcRestClient = oidcRestClient;
        this.url = url;
    }

    public List<SykepengeVedtak> hentGrunnlag(String fnr) {
        return hentGrunnlag(fnr, null);
    }

    public List<SykepengeVedtak> hentGrunnlag(String fnr, LocalDate fom) {
        return hentGrunnlag(fnr, fom, Duration.ofSeconds(30));
    }

    public List<SykepengeVedtak> hentGrunnlag(String fnr, LocalDate fom, Duration timeout) {
        if (fnr == null || fnr.isEmpty()) {
            throw new IllegalArgumentException("Ikke angitt fnr");
        }
        try {
            var request = new GrunnlagRequest(fnr, fom);
            return Arrays.asList(oidcRestClient.post(URI.create(url), request, SykepengeVedtak[].class));
        } catch (Exception e) {
            throw new TekniskException("FP-180126",
                String.format("SPokelse %s gir feil, ta opp med team sykepenger.", url.toString()), e);
        }
    }

    public List<SykepengeVedtak> hentGrunnlagFailSoft(String fnr) {
        return hentGrunnlagFailSoft(fnr, null);
    }

    public List<SykepengeVedtak> hentGrunnlagFailSoft(String fnr, LocalDate fom) {
        try {
            return hentGrunnlag(fnr, fom);
        } catch (Exception e) {
            LOG.info("SPokelse felles: feil ved oppslag mot {}, returnerer ingen grunnlag", url, e);
            return List.of();
        }
    }

    public record GrunnlagRequest(String fodselsnummer, LocalDate fom) { }
}
