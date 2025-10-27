package no.nav.k9.abakus.web.app.vedlikehold;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.k9.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.DRIFT;
import static no.nav.k9.abakus.felles.sikkerhet.AbakusBeskyttetRessursAttributt.FAGSAK;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.request.ByttAktørRequest;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.k9.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.k9.abakus.domene.iay.søknad.OppgittEgenNæring;
import no.nav.k9.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.kobling.KoblingReferanse;
import no.nav.k9.abakus.typer.Beløp;
import no.nav.k9.abakus.typer.JournalpostId;
import no.nav.k9.abakus.typer.OrgNummer;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.abakus.vedtak.domene.VedtakYtelse;
import no.nav.k9.abakus.vedtak.domene.feil.VedtakYtelseFeilRepository;
import no.nav.k9.abakus.web.app.diagnostikk.CsvOutput;
import no.nav.k9.abakus.web.app.diagnostikk.DumpKontekst;
import no.nav.k9.abakus.web.app.diagnostikk.DumpOutput;
import no.nav.k9.felles.sikkerhet.abac.AbacDataAttributter;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessurs;
import no.nav.k9.felles.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.k9.felles.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.k9.felles.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskGruppe;
import no.nav.k9.prosesstask.api.ProsessTaskTjeneste;

@Path("/forvaltning")
@ApplicationScoped
@Transactional
public class ForvaltningRestTjeneste {

    private static final String GAMMEL = "gammel";
    private static final String GJELDENDE = "gjeldende";

    private InntektArbeidYtelseTjeneste iayTjeneste;

    private EntityManager entityManager;
    private VedtakYtelseFeilRepository vedtakYtelseFeilRepository;
    private ProsessTaskTjeneste prosessTaskTjeneste;

    public ForvaltningRestTjeneste() {
        // For CDI
    }

    @Inject
    public ForvaltningRestTjeneste(EntityManager entityManager,
                                   InntektArbeidYtelseTjeneste iayTjeneste,
                                   VedtakYtelseFeilRepository vedtakYtelseFeilRepository,
                                   ProsessTaskTjeneste prosessTaskTjeneste) {
        this.entityManager = entityManager;
        this.iayTjeneste = iayTjeneste;
        this.vedtakYtelseFeilRepository = vedtakYtelseFeilRepository;
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }


    @POST
    @Path("/lagre-vedtak-med-feil")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Lagrer dump av vedtak med feil", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Antall dumpede vedtak med feil")})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = DRIFT)
    public Response lagreVedtakMedFeil() {
        List<VedtakYtelse> vedtakMedFeil = vedtakYtelseFeilRepository.hentVedtakMedFeil();
        ProsessTaskGruppe prosessTaskGruppe = new ProsessTaskGruppe();
        vedtakMedFeil.stream().map(vedtak -> {
            var taskData = ProsessTaskData.forProsessTask(LagreVedtakMedFeilTask.class);
            taskData.setProperty(LagreVedtakMedFeilTask.AKTOER_ID, vedtak.getAktør().getId());
            taskData.setProperty(LagreVedtakMedFeilTask.SAKSNUMMER, vedtak.getSaksnummer().getVerdi());
            taskData.setProperty(LagreVedtakMedFeilTask.YTELSE_TYPE, vedtak.getYtelseType().getKode());
            var periodeString = vedtak.getYtelseAnvist().stream().filter(ya -> ya.getDagsats().isPresent() && ya.getDagsats().get().compareTo(Beløp.ZERO) > 0 && ya.getUtbetalingsgradProsent().isPresent() && ya.getUtbetalingsgradProsent().get().erNulltall())
                .map(ya -> ya.getAnvistFom() + "/" + ya.getAnvistTom())
                .collect(Collectors.joining("|"));
            taskData.setProperty(LagreVedtakMedFeilTask.PERIODER, periodeString);
            taskData.setProperty(LagreVedtakMedFeilTask.VEDTAK_REFERANSE, vedtak.getVedtakReferanse().toString());
            taskData.setProperty(LagreVedtakMedFeilTask.VEDTAK_TIDSPUNKT, vedtak.getVedtattTidspunkt().format(DateTimeFormatter.ISO_DATE_TIME));
            return taskData;
        }).forEach(prosessTaskGruppe::addNesteSekvensiell);

        prosessTaskTjeneste.lagre(prosessTaskGruppe);

        return Response.ok(vedtakMedFeil.size()).build();
    }

    @POST
    @Path("/dump-vedtak-med-feil")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(description = "Henter dump av vedtak med feil", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Dumpede vedtak med feil")})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.READ, resource = DRIFT)
    public Response dumpVedtakMedFeil() {
        StreamingOutput streamingOutput = dump();
        return Response.ok(streamingOutput)
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachment; filename=\"vedtak-dump.zip\"")
            .build();
    }

    public StreamingOutput dump() {
        return outputStream -> {
            try (var zipOut = new ZipOutputStream(new BufferedOutputStream(outputStream));) {
                var results = vedtakYtelseFeilRepository.hentDump();
                String path = "dump-vedtak.csv";
                var output = CsvOutput.dumpResultSetToCsv(path, results);
                output.ifPresent(dump -> addToZip(zipOut, dump));
            } finally {
                outputStream.flush();
                outputStream.close();
            }
        };

    }

    private void addToZip(ZipOutputStream zipOut, DumpOutput dump) {
        var zipEntry = new ZipEntry("vedtakdump/" + dump.getPath());
        try {
            zipOut.putNextEntry(zipEntry);
            zipOut.write(dump.getContent().getBytes(StandardCharsets.UTF_8));
            zipOut.closeEntry();
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke zippe dump fra : " + dump, e);
        }
    }

    @POST
    @Path("/antall-vedtak-med-feil")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Finner antall vedtak med feil", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Antall dumpede vedtak med feil")})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = DRIFT)
    public Response antallVedtakMedFeil() {
        List<VedtakYtelse> vedtakMedFeil = vedtakYtelseFeilRepository.hentVedtakMedFeil();
        return Response.ok(vedtakMedFeil.size()).build();
    }


    @POST
    @Path("/vaskBegrunnelse")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Vasker begrunnelse for ugyldige tegn", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Forekomster av egen næring med vasket begrunnelse")})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = DRIFT)
    public Response vaskBegrunnelse(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid UuidDto eksternReferanse) {
        var iayAggregat = iayTjeneste.hentAggregat(new KoblingReferanse(eksternReferanse.getReferanse()));
        var oppgittOpptjening = iayAggregat.getOppgittOpptjeningAggregat().stream().flatMap(oo -> oo.getOppgitteOpptjeninger().stream()).toList();
        var næringer = oppgittOpptjening.stream().flatMap(oo -> oo.getEgenNæring().stream().filter(OppgittEgenNæring::getVarigEndring)).toList();
        var antall = næringer.stream().map(næring -> {
            var begrunnelse = BegrunnelseVasker.vask(næring.getBegrunnelse());
            if (!begrunnelse.equals(næring.getBegrunnelse())) {
                return entityManager.createNativeQuery("UPDATE iay_egen_naering SET begrunnelse = :begr WHERE id = :enid")
                    .setParameter("begr", begrunnelse)
                    .setParameter("enid", næring.getId())
                    .executeUpdate();
            }
            return 0;
        }).reduce(Integer::sum).orElse(0);
        return Response.ok(antall).build();
    }

    @POST
    @Path("/settVarigEndring")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Setter oppgitt opptjening til å være varig endring", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Forekomster av utgått aktørid erstattet.")})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.UPDATE, resource = FAGSAK)
    public Response setVarigEndring(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid VarigEndringRequest request) {
        var oppgittOpptjeningEksternReferanse = request.getEksternReferanse().toUuidReferanse();
        var org = new OrgNummer(request.getOrgnummer());
        OppgittOpptjening oppgittOpptjening = iayTjeneste.hentOppgittOpptjeningFor(oppgittOpptjeningEksternReferanse).orElseThrow();
        var næring = oppgittOpptjening.getEgenNæring().stream().filter(n -> n.getOrgnummer().equals(org)).findFirst().orElseThrow();
        if (næring.getVarigEndring()) {
            throw new IllegalArgumentException("Allerede varig endring");
        }
        int antall = entityManager.createNativeQuery(
                "UPDATE iay_egen_naering SET varig_endring = 'J', endring_dato = :edato , brutto_inntekt = :belop, begrunnelse = :begr WHERE id = :enid")
            .setParameter("edato", request.getEndringDato())
            .setParameter("belop", request.getBruttoInntekt())
            .setParameter("begr", request.getEndringBegrunnelse())
            .setParameter("enid", næring.getId())
            .executeUpdate();
        return Response.ok(antall).build();
    }

    @POST
    @Path("/eliminerInntektsmelding")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Fjerner angitt inntektsmelding/journalpost fra grunnlag", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Inntektsmelding eliminert.")})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.UPDATE, resource = FAGSAK)
    public Response eliminerInntektsmelding(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AbacDataSupplier.class) @NotNull @Valid EliminerInntektsmeldingRequest request) {
        var koblingReferanse = new KoblingReferanse(request.getEksternReferanse().toUuidReferanse());
        var journalpost = new JournalpostId(request.getJournalpostId());
        var eksisterende = iayTjeneste.hentGrunnlagFor(koblingReferanse).orElseThrow();
        var grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(eksisterende);
        var sammeJpId = eksisterende.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .orElse(List.of())
            .stream()
            .filter(im -> journalpost.equals(im.getJournalpostId()))
            .findFirst();
        if (sammeJpId.isEmpty()) {
            throw new IllegalArgumentException("Fant ingen inntektsmelding med angitt journalpostID");
        }
        var beholdIM = eksisterende.getInntektsmeldinger()
            .map(InntektsmeldingAggregat::getInntektsmeldinger)
            .orElse(List.of())
            .stream()
            .filter(im -> !journalpost.equals(im.getJournalpostId()))
            .toList();
        grunnlagBuilder.setInntektsmeldinger(new InntektsmeldingAggregat(beholdIM));
        iayTjeneste.lagre(koblingReferanse, grunnlagBuilder);
        return Response.ok().build();
    }


    @POST
    @Path("/oppdaterAktoerId")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "MERGE: Oppdaterer aktørid for bruker i nødvendige tabeller", tags = "FORVALTNING", responses = {@ApiResponse(responseCode = "200", description = "Forekomster av utgått aktørid erstattet.")})
    @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = DRIFT)
    public Response oppdaterAktoerId(@TilpassetAbacAttributt(supplierClass = ForvaltningRestTjeneste.AktørRequestAbacDataSupplier.class) @NotNull @Valid ByttAktørRequest request) {
        int antall = oppdaterAktørIdFor(request.getUtgåttAktør().getVerdi(), request.getGyldigAktør().getVerdi());
        return Response.ok(antall).build();
    }

    private int oppdaterAktørIdFor(String gammel, String gjeldende) {
        int antall = 0;
        antall += entityManager.createNativeQuery("UPDATE kobling SET bruker_aktoer_id = :gjeldende WHERE bruker_aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE kobling SET bruker_aktoer_id = :gjeldende WHERE bruker_aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE kobling SET annen_part_aktoer_id = :gjeldende WHERE annen_part_aktoer_id = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE iay_aktoer_inntekt SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE iay_aktoer_arbeid SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE iay_aktoer_ytelse SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        antall += entityManager.createNativeQuery("UPDATE vedtak_ytelse SET AKTOER_ID = :gjeldende WHERE AKTOER_ID = :gammel")
            .setParameter(GJELDENDE, gjeldende)
            .setParameter(GAMMEL, gammel)
            .executeUpdate();
        entityManager.flush();
        return antall;
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }

    public static class AktørRequestAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        public AktørRequestAbacDataSupplier() {
            // Jackson
        }

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (ByttAktørRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getGyldigAktør().getVerdi());
        }
    }


}
