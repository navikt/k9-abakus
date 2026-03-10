package no.nav.k9.abakus.registerdata;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.k9.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.k9.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.k9.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.k9.abakus.registerdata.ytelse.arena.FpwsproxyKlient;
import no.nav.k9.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.k9.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;
import no.nav.k9.abakus.registerdata.ytelse.kelvin.KelvinRestKlient;
import no.nav.k9.abakus.typer.AktørId;
import no.nav.k9.abakus.typer.PersonIdent;
import no.nav.k9.abakus.typer.Saksnummer;
import no.nav.k9.felles.konfigurasjon.env.Environment;
import no.nav.k9.felles.konfigurasjon.konfig.KonfigVerdi;

@ApplicationScoped
public class InnhentingSamletTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(InnhentingSamletTjeneste.class);
    private final boolean isDev = Environment.current().isDev();

    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private InntektTjeneste inntektTjeneste;
    private FpwsproxyKlient fpwsproxyKlient;
    private InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste;
    private KelvinRestKlient kelvinRestKlient;
    private boolean henterDataFraKelvin;

    InnhentingSamletTjeneste() {
        //CDI
    }

    @Inject
    public InnhentingSamletTjeneste(ArbeidsforholdTjeneste arbeidsforholdTjeneste,
                                    InntektTjeneste inntektTjeneste,
                                    InnhentingInfotrygdTjeneste innhentingInfotrygdTjeneste,
                                    FpwsproxyKlient fpwsproxyKlient,
                                    KelvinRestKlient kelvinRestKlient,
                                    @KonfigVerdi(value = "henter.data.fra.kelvin", defaultVerdi = "false") boolean henterDataFraKelvin
                                    ) {

        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.inntektTjeneste = inntektTjeneste;
        this.fpwsproxyKlient = fpwsproxyKlient;
        this.innhentingInfotrygdTjeneste = innhentingInfotrygdTjeneste;
        this.kelvinRestKlient = kelvinRestKlient;
        this.henterDataFraKelvin = henterDataFraKelvin;
    }


    public Map<InntektskildeType, InntektsInformasjon> getInntektsInformasjon(AktørId aktørId,
                                                                              IntervallEntitet periode,
                                                                              Set<InntektskildeType> kilder,
                                                                              YtelseType ytelseType) {
        InntektTjeneste.YearMonthPeriode månedsperiode = new InntektTjeneste.YearMonthPeriode(
            YearMonth.from(periode.getFomDato()),
            YearMonth.from(periode.getTomDato()));
        return inntektTjeneste.finnInntekt(aktørId, månedsperiode, kilder, ytelseType);
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforhold(AktørId aktørId,
                                                                                    PersonIdent ident,
                                                                                    IntervallEntitet opplysningsPeriode) {
        return arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(ident, aktørId, opplysningsPeriode);
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> getArbeidsforholdFrilans(AktørId aktørId,
                                                                                           PersonIdent ident,
                                                                                           IntervallEntitet opplysningsPeriode) {
        return arbeidsforholdTjeneste.finnArbeidsforholdFrilansForIdentIPerioden(ident, aktørId, opplysningsPeriode);
    }

    public List<InfotrygdYtelseGrunnlag> innhentInfotrygdGrunnlag(PersonIdent ident, IntervallEntitet periode) {
        if (isDev) {
            return innhentingInfotrygdTjeneste.getInfotrygdYtelserFailSoft(ident, periode);
        }
        return innhentingInfotrygdTjeneste.getInfotrygdYtelser(ident, periode);
    }

    public List<InfotrygdYtelseGrunnlag> innhentSpokelseGrunnlag(PersonIdent ident, @SuppressWarnings("unused") IntervallEntitet periode) {
        return innhentingInfotrygdTjeneste.getSPøkelseYtelser(ident, periode.getFomDato());
    }

    public List<MeldekortUtbetalingsgrunnlagSak> innhentMaksimumAAP(
        PersonIdent ident,
        IntervallEntitet opplysningsPeriode,
        Saksnummer saksnummer, List<MeldekortUtbetalingsgrunnlagSak> aapFraArena) {
        if (!henterDataFraKelvin) {
            return Collections.emptyList();
        }

        var aapGrunnlag =  kelvinRestKlient.hentAAP(ident, opplysningsPeriode.getFomDato(), opplysningsPeriode.getTomDato(), saksnummer);

        var grunnlagFraKelvin = aapGrunnlag.stream().filter(grunnlag -> grunnlag.getKilde().equals(Fagsystem.KELVIN)).collect(Collectors.toSet());
        var grunnlagFraArena = aapGrunnlag.stream().filter(grunnlag -> grunnlag.getKilde().equals(Fagsystem.ARENA)).collect(Collectors.toList());

        try {
            sammenligneArenaDirekteVsKelvin(aapFraArena, grunnlagFraArena);
        } catch (Exception _) {
            LOG.info("Maksimum AAP sammenligning av Arenadata for sak {} feilet", saksnummer.getVerdi());
        }
        var antattStp = opplysningsPeriode.getFomDato().plusMonths(17);
        var overlappStp = grunnlagFraKelvin.stream().anyMatch(v -> v.getVedtaksPeriodeFom().isBefore(antattStp) && v.getVedtaksPeriodeTom().isAfter(antattStp));
        if (overlappStp) {
            var saksnumreAAP = grunnlagFraKelvin.stream()
                .map(MeldekortUtbetalingsgrunnlagSak::getSaksnummer)
                .map(Saksnummer::getVerdi)
                .collect(Collectors.joining(", "));
            LOG.warn("Sak {} har innhentet nye Arbeidsavklaringspenger fra Kelvin saker {}. Kontakt produkteier for validering", saksnummer.getVerdi(), saksnumreAAP);
        }
        return aapGrunnlag;
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentDagpengerAAP(PersonIdent ident, IntervallEntitet opplysningsPeriode) {
        var fom = opplysningsPeriode.getFomDato();
        var tom = opplysningsPeriode.getTomDato();
        var saker = fpwsproxyKlient.hentDagpengerAAP(ident, fom, tom);
        return filtrerYtelserTjenester(saker);
    }

    private List<MeldekortUtbetalingsgrunnlagSak> filtrerYtelserTjenester(List<MeldekortUtbetalingsgrunnlagSak> saker) {
        List<MeldekortUtbetalingsgrunnlagSak> filtrert = new ArrayList<>();
        for (MeldekortUtbetalingsgrunnlagSak sak : saker) {
            if (sak.getKravMottattDato() == null) {
                if (sak.getVedtakStatus() == null) {
                    loggArenaIgnorert("vedtak", sak.getSaksnummer());
                } else {
                    loggArenaIgnorert("kravMottattDato", sak.getSaksnummer());
                }
            } else if (YtelseStatus.UNDER_BEHANDLING.equals(sak.getYtelseTilstand()) && sak.getMeldekortene().isEmpty()) {
                loggArenaIgnorert("meldekort", sak.getSaksnummer());
            } else if (sak.getVedtaksPeriodeFom() == null && sak.getMeldekortene().isEmpty()) {
                loggArenaIgnorert("vedtaksDato", sak.getSaksnummer());
            } else if (sak.getVedtaksPeriodeTom() != null && sak.getVedtaksPeriodeTom().isBefore(sak.getVedtaksPeriodeFom()) && sak.getMeldekortene()
                .isEmpty()) {
                loggArenaTomFørFom(sak.getSaksnummer());
            } else {
                filtrert.add(sak);
            }
        }
        return filtrert;
    }

    private void sammenligneArenaDirekteVsKelvin(List<MeldekortUtbetalingsgrunnlagSak> arena, List<MeldekortUtbetalingsgrunnlagSak> kelvin) {
        var arenaMK = arena.stream().map(MeldekortUtbetalingsgrunnlagSak::getMeldekortene).flatMap(Collection::stream).collect(Collectors.toSet());
        var kelvinMK = kelvin.stream().map(MeldekortUtbetalingsgrunnlagSak::getMeldekortene).flatMap(Collection::stream).collect(Collectors.toSet());
        var vAIkkeK = arena.stream().filter(a -> kelvin.stream().noneMatch(a::likeNokVedtak))
            .map(MeldekortUtbetalingsgrunnlagSak::utskriftUtenMK).collect(Collectors.joining(", "));
        var vKIkkeA = kelvin.stream().filter(a -> arena.stream().noneMatch(a::likeNokVedtak))
            .map(MeldekortUtbetalingsgrunnlagSak::utskriftUtenMK).collect(Collectors.joining(", "));
        var mAIkkeK = arenaMK.stream().filter(a -> kelvinMK.stream().noneMatch(a::equals)).collect(Collectors.toSet());
        var mKIkkeA = kelvinMK.stream().filter(a -> arenaMK.stream().noneMatch(a::equals)).collect(Collectors.toSet());
        if (arena.isEmpty() && kelvin.isEmpty()) {
            return;
        } else if (arena.isEmpty() || kelvin.isEmpty()) {
            LOG.info("Maksimum AAP sammenligning ene er tom:  arena: {} mk {} kelvin: {} mk {}", vAIkkeK, mAIkkeK, vKIkkeA, mKIkkeA);
        } else if (arena.size() != kelvin.size() || arenaMK.size() != kelvinMK.size()) {
            LOG.info("Maksimum AAP sammenligning ulik størrelse:  arena: {} mk {} kelvin: {} mk {}", vAIkkeK, mAIkkeK, vKIkkeA, mKIkkeA);
        } else {
            var likeNokVedtak = arena.stream().allMatch(a -> kelvin.stream().anyMatch(a::likeNokVedtak));
            var likeMk = kelvinMK.containsAll(arenaMK);
            if (likeNokVedtak && likeMk) {
                LOG.info("Maksimum AAP sammenligning likt svar fra arena og AAP-api");
            } else {
                LOG.info("Maksimum AAP sammenligning lik størrelse ulikt innhold: arena: {} mk {} kelvin: {} mk {}", vAIkkeK, mAIkkeK, vKIkkeA, mKIkkeA);
            }
        }
    }

    private void loggArenaIgnorert(String ignorert, Saksnummer saksnummer) {
        LOG.info("FP-112843 Ignorerer Arena-sak uten {}, saksnummer: {}", ignorert, saksnummer);
    }

    private void loggArenaTomFørFom(Saksnummer saksnummer) {
        LOG.info("FP-597341 Ignorerer Arena-sak med vedtakTom før vedtakFom, saksnummer: {}", saksnummer);
    }

}
