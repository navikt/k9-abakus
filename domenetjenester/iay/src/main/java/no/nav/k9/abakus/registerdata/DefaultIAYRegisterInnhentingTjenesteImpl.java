package no.nav.k9.abakus.registerdata;

import no.nav.k9.abakus.aktor.AktørTjeneste;
import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.k9.abakus.domene.iay.søknad.OppgittOpptjeningAggregat;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.kobling.Kobling;
import no.nav.k9.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.k9.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.k9.abakus.felles.samtidighet.SystemuserThreadLogin;
import no.nav.k9.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

/**
 * Standard IAY register innhenter.
 */
@ApplicationScoped
@YtelseTypeRef
public class DefaultIAYRegisterInnhentingTjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    DefaultIAYRegisterInnhentingTjenesteImpl() {
        // CDI
    }

    @Inject
    public DefaultIAYRegisterInnhentingTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                    VirksomhetTjeneste virksomhetTjeneste,
                                                    InnhentingSamletTjeneste innhentingSamletTjeneste,
                                                    AktørTjeneste aktørConsumer,
                                                    SigrunTjeneste sigrunTjeneste,
                                                    VedtattYtelseInnhentingTjeneste vedtattYtelseInnhentingTjeneste) {
        super(inntektArbeidYtelseTjeneste, virksomhetTjeneste, innhentingSamletTjeneste, aktørConsumer, sigrunTjeneste,
            vedtattYtelseInnhentingTjeneste);
    }

    @Override
    public boolean skalInnhenteNæringsInntekterFor(Kobling kobling) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse());

        //FP,SVP,FRISINN bruker ikke aggregat for oppgitt opptjening (støtter kun en pr behandling)
        boolean harOppgittSNOpptjeningUtenAggregat = grunnlag.flatMap(InntektArbeidYtelseGrunnlag::getGjeldendeOppgittOpptjening)
            .map(oppgittOpptjening -> !oppgittOpptjening.getEgenNæring().isEmpty())
            .orElse(false);

        //OMP, PSB bruker aggregat for oppgitt opptjening (støtter mange pr behandling)
        Optional<OppgittOpptjeningAggregat> aggregat = grunnlag.flatMap(InntektArbeidYtelseGrunnlag::getOppgittOpptjeningAggregat);
        boolean harOppgittOpptjeningSNMedAggregat = aggregat.isPresent() && aggregat.get()
            .getOppgitteOpptjeninger()
            .stream()
            .anyMatch(oppgittOpptjening -> !oppgittOpptjening.getEgenNæring().isEmpty());

        return harOppgittSNOpptjeningUtenAggregat || harOppgittOpptjeningSNMedAggregat;


    }

    @Override
    public boolean skalInnhenteYtelseGrunnlag(Kobling kobling) {
        return true;
    }

}
