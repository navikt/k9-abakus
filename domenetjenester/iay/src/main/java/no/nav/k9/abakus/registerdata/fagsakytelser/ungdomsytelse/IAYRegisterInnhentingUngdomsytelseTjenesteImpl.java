package no.nav.k9.abakus.registerdata.fagsakytelser.ungdomsytelse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.aktor.AktørTjeneste;
import no.nav.k9.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.k9.abakus.kobling.Kobling;
import no.nav.k9.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.k9.abakus.registerdata.IAYRegisterInnhentingFellesTjenesteImpl;
import no.nav.k9.abakus.registerdata.InnhentingSamletTjeneste;
import no.nav.k9.abakus.registerdata.VedtattYtelseInnhentingTjeneste;
import no.nav.k9.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.k9.abakus.felles.samtidighet.SystemuserThreadLogin;
import no.nav.k9.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;

@ApplicationScoped
@YtelseTypeRef(YtelseType.UNGDOMSYTELSE)
public class IAYRegisterInnhentingUngdomsytelseTjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    protected IAYRegisterInnhentingUngdomsytelseTjenesteImpl() {
        super();
    }

    @Inject
    public IAYRegisterInnhentingUngdomsytelseTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                          VirksomhetTjeneste virksomhetTjeneste,
                                                          InnhentingSamletTjeneste innhentingSamletTjeneste,
                                                          AktørTjeneste aktørConsumer,
                                                          SigrunTjeneste sigrunTjeneste,
                                                          VedtattYtelseInnhentingTjeneste vedtattYtelseInnhentingTjeneste) {
        super(inntektArbeidYtelseTjeneste, virksomhetTjeneste, innhentingSamletTjeneste, aktørConsumer, sigrunTjeneste,
            vedtattYtelseInnhentingTjeneste);
    }

    @Override
    public boolean skalInnhenteNæringsInntekterFor(Kobling behandling) {
        return false;
    }

    @Override
    public boolean skalInnhenteYtelseGrunnlag(Kobling kobling) {
        return false;
    }

}
