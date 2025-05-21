package no.nav.k9.abakus.web.jetty.abac;

import java.util.List;
import java.util.Set;

import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.k9.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt;
import no.nav.k9.felles.sikkerhet.abac.PdpRequest;
import no.nav.sif.abac.kontrakt.abac.AbacBehandlingStatus;
import no.nav.sif.abac.kontrakt.abac.AbacFagsakStatus;
import no.nav.sif.abac.kontrakt.abac.BeskyttetRessursActionAttributt;
import no.nav.sif.abac.kontrakt.abac.ResourceType;
import no.nav.sif.abac.kontrakt.abac.dto.OperasjonDto;
import no.nav.sif.abac.kontrakt.abac.dto.SaksinformasjonDto;
import no.nav.sif.abac.kontrakt.abac.dto.SaksinformasjonOgPersonerTilgangskontrollInputDto;
import no.nav.sif.abac.kontrakt.person.AktørId;
import no.nav.sif.abac.kontrakt.person.PersonIdent;

public class PdpRequestMapper {

    public static SaksinformasjonOgPersonerTilgangskontrollInputDto map(PdpRequest pdpRequest) {
        Set<String> aktørIder = (Set<String>) pdpRequest.get(FellesAbacAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE);
        Set<String> fødselsnumre = (Set<String>) pdpRequest.get(FellesAbacAttributter.RESOURCE_FELLES_PERSON_FNR);
        OperasjonDto operasjon = operasjon(pdpRequest);
        List<AktørId> mappetAktørId = aktørIder != null
            ? aktørIder.stream().map(AktørId::new).toList()
            : List.of();
        List<PersonIdent> mappetPersonIdent = fødselsnumre != null
            ? fødselsnumre.stream().map(PersonIdent::new).toList()
            : List.of();
        //TODO saksinformasjon bør komme fra k9-sak/ung-sak og ikke hardkodes her
        SaksinformasjonDto saksinformasjon = new SaksinformasjonDto(null, AbacBehandlingStatus.UTREDES, AbacFagsakStatus.UNDER_BEHANDLING, Set.of());
        return new SaksinformasjonOgPersonerTilgangskontrollInputDto(mappetAktørId, mappetPersonIdent, operasjon, saksinformasjon);
    }

    public static OperasjonDto operasjon(PdpRequest pdpRequest) {
        ResourceType resource = resourceTypeFraKode(pdpRequest.getString(AbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE));
        return new OperasjonDto(resource, actionFraKode(pdpRequest.getString(AbacAttributter.XACML_1_0_ACTION_ACTION_ID)));
    }

    static ResourceType resourceTypeFraKode(String kode) {
        return switch (kode) {
            case AbakusBeskyttetRessursAttributt.APPLIKASJON -> ResourceType.APPLIKASJON;
            case AbakusBeskyttetRessursAttributt.FAGSAK -> ResourceType.FAGSAK;
            case AbakusBeskyttetRessursAttributt.DRIFT -> ResourceType.DRIFT;
            default -> throw new IllegalArgumentException("Ikke-støttet verdi: " + kode);
        };
    }

    static BeskyttetRessursActionAttributt actionFraKode(String kode) {
        return switch (kode) {
            case "read" -> BeskyttetRessursActionAttributt.READ;
            case "update" -> BeskyttetRessursActionAttributt.UPDATE;
            case "create" -> BeskyttetRessursActionAttributt.CREATE;
            default -> throw new IllegalArgumentException("Ikke-styttet verdi: " + kode);
        };
    }

}

