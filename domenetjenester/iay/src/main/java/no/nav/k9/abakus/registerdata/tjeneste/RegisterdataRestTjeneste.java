package no.nav.k9.abakus.registerdata.tjeneste;

import static no.nav.k9.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.FAGSAK;

import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.FnrPersonident;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.abakus.iaygrunnlag.request.RegisterdataType;
import no.nav.abakus.iaygrunnlag.request.SjekkStatusRequest;
import no.nav.k9.abakus.domene.iay.GrunnlagReferanse;
import no.nav.k9.abakus.felles.LoggUtil;
import no.nav.k9.abakus.kobling.KoblingReferanse;
import no.nav.k9.abakus.kobling.KoblingTjeneste;
import no.nav.k9.abakus.registerdata.tjeneste.dto.TaskResponsDto;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;

@OpenAPIDefinition(tags = @Tag(name = "registerinnhenting"))
@Path("/registerdata/v1")
@ApplicationScoped
@Transactional
public class RegisterdataRestTjeneste {

    private InnhentRegisterdataTjeneste innhentTjeneste;

    private KoblingTjeneste koblingTjeneste;

    public RegisterdataRestTjeneste() {
    } // CDI ctor

    @Inject
    public RegisterdataRestTjeneste(InnhentRegisterdataTjeneste innhentTjeneste, KoblingTjeneste koblingTjeneste) {
        this.innhentTjeneste = innhentTjeneste;
        this.koblingTjeneste = koblingTjeneste;
    }

    @POST
    @Path("/innhent/async")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Trigger registerinnhenting for en gitt id", tags = "registerinnhenting")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = FAGSAK)
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response innhentOgLagreRegisterdataAsync(@Parameter(name = "innhent") @Valid InnhentRegisterdataAbacDto dto) {
        Response response;
        if (dto.getCallbackUrl() == null || dto.getCallbackScope() == null) {
            return Response.status(HttpURLConnection.HTTP_FORBIDDEN).build();
        }
        if (!YtelseType.abakusYtelser().contains(dto.getYtelseType())) {
            return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
        }
        LoggUtil.setupLogMdc(dto.getYtelseType(), dto.getSaksnummer());
        String taskGruppe = innhentTjeneste.triggAsyncInnhent(dto);
        if (taskGruppe != null) {
            response = Response.accepted(new TaskResponsDto(taskGruppe)).build();
        } else {
            response = Response.noContent().build();
        }
        return response;
    }

    @POST
    @Path("/innhent/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Sjekker innhentingFerdig på async innhenting og gir siste referanseid på grunnlaget når tasken er ferdig. "
        + "Hvis ikke innhentingFerdig", tags = "registerinnhenting")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, resource = FAGSAK)
    @SuppressWarnings({"findsecbugs:JAXRS_ENDPOINT", "resource"})
    public Response innhentAsyncStatus(@Parameter(name = "status") @Valid SjekkStatusAbacDto dto) {
        Response response;
        KoblingReferanse koblingRef = new KoblingReferanse(dto.getReferanse().getReferanse());
        setupLogMdcFraKoblingReferanse(koblingRef);
        if (innhentTjeneste.innhentingFerdig(dto.getTaskReferanse())) {
            Optional<GrunnlagReferanse> grunnlagReferanse = innhentTjeneste.hentSisteReferanseFor(koblingRef);
            if (grunnlagReferanse.isPresent()) {
                response = Response.ok(new UuidDto(grunnlagReferanse.get().toString())).build();
            } else {
                response = Response.noContent().build();
            }
        } else {
            response = Response.status(425).build();
        }
        return response;
    }

    private void setupLogMdcFraKoblingReferanse(KoblingReferanse koblingReferanse) {
        var kobling = koblingTjeneste.hentFor(koblingReferanse);
        kobling.filter(k -> k.getSaksnummer() != null)
            .ifPresent(k -> LoggUtil.setupLogMdc(k.getYtelseType(), kobling.get().getSaksnummer().getVerdi(),
                koblingReferanse.getReferanse())); // legger til saksnummer i MDC
    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
    public static class InnhentRegisterdataAbacDto extends InnhentRegisterdataRequest implements AbacDto {

        @JsonCreator
        public InnhentRegisterdataAbacDto(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                          @JsonProperty(value = "referanse", required = true) @Valid @NotNull UUID referanse,
                                          @JsonProperty(value = "ytelseType", required = true) @Valid @NotNull YtelseType ytelseType,
                                          @JsonProperty(value = "opplysningsperiode", required = true) @NotNull @Valid Periode opplysningsperiode,
                                          @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                          @JsonProperty(value = "elementer", required = true) @NotNull @Valid Set<RegisterdataType> elementer) {
            super(saksnummer, referanse, ytelseType, opplysningsperiode, aktør, elementer);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            AbacDataAttributter opprett = AbacDataAttributter.opprett();
            if (getAnnenPartAktør() != null) {
                leggTil(opprett, getAnnenPartAktør());
            }
            leggTil(opprett, getAktør());
            return opprett;
        }

        private void leggTil(AbacDataAttributter abac, PersonIdent person) {
            if (person != null) {
                if (FnrPersonident.IDENT_TYPE.equals(person.getIdentType())) {
                    abac.leggTil(StandardAbacAttributtType.FNR, person.getIdent());
                } else if (AktørIdPersonident.IDENT_TYPE.equals(person.getIdentType())) {
                    abac.leggTil(StandardAbacAttributtType.AKTØR_ID, person.getIdent());
                }
            }
        }

    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
    public static class SjekkStatusAbacDto extends SjekkStatusRequest implements AbacDto {

        @JsonCreator
        public SjekkStatusAbacDto(@JsonProperty(value = "referanse", required = true) @Valid @NotNull UuidDto referanse,
                                  @JsonProperty(value = "taskReferanse", required = true) @NotNull @Pattern(regexp = "\\d+") String taskReferanse) {
            super(referanse, taskReferanse);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett();
        }
    }
}
