package no.nav.foreldrepenger.abakus.registerdata;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

@ApplicationScoped
@FagsakYtelseTypeRef("ES")
public class IAYRegisterInnhentingESTjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    IAYRegisterInnhentingESTjenesteImpl() {
        // CDI
    }

    @Inject
    public IAYRegisterInnhentingESTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                               KodeverkRepository kodeverkRepository,
                                               VirksomhetTjeneste virksomhetTjeneste,
                                               InnhentingSamletTjeneste innhentingSamletTjeneste,
                                               AktørConsumer aktørConsumer, SigrunTjeneste sigrunTjeneste) {
        super(inntektArbeidYtelseTjeneste,
            kodeverkRepository,
            virksomhetTjeneste,
            innhentingSamletTjeneste,
            aktørConsumer, sigrunTjeneste);
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
