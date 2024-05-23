package no.nav.k9.abakus.registerdata.inntekt.komponenten;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

class UtledFormålTest {

    @Test
    void skalUtledeRiktigFormålForOmsorgspenger() {
        assertThat(UtledFormål.utledFormålFraYtelse(YtelseType.OMSORGSPENGER)).isEqualTo(InntektsFormål.FORMAAL_OMSORSGPENGER);
    }

    @Test
    void skalUtledeRiktigFormålForPSB() {
        assertThat(UtledFormål.utledFormålFraYtelse(YtelseType.PLEIEPENGER_SYKT_BARN)).isEqualTo(InntektsFormål.FORMAAL_PLEIEPENGER_SYKT_BARN);
    }

    @Test
    void skalUtledeRiktigFormålForOLP() {
        assertThat(UtledFormål.utledFormålFraYtelse(YtelseType.OPPLÆRINGSPENGER)).isEqualTo(InntektsFormål.FORMAAL_OPPLÆRINGSPENGER);
    }

    @Test
    void skalUtledeRiktigFormålForPPN() {
        assertThat(UtledFormål.utledFormålFraYtelse(YtelseType.PLEIEPENGER_NÆRSTÅENDE)).isEqualTo(InntektsFormål.FORMAAL_PLEIEPENGER_NÆRSTÅENDE);
    }

}
