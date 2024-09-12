package no.nav.k9.abakus.iay.tjeneste;

import static no.nav.k9.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.FAGSAK;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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
import no.nav.abakus.iaygrunnlag.ArbeidsforholdReferanse;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.AktørDatoRequest;
import no.nav.k9.abakus.aktor.AktørTjeneste;
import no.nav.k9.abakus.domene.iay.Arbeidsgiver;
import no.nav.k9.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.k9.abakus.felles.LoggUtil;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.iay.tjeneste.dto.arbeidsforhold.ArbeidsforholdDtoTjeneste;
import no.nav.k9.abakus.kobling.KoblingReferanse;
import no.nav.k9.abakus.kobling.KoblingTjeneste;
import no.nav.k9.abakus.registerdata.arbeidsforhold.rest.AaregRestKlient;
import no.nav.k9.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.InternArbeidsforholdRef;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;

@OpenAPIDefinition(tags = @Tag(name = "arbeidsforhold"))
@Path("/arbeidsforhold/v1")
@ApplicationScoped
@Transactional
public class ArbeidsforholdRestTjeneste {
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess");
    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsforholdRestTjeneste.class);

    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private ArbeidsforholdDtoTjeneste dtoTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;
    private AaregRestKlient aaregRestKlient;
    private AktørTjeneste aktørConsumer;

    public ArbeidsforholdRestTjeneste() {
    } // CDI Ctor

    @Inject
    public ArbeidsforholdRestTjeneste(KoblingTjeneste koblingTjeneste,
                                      InntektArbeidYtelseTjeneste iayTjeneste,
                                      ArbeidsforholdDtoTjeneste dtoTjeneste,
                                      VirksomhetTjeneste virksomhetTjeneste, AaregRestKlient aaregRestKlient, AktørTjeneste aktørConsumer) {
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.dtoTjeneste = dtoTjeneste;
        this.virksomhetTjeneste = virksomhetTjeneste;
        this.aaregRestKlient = aaregRestKlient;
        this.aktørConsumer = aktørConsumer;
    }

    @POST
    @Path("/arbeidstaker")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Gir ut alle arbeidsforhold i en gitt periode/dato for en gitt aktør. NB! Proxyer direkte til aa-registeret / ingen bruk av sak/kobling i abakus", tags = "arbeidsforhold")
    @BeskyttetRessurs(actionType = ActionType.READ, resource = FAGSAK)
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


    @POST
    @Path("/arbeidstaker/raw")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Gir ut alle arbeidsforhold i en gitt periode/dato for en gitt aktør. NB! Proxyer direkte til aa-registeret / ingen bruk av sak/kobling i abakus", tags = "arbeidsforhold")
    @BeskyttetRessurs(actionType = ActionType.READ, resource = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentArbeidsforholdRaw(@NotNull @TilpassetAbacAttributt(supplierClass = AktørDatoRequestAbacDataSupplier.class) @Valid AktørDatoRequest request) {
        AktørId aktørId = new AktørId(request.getAktør().getIdent());
        Periode periode = request.getPeriode();
        YtelseType ytelse = request.getYtelse() != null ? request.getYtelse() : YtelseType.UDEFINERT;
        LOG_CONTEXT.add("ytelseType", request.getYtelse().getKode());
        LOG_CONTEXT.add("periode", periode);

        LocalDate fom = periode.getFom();
        LocalDate tom = Objects.equals(fom, periode.getTom()) ? fom.plusDays(1) // enkel dato søk
            : periode.getTom(); // periode søk
        LOG.info("ABAKUS arbeidstaker - sjekk consumers for ytelse {}", ytelse);
        var personIdent = aktørConsumer.hentIdentForAktør(aktørId).orElseThrow();
        var arbeidstakersArbeidsforhold = aaregRestKlient.finnArbeidsforholdForArbeidstaker(personIdent.getIdent(), fom, tom);
        final Response response = Response.ok(arbeidstakersArbeidsforhold).build();
        return response;
    }


    @POST
    @Path("/referanse")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Finner eksisterende intern referanse for arbeidsforholdId eller lager en ny", tags = "arbeidsforhold")
    @BeskyttetRessurs(actionType = ActionType.CREATE, resource = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response finnEllerOpprettArbeidsforholdReferanse(@NotNull @TilpassetAbacAttributt(supplierClass = ArbeidsforholdReferanseAbacDataSupplier.class) @Valid ArbeidsforholdReferanse request) {

        KoblingReferanse referanse = new KoblingReferanse(UUID.fromString(request.getKoblingReferanse().getReferanse()));
        setupLogMdcFraKoblingReferanse(referanse);

        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(referanse));

        ArbeidsforholdInformasjon arbeidsforholdInformasjon = iayTjeneste.hentArbeidsforholdInformasjonForKobling(referanse);

        String abakusReferanse = request.getArbeidsforholdId().getAbakusReferanse();
        InternArbeidsforholdRef arbeidsforholdRef = arbeidsforholdInformasjon.finnEllerOpprett(tilArbeidsgiver(request),
            InternArbeidsforholdRef.ref(abakusReferanse));

        var dto = dtoTjeneste.mapArbeidsforhold(request.getArbeidsgiver(), abakusReferanse, arbeidsforholdRef.getReferanse());
        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));
        final Response response = Response.ok(dto).build();
        return response;
    }

    private Arbeidsgiver tilArbeidsgiver(@Valid @NotNull ArbeidsforholdReferanse request) {
        var arbeidsgiver = request.getArbeidsgiver();
        if (arbeidsgiver.getErOrganisasjon()) {
            return Arbeidsgiver.virksomhet(virksomhetTjeneste.hentOrganisasjon(arbeidsgiver.getIdent()));
        }
        return Arbeidsgiver.person(new AktørId(arbeidsgiver.getIdent()));
    }

    private void setupLogMdcFraKoblingReferanse(KoblingReferanse koblingReferanse) {
        var kobling = koblingTjeneste.hentFor(koblingReferanse);
        kobling.filter(k -> k.getSaksnummer() != null)
            .ifPresent(k -> LoggUtil.setupLogMdc(k.getYtelseType(), kobling.get().getSaksnummer().getVerdi(),
                koblingReferanse.getReferanse())); // legger til saksnummer i MDC
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

    public static class ArbeidsforholdReferanseAbacDataSupplier implements Function<Object, AbacDataAttributter> {
        public ArbeidsforholdReferanseAbacDataSupplier() {
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }
}
