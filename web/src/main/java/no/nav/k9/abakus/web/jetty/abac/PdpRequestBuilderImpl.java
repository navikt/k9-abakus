package no.nav.k9.abakus.web.jetty.abac;

import java.util.Set;

import no.nav.k9.abakus.felles.sikkerhet.AbakusAbacAttributtType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.k9.felles.sikkerhet.abac.AbacAttributtSamling;
import no.nav.k9.felles.sikkerhet.abac.PdpKlient;
import no.nav.k9.felles.sikkerhet.abac.PdpRequest;
import no.nav.k9.felles.sikkerhet.abac.PdpRequestBuilder;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.sif.abac.kontrakt.abac.AbacBehandlingStatus;
import no.nav.sif.abac.kontrakt.abac.AbacFagsakStatus;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@Dependent
@Alternative
@Priority(2)
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(PdpRequestBuilderImpl.class);
    private String abacDomain = "k9";

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(FellesAbacAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(FellesAbacAttributter.RESOURCE_FELLES_DOMENE, abacDomain);
        pdpRequest.put(FellesAbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());

        Set<String> ytelseTypeKoder = attributter.getVerdier(AbakusAbacAttributtType.YTELSETYPE);
        if (ytelseTypeKoder != null && !ytelseTypeKoder.isEmpty()) {
            pdpRequest.put(FellesAbacAttributter.YTELSE_TYPE, ytelseTypeKoder);
        }

        Set<String> aktørIder = attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID);
        Set<String> fødselsnumre = attributter.getVerdier(StandardAbacAttributtType.FNR);

        if (!aktørIder.isEmpty()) {
            pdpRequest.put(FellesAbacAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørIder);
        }
        if (!fødselsnumre.isEmpty()) {
            pdpRequest.put(FellesAbacAttributter.RESOURCE_FELLES_PERSON_FNR, fødselsnumre);
        }

        // TODO: Gå over til å hente fra pip-tjenesten når alle kall inkluderer behandlinguuid?
        pdpRequest.put(k9DataKeys.BEHANDLING_STATUS.getKey(), AbacBehandlingStatus.UTREDES.getEksternKode());
        pdpRequest.put(k9DataKeys.FAGSAK_STATUS.getKey(), AbacFagsakStatus.UNDER_BEHANDLING.getEksternKode());
        return pdpRequest;
    }

}
