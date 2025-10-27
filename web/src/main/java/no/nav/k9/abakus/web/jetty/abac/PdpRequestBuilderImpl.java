package no.nav.k9.abakus.web.jetty.abac;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import no.nav.k9.abakus.felles.sikkerhet.AbakusAbacAttributtType;
import no.nav.k9.felles.sikkerhet.abac.AbacAttributtSamling;
import no.nav.k9.felles.sikkerhet.abac.AktørId;
import no.nav.k9.felles.sikkerhet.abac.BerørtePersonerForAuditlogg;
import no.nav.k9.felles.sikkerhet.abac.Fnr;
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

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        AbakusPdpRequest pdpRequest = new AbakusPdpRequest();

        pdpRequest.setActionType(attributter.getActionType());
        pdpRequest.setResourceType(attributter.getResourceType());
        pdpRequest.setYtelseTypeKoder(attributter.getVerdier(AbakusAbacAttributtType.YTELSETYPE));

        Set<String> aktørIder = attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID);
        Set<String> fødselsnumre = attributter.getVerdier(StandardAbacAttributtType.FNR);
        pdpRequest.setAktørIderStr(aktørIder);
        pdpRequest.setFødselsnumreStr(fødselsnumre);


        Set<Fnr> fnrSett = fødselsnumre.stream().map(Fnr::new).collect(Collectors.toSet());
        Set<AktørId> aktørIdSett = aktørIder.stream().map(AktørId::new).collect(Collectors.toSet());
        pdpRequest.setBerørtePersonerForAuditlogg(new BerørtePersonerForAuditlogg(fnrSett, aktørIdSett));

        // TODO: Gå over til å hente fra pip-tjenesten når alle kall inkluderer behandlinguuid?
        pdpRequest.setBehandlingStatusEksternKode(AbacBehandlingStatus.UTREDES.getEksternKode());
        pdpRequest.setFagsakStatusEksternKode(AbacFagsakStatus.UNDER_BEHANDLING.getEksternKode());
        return pdpRequest;
    }

}
