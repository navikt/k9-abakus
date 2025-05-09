package no.nav.k9.abakus.app.diagnostikk.dumps;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.app.diagnostikk.DebugDump;
import no.nav.k9.abakus.app.diagnostikk.DumpKontekst;
import no.nav.k9.abakus.app.diagnostikk.DumpOutput;
import no.nav.k9.abakus.app.jackson.JacksonJsonConfig;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.k9.abakus.registerdata.IAYRegisterInnhentingFellesTjenesteImpl;
import no.nav.k9.abakus.registerdata.arbeidsforhold.rest.AaregRestKlient;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.FinnInntektRequest;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.SpøkelseKlient;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.felles.InfotrygdGrunnlagAggregator;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.respons.Grunnlag;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.PersonIdent;

@ApplicationScoped
@YtelseTypeRef
public class RegisterInnhentingDump implements DebugDump {

    private static final Collection<InntektskildeType> INNTEKTSKILDER = IAYRegisterInnhentingFellesTjenesteImpl.ELEMENT_TIL_INNTEKTS_KILDE_MAP.values();
    private static final String PREFIKS = "register-innhenting";
    private InntektTjeneste inntektTjeneste;
    private AaregRestKlient aaregKlient;
    private InfotrygdGrunnlagAggregator infotrygdGrunnlag;
    private SpøkelseKlient spokelseKlient;
    private ObjectWriter writer;

    public RegisterInnhentingDump() {
        //
    }

    @Inject
    public RegisterInnhentingDump(AaregRestKlient aaregKlient,
                                  InntektTjeneste inntektTjeneste,
                                  InfotrygdGrunnlagAggregator infotrygdGrunnlag,
                                  SpøkelseKlient spokelseKlient) {
        this.aaregKlient = aaregKlient;
        this.inntektTjeneste = inntektTjeneste;
        this.infotrygdGrunnlag = infotrygdGrunnlag;
        this.spokelseKlient = spokelseKlient;
        this.writer = new JacksonJsonConfig().getObjectMapper().writerWithDefaultPrettyPrinter();
    }

    @Override
    public List<DumpOutput> dump(DumpKontekst dumpKontekst) {
        try {
            var future = submit(dumpKontekst, this::dumpRegister);
            return future.get(120, TimeUnit.SECONDS);
        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return lagOutputForException(e);
        } catch (Exception e) {
            return lagOutputForException(e);
        }
    }

    private static List<DumpOutput> lagOutputForException(Exception e) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return List.of(new DumpOutput(PREFIKS + "-" + e.getClass().getSimpleName() + "-ERROR.txt", sw.toString()));
    }

    private List<DumpOutput> dumpRegister(DumpKontekst kontekst) {
        var periode = kontekst.getKobling().getOpplysningsperiode();
        var aktørId = kontekst.getKobling().getAktørId();
        var ident = kontekst.getIdent();
        var fom = periode.getFomDato();
        var tom = periode.getTomDato();
        var dumps = new ArrayList<DumpOutput>();

        dumps.addAll(innhentYtelser(ident, fom, tom));

        dumps.addAll(innhentAareg(ident, periode));

        dumps.addAll(innhentInntekt(aktørId, periode, kontekst.getYtelseType()));

        return dumps;
    }

    private List<DumpOutput> innhentInntekt(AktørId aktørId, IntervallEntitet periode, YtelseType ytelseType) {
        var dumps = new ArrayList<DumpOutput>();
        var fom = periode.getFomDato();
        var tom = periode.getTomDato();
        INNTEKTSKILDER.forEach(inntektsKilde -> {
            var request = FinnInntektRequest.builder(YearMonth.from(fom), YearMonth.from(tom)).medAktørId(aktørId.getId()).build();

            dumps.add(dumpJsonOutput(PREFIKS + "-inntekt-" + inntektsKilde.getKode(), () -> inntektTjeneste.finnInntektRaw(request, inntektsKilde, ytelseType)));
        });
        return dumps;
    }

    private List<DumpOutput> innhentAareg(PersonIdent ident, IntervallEntitet periode) {
        var dumps = new ArrayList<DumpOutput>();
        var fom = periode.getFomDato();
        var tom = periode.getTomDato();
        dumps.add(dumpJsonOutput(PREFIKS + "-aareg-arbeid", () -> aaregKlient.finnArbeidsforholdForArbeidstaker(ident.getIdent(), fom, tom)));
        dumps.add(dumpJsonOutput(PREFIKS + "-aareg-frilans", () -> aaregKlient.finnArbeidsforholdForFrilanser(ident.getIdent(), fom, tom)));
        return dumps;
    }

    private List<DumpOutput> innhentYtelser(PersonIdent ident, LocalDate fom, LocalDate tom) {
        var dumps = new ArrayList<DumpOutput>();
        dumps.add(dumpJsonOutput(PREFIKS + "-sp", () -> spokelseKlient.hentGrunnlag(ident.getIdent())));

        var løpenummer = 1;
        for (Grunnlag g : infotrygdGrunnlag.hentAggregertGrunnlag(ident.getIdent(), fom, tom)) {
            dumps.add(dumpJsonOutput(PREFIKS + "-infotrygd-" + g.tema() + "-" + g.behandlingstema() + "-" + løpenummer++, () -> g));
        }
        return dumps;
    }

    private DumpOutput dumpJsonOutput(String navn, Callable<Object> kall) {
        try {
            var res = kall.call();
            return new DumpOutput(navn + ".json", writer.writeValueAsString(res));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return new DumpOutput(navn + "-ERROR.txt", sw.toString());
        }

    }

    private Future<List<DumpOutput>> submit(DumpKontekst kontekst, Function<DumpKontekst, List<DumpOutput>> call) {
        //TODO fix (se til k9-sak)
        return null;
    }
}
