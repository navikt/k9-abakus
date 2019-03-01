package no.nav.foreldrepenger.abakus.registerdata;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface IAYRegisterInnhentingTjeneste {

    InntektArbeidYtelseAggregatBuilder innhentOpptjeningForInnvolverteParter(Kobling behandling);

    InntektArbeidYtelseAggregatBuilder innhentInntekterFor(Kobling behandling, AktørId aktørId, InntektsKilde... kilder);

    boolean skalInnhenteNæringsInntekterFor(Kobling behandling);

    InntektArbeidYtelseAggregatBuilder innhentYtelserForInvolverteParter(Kobling behandling);

}
