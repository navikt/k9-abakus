package no.nav.k9.abakus.registerdata.fagsakytelser.k9;

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
import no.nav.k9.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;

@ApplicationScoped
@YtelseTypeRef(YtelseType.PLEIEPENGER_SYKT_BARN)
@YtelseTypeRef(YtelseType.PLEIEPENGER_NÆRSTÅENDE)
@YtelseTypeRef(YtelseType.OMSORGSPENGER)
@YtelseTypeRef(YtelseType.OPPLÆRINGSPENGER)
public class IAYRegisterInnhentingK9TjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    protected IAYRegisterInnhentingK9TjenesteImpl() {
        super();
    }

    @Inject
    public IAYRegisterInnhentingK9TjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                               VirksomhetTjeneste virksomhetTjeneste,
                                               InnhentingSamletTjeneste innhentingSamletTjeneste,
                                               AktørTjeneste aktørConsumer,
                                               SigrunTjeneste sigrunTjeneste,
                                               VedtattYtelseInnhentingTjeneste vedtattYtelseInnhentingTjeneste) {
        super(inntektArbeidYtelseTjeneste, virksomhetTjeneste, innhentingSamletTjeneste, aktørConsumer, sigrunTjeneste,
            vedtattYtelseInnhentingTjeneste);
    }

    // Skal alltid innhente næringsinntekter om det ligger i informasjonselementer, dette for å støtte 8-47.
    @Override
    public boolean skalInnhenteNæringsInntekterFor(Kobling behandling) {
        return true;
    }

    @Override
    public boolean skalInnhenteYtelseGrunnlag(Kobling kobling) {
        return true;
    }

}
