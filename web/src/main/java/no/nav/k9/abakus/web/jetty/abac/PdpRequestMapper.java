package no.nav.k9.abakus.web.jetty.abac;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionType;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursResourceType;
import no.nav.k9.felles.sikkerhet.abac.PdpRequest;
import no.nav.sif.abac.kontrakt.abac.AbacBehandlingStatus;
import no.nav.sif.abac.kontrakt.abac.AbacFagsakStatus;
import no.nav.sif.abac.kontrakt.abac.AbacFagsakYtelseType;
import no.nav.sif.abac.kontrakt.abac.BeskyttetRessursActionAttributt;
import no.nav.sif.abac.kontrakt.abac.ResourceType;
import no.nav.sif.abac.kontrakt.abac.dto.OperasjonDto;
import no.nav.sif.abac.kontrakt.abac.dto.SaksinformasjonDto;
import no.nav.sif.abac.kontrakt.abac.dto.SaksinformasjonOgPersonerTilgangskontrollInputDto;
import no.nav.sif.abac.kontrakt.person.AktørId;
import no.nav.sif.abac.kontrakt.person.PersonIdent;

public class PdpRequestMapper {

    private static final Logger logger = LoggerFactory.getLogger(PdpRequestMapper.class);

    public static SaksinformasjonOgPersonerTilgangskontrollInputDto map(AbakusPdpRequest pdpRequest) {
        OperasjonDto operasjon = operasjon(pdpRequest);
        List<AktørId> mappetAktørId = pdpRequest.getAktørIder().stream().map(it -> new AktørId(it.aktørId())).toList();
        List<PersonIdent> mappetPersonIdent = pdpRequest.getFødselsnumre().stream().map(it -> new PersonIdent(it.fnr())).toList();

        AbacFagsakYtelseType ytelseType = map(pdpRequest.getYtelseTyper());

        //TODO saksinformasjon bør komme fra k9-sak/ung-sak og ikke hardkodes her
        SaksinformasjonDto saksinformasjon = new SaksinformasjonDto(null, AbacBehandlingStatus.UTREDES, AbacFagsakStatus.UNDER_BEHANDLING, ytelseType);
        return new SaksinformasjonOgPersonerTilgangskontrollInputDto(mappetAktørId, mappetPersonIdent, operasjon, saksinformasjon);
    }

    private static AbacFagsakYtelseType map(Set<YtelseType> ytelseTyper) {
        if (ytelseTyper.size() == 1) {
            YtelseType ytelseType = ytelseTyper.iterator().next();
            return switch (ytelseType) {
                case AKTIVITETSPENGER -> AbacFagsakYtelseType.AKTIVITETSPENGER;
                case FRISINN -> AbacFagsakYtelseType.FRISINN;
                case PLEIEPENGER_SYKT_BARN -> AbacFagsakYtelseType.PLEIEPENGER_SYKT_BARN;
                case PLEIEPENGER_NÆRSTÅENDE -> AbacFagsakYtelseType.PLEIEPENGER_NÆRSTÅENDE;
                case OMSORGSPENGER -> AbacFagsakYtelseType.OMSORGSPENGER;
                case OPPLÆRINGSPENGER -> AbacFagsakYtelseType.OPPLÆRINGSPENGER;
                case UNGDOMSYTELSE -> AbacFagsakYtelseType.UNGDOMSYTELSE;
                default -> throw new IllegalArgumentException("Ikke-støttet verdi: " + ytelseType);
            };
        }
        logger.warn("Klarte ikke utlede ytelsetype fra {}", ytelseTyper);
        return null;
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

