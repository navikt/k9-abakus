package no.nav.k9.abakus.web.app.vedlikehold;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.SpøkelseKlient;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.spokelse.SykepengeVedtak;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.abakus.vedtak.domene.VedtakYtelse;
import no.nav.k9.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.k9.abakus.vedtak.domene.feil.VedtakYtelseFeilDump;
import no.nav.k9.abakus.vedtak.domene.feil.VedtakYtelseFeilRepository;
import no.nav.k9.felles.integrasjon.pdl.PdlKlient;
import no.nav.k9.prosesstask.api.ProsessTask;
import no.nav.k9.prosesstask.api.ProsessTaskData;
import no.nav.k9.prosesstask.api.ProsessTaskHandler;

@ProsessTask("vedtakfeil.lagre")
public class LagreVedtakMedFeilTask implements ProsessTaskHandler {

    public static final String AKTOER_ID = "AKTOER_ID";
    public static final String VEDTAK_REFERANSE = "VEDTAK_REFERANSE";
    public static final String VEDTAK_TIDSPUNKT = "VEDTAK_TIDSPUNKT";
    public static final String YTELSE_TYPE = "YTELSE_TYPE";
    public static final String SAKSNUMMER = "SAKSNUMMER";
    public static final String PERIODER = "PERIODER";

    private SpøkelseKlient spøkelseKlient;
    private PdlKlient pdlKlient;
    private VedtakYtelseFeilRepository vedtakYtelseFeilRepository;
    private VedtakYtelseRepository vedtakYtelseRepository;
    ;

    @Inject
    public LagreVedtakMedFeilTask(SpøkelseKlient spøkelseKlient, PdlKlient pdlKlient, VedtakYtelseFeilRepository vedtakYtelseFeilRepository) {
        this.spøkelseKlient = spøkelseKlient;
        this.pdlKlient = pdlKlient;
        this.vedtakYtelseFeilRepository = vedtakYtelseFeilRepository;
    }

    public LagreVedtakMedFeilTask() {
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Set<IntervallEntitet> perioder = utledPerioder(prosessTaskData);
        var aktørId = new AktørId(prosessTaskData.getPropertyValue(AKTOER_ID));
        var vedtakReferanse = UUID.fromString(prosessTaskData.getPropertyValue(VEDTAK_REFERANSE));
        var vedtakTidspunkt = LocalDateTime.parse(prosessTaskData.getPropertyValue(VEDTAK_TIDSPUNKT), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        var ytelseType = YtelseType.fraKode(prosessTaskData.getPropertyValue(YTELSE_TYPE));
        Saksnummer saksnummer = new Saksnummer(prosessTaskData.getPropertyValue(SAKSNUMMER));

        var personIdent = pdlKlient.hentPersonIdentForAktørId(aktørId.getId()).orElseThrow();
        LocalDate fom = perioder.stream().map(IntervallEntitet::getFomDato).min(Comparator.naturalOrder()).orElseThrow();
        boolean harOverlappendeSykepengeVedtak = harOverlappendeSykepengeVedtak(personIdent, fom, perioder);


        LocalDate tom = perioder.stream().map(IntervallEntitet::getTomDato).max(Comparator.naturalOrder()).orElseThrow();

        boolean harOverlappendeForeldrepengeVedtak = harOvelappendeForeldrepengeVedtak(aktørId, fom, tom, perioder);

        vedtakYtelseFeilRepository.lagre(
            new VedtakYtelseFeilDump(aktørId, ytelseType, vedtakTidspunkt, vedtakReferanse, IntervallEntitet.fraOgMedTilOgMed(fom, tom),
                harOverlappendeSykepengeVedtak, harOverlappendeForeldrepengeVedtak, saksnummer));

    }

    private boolean harOvelappendeForeldrepengeVedtak(AktørId aktørId, LocalDate fom, LocalDate tom, Set<IntervallEntitet> perioder) {
        List<VedtakYtelse> foreldrepengeVedtak = vedtakYtelseRepository.hentYtelserForIPeriode(aktørId, fom, tom)
            .stream()
            .filter(it -> it.getYtelseType() == YtelseType.FORELDREPENGER)
            .toList();

        boolean harOverlappendeForeldrepengeVedtak = foreldrepengeVedtak.stream()
            .flatMap(it -> it.getYtelseAnvist().stream())
            .anyMatch(it -> perioder.stream().anyMatch(p -> IntervallEntitet.fraOgMedTilOgMed(it.getAnvistFom(), it.getAnvistTom()).overlapper(p))
                && it.getUtbetalingsgradProsent().isPresent() && !it.getUtbetalingsgradProsent().get().erNulltall());
        return harOverlappendeForeldrepengeVedtak;
    }

    private boolean harOverlappendeSykepengeVedtak(String personIdent, LocalDate fom, Set<IntervallEntitet> perioder) {
        List<SykepengeVedtak> sykepengeVedtakListe = spøkelseKlient.hentGrunnlag(personIdent, fom);
        boolean harOverlappendeSykepengeVedtak = sykepengeVedtakListe.stream()
            .flatMap(it -> it.utbetalingerNonNull().stream())
            .anyMatch(it -> perioder.stream().anyMatch(p -> IntervallEntitet.fraOgMedTilOgMed(it.fom(), it.tom()).overlapper(p))
                && it.grad().compareTo(BigDecimal.ZERO) > 0);
        return harOverlappendeSykepengeVedtak;
    }

    private Set<IntervallEntitet> utledPerioder(ProsessTaskData prosessTaskData) {
        var perioderString = prosessTaskData.getPropertyValue(PERIODER);
        return parseToPeriodeSet(perioderString);

    }

    TreeSet<IntervallEntitet> parseToPeriodeSet(String perioderString) {
        return Arrays.stream(perioderString.split("\\|")).map(Periode::new).map(IntervallEntitet::fra).collect(Collectors.toCollection(TreeSet::new));
    }

}
