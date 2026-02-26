package no.nav.k9.abakus.registerdata.fagsakytelser.aktivitetspenger;

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
@YtelseTypeRef(YtelseType.AKTIVITETSPENGER)
public class IAYRegisterInnhentingAktivitetspengerTjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    protected IAYRegisterInnhentingAktivitetspengerTjenesteImpl() {
        super();
    }

    @Inject
    public IAYRegisterInnhentingAktivitetspengerTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
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
        return true;
    }

    @Override
    public boolean skalInnhenteYtelseGrunnlag(Kobling kobling) {
        return false;
    }

}
