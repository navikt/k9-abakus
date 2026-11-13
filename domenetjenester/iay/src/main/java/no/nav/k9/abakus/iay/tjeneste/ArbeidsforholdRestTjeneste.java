package no.nav.k9.abakus.iay.tjeneste;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.AktørDatoRequest;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.iay.tjeneste.dto.arbeidsforhold.ArbeidsforholdDtoTjeneste;
import no.nav.k9.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.felles.log.mdc.MdcExtendedLogContext;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionType;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursResourceType;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "arbeidsforhold"))
@Path("/arbeidsforhold/v1")
@ApplicationScoped
@Transactional
public class ArbeidsforholdRestTjeneste {
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");
    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsforholdRestTjeneste.class);

    private ArbeidsforholdDtoTjeneste dtoTjeneste;

    public ArbeidsforholdRestTjeneste() {
    } // CDI Ctor

    @Inject
    public ArbeidsforholdRestTjeneste(InntektArbeidYtelseTjeneste iayTjeneste,
                                      ArbeidsforholdDtoTjeneste dtoTjeneste,
                                      VirksomhetTjeneste virksomhetTjeneste) {
        this.dtoTjeneste = dtoTjeneste;
    }

    @POST
    @Path("/arbeidstaker")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Gir ut alle arbeidsforhold i en gitt periode/dato for en gitt aktør. NB! Proxyer direkte til aa-registeret / ingen bruk av sak/kobling i abakus", tags = "arbeidsforhold")
    @BeskyttetRessurs(action = BeskyttetRessursActionType.READ, resource = BeskyttetRessursResourceType.FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentArbeidsforhold(@NotNull @TilpassetAbacAttributt(supplierClass = AktørDatoRequestAbacDataSupplier.class) @Valid AktørDatoRequest request) {
        AktørId aktørId = new AktørId(request.getAktør().getIdent());
        Periode periode = request.getPeriode();
        YtelseType ytelse = request.getYtelse() != null ? request.getYtelse() : YtelseType.UDEFINERT;
        LOG_CONTEXT.add("ytelseType", request.getYtelse().getKode());
        LOG_CONTEXT.add("periode", periode);

        LocalDate fom = periode.getFom();
        LocalDate tom = Objects.equals(fom, periode.getTom()) ? fom.plusDays(1) // enkel dato søk
            : periode.getTom(); // periode søk
        LOG.info("ABAKUS arbeidstaker - sjekk consumers for ytelse {}", ytelse);
        var arbeidstakersArbeidsforhold = dtoTjeneste.mapFor(aktørId, fom, tom);
        final Response response = Response.ok(arbeidstakersArbeidsforhold).build();
        return response;
    }
    public static class AktørDatoRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AktørDatoRequestAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            AktørDatoRequest req = (AktørDatoRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getIdent());
        }
    }
}
