package no.nav.k9.abakus.registerdata;

import java.util.Set;

import no.nav.k9.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.k9.abakus.kobling.Kobling;
import no.nav.k9.abakus.registerdata.tjeneste.RegisterdataElement;

public interface IAYRegisterInnhentingTjeneste {

    boolean skalInnhenteNæringsInntekterFor(Kobling behandling);

    boolean skalInnhenteYtelseGrunnlag(Kobling kobling);

    InntektArbeidYtelseGrunnlagBuilder innhentRegisterdata(Kobling kobling, Set<RegisterdataElement> informasjonsElementer);

}
