package no.nav.k9.abakus.iay.tjeneste;

import static no.nav.k9.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.FAGSAK;

import java.util.Optional;
import java.util.function.Function;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.request.OppgittOpptjeningMottattRequest;
import no.nav.k9.abakus.domene.iay.GrunnlagReferanse;
import no.nav.k9.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.k9.abakus.felles.LoggUtil;
import no.nav.k9.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt;
import no.nav.k9.abakus.iay.OppgittOpptjeningTjeneste;
import no.nav.k9.abakus.iay.tjeneste.dto.iay.MapOppgittOpptjening;
import no.nav.k9.abakus.kobling.KoblingReferanse;
import no.nav.k9.abakus.kobling.KoblingTjeneste;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "oppgitt opptjening"))
@Path("/iay/oppgitt/v1")
@ApplicationScoped
@Transactional
public class OppgittOpptjeningRestTjeneste {

    private KoblingTjeneste koblingTjeneste;
    private OppgittOpptjeningTjeneste oppgittOpptjeningTjeneste;

    public OppgittOpptjeningRestTjeneste() {
    } // CDI Ctor

    @Inject
    public OppgittOpptjeningRestTjeneste(KoblingTjeneste koblingTjeneste, OppgittOpptjeningTjeneste oppgittOpptjeningTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.oppgittOpptjeningTjeneste = oppgittOpptjeningTjeneste;
    }

    @POST
    @Path("/motta")
    @Operation(description = "Lagrer ned mottatt oppgitt opptjening", tags = "oppgitt opptjening", responses = {@ApiResponse(description = "Oppdatert grunnlagreferanse", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UuidDto.class)))})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = AbakusBeskyttetRessursAttributt.FAGSAK)
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response lagreOppgittOpptjening(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid OppgittOpptjeningMottattRequest mottattRequest) {
        LoggUtil.setupLogMdc(mottattRequest.getYtelseType(), mottattRequest.getSaksnummer(), mottattRequest.getKoblingReferanse());

        if (mottattRequest.harOppgittJournalpostId() || mottattRequest.harOppgittInnsendingstidspunkt()) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(),
                "v1/motta skal ikke ha journalpostId eller innsendingstidspunkt. Skal du egentlig bruke /v2/motta ?").build();
        }

        Response response;

        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(koblingReferanse));
        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());
        var kobling = koblingTjeneste.finnEllerOpprett(mottattRequest.getYtelseType(), koblingReferanse, aktørId,
            new Saksnummer(mottattRequest.getSaksnummer()));

        OppgittOpptjeningBuilder builder = new MapOppgittOpptjening().mapFraDto(mottattRequest.getOppgittOpptjening());
        GrunnlagReferanse grunnlagReferanse = oppgittOpptjeningTjeneste.lagre(koblingReferanse, builder);

        koblingTjeneste.lagre(kobling);
        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));

        if (grunnlagReferanse != null) {
            response = Response.ok(new UuidDto(grunnlagReferanse.getReferanse())).build();
        } else {
            response = Response.noContent().build();
        }

        return response;
    }

    @POST
    @Path("/overstyr")
    @Operation(description = "Lagrer ned mottatt oppgitt opptjening", tags = "oppgitt opptjening", responses = {@ApiResponse(description = "Oppdatert grunnlagreferanse", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UuidDto.class)))})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.UPDATE, resource = FAGSAK)
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response lagreOverstyrtOppgittOpptjening(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid OppgittOpptjeningMottattRequest mottattRequest) {
        Response response;
        LoggUtil.setupLogMdc(mottattRequest.getYtelseType(), mottattRequest.getSaksnummer(), mottattRequest.getKoblingReferanse());
        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(koblingReferanse));
        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());
        var kobling = koblingTjeneste.finnEllerOpprett(mottattRequest.getYtelseType(), koblingReferanse, aktørId,
            new Saksnummer(mottattRequest.getSaksnummer()));

        OppgittOpptjeningBuilder builder = new MapOppgittOpptjening().mapFraDto(mottattRequest.getOppgittOpptjening());
        GrunnlagReferanse grunnlagReferanse = oppgittOpptjeningTjeneste.lagreOverstyring(koblingReferanse, builder);

        koblingTjeneste.lagre(kobling);
        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));

        if (grunnlagReferanse != null) {
            response = Response.ok(new UuidDto(grunnlagReferanse.getReferanse())).build();
        } else {
            response = Response.noContent().build();
        }

        return response;
    }

    @POST
    @Path("/motta-og-nullstill-overstyring")
    @Operation(description = "Lagrer ned mottatt oppgitt opptjening og fjerner overstyring om den finnes", tags = "oppgitt opptjening", responses = {@ApiResponse(description = "Oppdatert grunnlagreferanse", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UuidDto.class)))})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.UPDATE, resource = FAGSAK)
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response lagreOppgittOpptjeningOgNullstillOverstyring(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid OppgittOpptjeningMottattRequest mottattRequest) {
        Response response;
        LoggUtil.setupLogMdc(mottattRequest.getYtelseType(), mottattRequest.getSaksnummer(), mottattRequest.getKoblingReferanse());
        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(koblingReferanse));
        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());
        var kobling = koblingTjeneste.finnEllerOpprett(mottattRequest.getYtelseType(), koblingReferanse, aktørId,
            new Saksnummer(mottattRequest.getSaksnummer()));

        OppgittOpptjeningBuilder builder = new MapOppgittOpptjening().mapFraDto(mottattRequest.getOppgittOpptjening());
        GrunnlagReferanse grunnlagReferanse = oppgittOpptjeningTjeneste.lagreOgNullstillOverstyring(koblingReferanse, builder);

        koblingTjeneste.lagre(kobling);
        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));

        if (grunnlagReferanse != null) {
            response = Response.ok(new UuidDto(grunnlagReferanse.getReferanse())).build();
        } else {
            response = Response.noContent().build();
        }

        return response;
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object o) {
            var req = (OppgittOpptjeningMottattRequest) o;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getIdent());
        }
    }
}
