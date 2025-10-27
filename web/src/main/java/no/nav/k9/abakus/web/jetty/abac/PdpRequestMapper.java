package no.nav.k9.abakus.web.jetty.abac;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionType;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursResourceType;
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
        OperasjonDto operasjon = operasjon(pdpRequest);
        List<AktørId> mappetAktørId = pdpRequest.getAktørIder().stream().map(it -> new AktørId(it.aktørId())).toList();
        List<PersonIdent> mappetPersonIdent = pdpRequest.getFødselsnumre().stream().map(it -> new PersonIdent(it.fnr())).toList();
        //TODO saksinformasjon bør komme fra k9-sak/ung-sak og ikke hardkodes her
        SaksinformasjonDto saksinformasjon = new SaksinformasjonDto(null, AbacBehandlingStatus.UTREDES, AbacFagsakStatus.UNDER_BEHANDLING, Set.of());
        return new SaksinformasjonOgPersonerTilgangskontrollInputDto(mappetAktørId, mappetPersonIdent, operasjon, saksinformasjon);
    }

    public static OperasjonDto operasjon(PdpRequest pdpRequest) {
        return new OperasjonDto(
            resourceTypeFraKode(pdpRequest.getResourceType()),
            mapAction(pdpRequest.getActionType()),
            Set.of()); // Abakus har ikkje noko forhold til aksjonspunkttyper, så vi sender tom mengde
    }

    static ResourceType resourceTypeFraKode(BeskyttetRessursResourceType kode) {
        return switch (kode) {
            case APPLIKASJON -> ResourceType.APPLIKASJON;
            case FAGSAK -> ResourceType.FAGSAK;
            case DRIFT -> ResourceType.DRIFT;
            case VENTEFRIST -> ResourceType.VENTEFRIST;
            default -> throw new IllegalArgumentException("Ikke-støttet verdi: " + kode);
        };
    }

    static no.nav.sif.abac.kontrakt.abac.BeskyttetRessursActionAttributt mapAction(BeskyttetRessursActionType kode) {
        return switch (kode) {
            case READ -> BeskyttetRessursActionAttributt.READ;
            case UPDATE -> BeskyttetRessursActionAttributt.UPDATE;
            case CREATE -> BeskyttetRessursActionAttributt.CREATE;
            default -> throw new IllegalArgumentException("Ikke-styttet verdi: " + kode);
        };
    }

}

