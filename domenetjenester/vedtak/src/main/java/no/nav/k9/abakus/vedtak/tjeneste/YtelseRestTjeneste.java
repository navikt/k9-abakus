package no.nav.k9.abakus.vedtak.tjeneste;

import static no.nav.k9.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.APPLIKASJON;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.request.VedtakForPeriodeRequest;
import no.nav.k9.abakus.aktor.AktørTjeneste;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.k9.abakus.vedtak.extract.v1.ConvertToYtelseV1;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "ytelse"))
@Path("/ytelse/v1")
@ApplicationScoped
@Transactional
public class YtelseRestTjeneste {

    private VedtakYtelseRepository ytelseRepository;
    private AktørTjeneste aktørTjeneste;

    public YtelseRestTjeneste() {
    } // CDI Ctor

    @Inject
    public YtelseRestTjeneste(VedtakYtelseRepository ytelseRepository, AktørTjeneste aktørTjeneste) {
        this.ytelseRepository = ytelseRepository;
        this.aktørTjeneste = aktørTjeneste;
    }

    /**
     * Intern bruk - kun fra fpsak så langt
     */
    @POST
    @Path("/hent-vedtak-ytelse")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter alle vedtak for en gitt person, evt med periode etter en fom", tags = "ytelse")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, resource = APPLIKASJON)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<Ytelse> hentVedtakYtelse(@NotNull @TilpassetAbacAttributt(supplierClass = VedtakForPeriodeRequestAbacDataSupplier.class) @Valid VedtakForPeriodeRequest request) {

        if (request.getYtelser().isEmpty()) {
            return List.of();
        }

        Set<AktørId> aktørIder = utledAktørIdFraRequest(request.getIdent());
        var periode = IntervallEntitet.fraOgMedTilOgMed(request.getPeriode().getFom(), request.getPeriode().getTom());
        var ytelser = new ArrayList<Ytelse>();
        for (AktørId aktørId : aktørIder) {
            ytelser.addAll(ytelseRepository.hentYtelserForIPeriode(aktørId, periode)
                .stream()
                .filter(it -> request.getYtelser().contains(ConvertToYtelseV1.mapYtelser(it.getYtelseType())))
                .map(ConvertToYtelseV1::convert)
                .toList());
        }

        return ytelser;
    }

    private Set<AktørId> utledAktørIdFraRequest(Aktør aktør) {
        if (aktør.erAktørId()) {
            return Set.of(new AktørId(aktør.getVerdi()));
        }
        return aktørTjeneste.hentAktørIderForIdent(new PersonIdent(aktør.getVerdi()));
    }

    public static class VedtakForPeriodeRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public VedtakForPeriodeRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (VedtakForPeriodeRequest) obj;
            var attributeType = req.getIdent().erAktørId() ? StandardAbacAttributtType.AKTØR_ID : StandardAbacAttributtType.FNR;
            return AbacDataAttributter.opprett().leggTil(attributeType, req.getIdent().getVerdi());
        }
    }

}
