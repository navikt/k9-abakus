package no.nav.k9.abakus.vedtak.extract;

import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.k9.abakus.vedtak.domene.VedtakYtelseBuilder;

public interface ExtractFromYtelse<T extends Ytelse> {
    VedtakYtelseBuilder extractFrom(T ytelse);
}
