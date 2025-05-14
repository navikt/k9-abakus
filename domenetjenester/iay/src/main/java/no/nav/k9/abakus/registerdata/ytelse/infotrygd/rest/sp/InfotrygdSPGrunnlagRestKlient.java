package no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import no.nav.k9.abakus.registerdata.ytelse.infotrygd.InfotrygdGrunnlag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.request.GrunnlagRequest;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.respons.Grunnlag;
import no.nav.k9.felles.exception.TekniskException;
import no.nav.k9.felles.integrasjon.rest.ScopedRestIntegration;
import no.nav.k9.felles.integrasjon.rest.SystemUserOidcRestClient;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@SP
@Dependent
@ScopedRestIntegration(scopeKey = "fpabakus.it.sp.scope", defaultScope = "api://prod-fss.teamforeldrepenger.fp-infotrygd-sykepenger/.default")
public class InfotrygdSPGrunnlagRestKlient implements InfotrygdGrunnlag {

    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdSPGrunnlagRestKlient.class);

    private SystemUserOidcRestClient oidcRestClient;
    private final String url;

    @Inject
    public InfotrygdSPGrunnlagRestKlient(SystemUserOidcRestClient oidcRestClient,
                                         @KonfigVerdi(value = "fpabakus.it.sp.grunnlag.url", defaultVerdi = "http://fp-infotrygd-sykepenger.teamforeldrepenger/grunnlag") String url) {
        this.oidcRestClient = oidcRestClient;
        this.url = url;
    }

    public List<Grunnlag> hentGrunnlag(GrunnlagRequest request) {
        try {
            return Arrays.asList(oidcRestClient.post(URI.create(url), request, Grunnlag[].class));

        } catch (Exception e) {
            throw new TekniskException("FP-180125",
                String.format("Tjeneste %s gir feil, meld til #infotrygd_replikering hvis dette skjer gjennom lengre tidsperiode.",
                    url), e);
        }
    }

    public List<Grunnlag> hentGrunnlagFailSoft(GrunnlagRequest request) {
        try {
            return hentGrunnlag(request);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", url, e);
            return Collections.emptyList();
        }
    }
}
