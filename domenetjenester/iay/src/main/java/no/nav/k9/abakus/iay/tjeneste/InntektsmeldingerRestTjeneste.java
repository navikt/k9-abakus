package no.nav.k9.abakus.iay.tjeneste;

import static no.nav.k9.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.FAGSAK;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.FnrPersonident;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InntektsmeldingDiffRequest;
import no.nav.abakus.iaygrunnlag.request.InntektsmeldingerMottattRequest;
import no.nav.abakus.iaygrunnlag.request.InntektsmeldingerRequest;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.k9.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.k9.abakus.felles.LoggUtil;
import no.nav.k9.abakus.felles.sikkerhet.AbakusAbacAttributtType;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.iay.InntektsmeldingerTjeneste;
import no.nav.k9.abakus.iay.tjeneste.dto.iay.MapInntektsmeldinger;
import no.nav.k9.abakus.kobling.KoblingReferanse;
import no.nav.k9.abakus.kobling.KoblingTjeneste;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.AbacDto;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;

@OpenAPIDefinition(tags = @Tag(name = "inntektsmelding"))
@Path("/iay/inntektsmeldinger/v1")
@ApplicationScoped
@Transactional
public class InntektsmeldingerRestTjeneste {

    private InntektsmeldingerTjeneste imTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;

    public InntektsmeldingerRestTjeneste() {
    } // CDI Ctor

    @Inject
    public InntektsmeldingerRestTjeneste(InntektsmeldingerTjeneste imTjeneste,
                                         KoblingTjeneste koblingTjeneste,
                                         InntektArbeidYtelseTjeneste iayTjeneste) {
        this.imTjeneste = imTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
    }

    @POST
    @Path("/hentAlle")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent inntektsmeldinger for angitt søke spesifikasjon", tags = "inntektsmelding")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, resource = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentInntektsmeldingerForSak(@NotNull @Valid InntektsmeldingerRequestAbacDto spesifikasjon) {
        LoggUtil.setupLogMdc(spesifikasjon.getYtelseType(), spesifikasjon.getSaksnummer(), "<alle>");

        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseType = spesifikasjon.getYtelseType();
        var inntektsmeldingerMap = iayTjeneste.hentArbeidsforholdinfoInntektsmeldingerMapFor(aktørId, saksnummer, ytelseType);
        InntektsmeldingerDto inntektsmeldingerDto = MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(inntektsmeldingerMap);
        final Response build = Response.ok(inntektsmeldingerDto).build();

        return build;
    }

    @POST
    @Path("/motta")
    @Operation(description = "Motta og lagre inntektsmelding(er)", tags = "inntektsmelding", responses = {@ApiResponse(description = "Oppdatert grunnlagreferanse", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UuidDto.class)))})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public UuidDto lagreInntektsmeldinger(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid InntektsmeldingerMottattRequest mottattRequest) {
        UuidDto resultat = null;
        LoggUtil.setupLogMdc(mottattRequest.getYtelseType(), mottattRequest.getSaksnummer(), mottattRequest.getKoblingReferanse().toString());

        var aktørId = new AktørId(mottattRequest.getAktør().getIdent());

        var koblingReferanse = new KoblingReferanse(mottattRequest.getKoblingReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(koblingReferanse));
        var kobling = koblingTjeneste.finnEllerOpprett(mottattRequest.getYtelseType(), koblingReferanse, aktørId,
            new Saksnummer(mottattRequest.getSaksnummer()));

        var informasjonBuilder = ArbeidsforholdInformasjonBuilder.oppdatere(imTjeneste.hentArbeidsforholdInformasjonForKobling(koblingReferanse));

        var inntektsmeldingerAggregat = new MapInntektsmeldinger.MapFraDto().map(informasjonBuilder, mottattRequest.getInntektsmeldinger());

        List<Inntektsmelding> inntektsmeldinger = inntektsmeldingerAggregat.getInntektsmeldinger();
        valider(kobling.getYtelseType(), inntektsmeldinger);
        var grunnlagReferanse = imTjeneste.lagre(koblingReferanse, informasjonBuilder, inntektsmeldinger);

        koblingTjeneste.lagre(kobling);

        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));

        if (grunnlagReferanse != null) {
            resultat = new UuidDto(grunnlagReferanse.getReferanse());
        }

        return resultat;
    }

    private void valider(YtelseType ytelseType, List<Inntektsmelding> inntektsmeldinger) {
        switch (ytelseType) {
            case FORELDREPENGER:
            case SVANGERSKAPSPENGER:
            case UDEFINERT:
                // har ikke validering på Kapittel 14 ytelser her ennå pga feil i Gosys kopiering ved journalføring på annen sak.
                return;
            default:
                var feil = inntektsmeldinger.stream().filter(im -> im.getKanalreferanse() == null).findFirst();
                if (feil.isPresent()) {
                    throw new IllegalArgumentException("Inntektsmelding mangler kanalreferanse: " + feil);
                }
        }
    }

    @POST
    @Path("/hentDiff")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Hent inntektsmeldinger for angitt søke spesifikasjon", tags = "inntektsmelding")
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, resource = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentDifferanseMellomToReferanserPåSak(@NotNull @Valid InntektsmeldingDiffRequestAbacDto spesifikasjon) {
        LoggUtil.setupLogMdc(spesifikasjon.getYtelseType(), spesifikasjon.getSaksnummer(),
            spesifikasjon.getEksternRefEn() + "/" + spesifikasjon.getEksternRefTo());

        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());
        var saksnummer = new Saksnummer(spesifikasjon.getSaksnummer());
        var ytelseType = spesifikasjon.getYtelseType();
        Map<Inntektsmelding, ArbeidsforholdInformasjon> førsteMap = iayTjeneste.hentAlleInntektsmeldingerForEksternRef(aktørId, saksnummer,
            new KoblingReferanse(spesifikasjon.getEksternRefEn()), ytelseType);
        Map<Inntektsmelding, ArbeidsforholdInformasjon> andreMap = iayTjeneste.hentAlleInntektsmeldingerForEksternRef(aktørId, saksnummer,
            new KoblingReferanse(spesifikasjon.getEksternRefTo()), ytelseType);

        var diffMap = iayTjeneste.utledInntektsmeldingDiff(førsteMap, andreMap);
        InntektsmeldingerDto imDiffListe = MapInntektsmeldinger.mapUnikeInntektsmeldingerFraGrunnlag(diffMap);
        final Response response = Response.ok(imDiffListe).build();

        return response;
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (InntektsmeldingerMottattRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getIdent());
        }

    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    public static class InntektsmeldingerRequestAbacDto extends InntektsmeldingerRequest implements AbacDto {

        @JsonCreator
        public InntektsmeldingerRequestAbacDto(@JsonProperty(value = "personIdent", required = true) @Valid @NotNull PersonIdent person) {
            super(person);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            YtelseType ytelseType = getYtelseType();
            if (ytelseType != null) {
                abacDataAttributter.leggTil(AbakusAbacAttributtType.YTELSETYPE, ytelseType.getKode());
            }
            if (FnrPersonident.IDENT_TYPE.equals(getPerson().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.FNR, getPerson().getIdent());
            } else if (AktørIdPersonident.IDENT_TYPE.equals(getPerson().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getPerson().getIdent());
            }
            throw new java.lang.IllegalArgumentException("Ukjent identtype: " + getPerson().getIdentType());
        }

    }

    /**
     * Json bean med Abac.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
    public static class InntektsmeldingDiffRequestAbacDto extends InntektsmeldingDiffRequest implements AbacDto {

        @JsonCreator
        public InntektsmeldingDiffRequestAbacDto(@JsonProperty(value = "personIdent", required = true) @Valid @NotNull PersonIdent person) {
            super(person);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            final var abacDataAttributter = AbacDataAttributter.opprett();
            if (FnrPersonident.IDENT_TYPE.equals(getPerson().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.FNR, getPerson().getIdent());
            } else if (AktørIdPersonident.IDENT_TYPE.equals(getPerson().getIdentType())) {
                return abacDataAttributter.leggTil(StandardAbacAttributtType.AKTØR_ID, getPerson().getIdent());
            }
            throw new java.lang.IllegalArgumentException("Ukjent identtype: " + getPerson().getIdentType());
        }

    }

}
