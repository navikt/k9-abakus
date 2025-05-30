package no.nav.k9.abakus.web.app.vedlikehold;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import no.nav.k9.abakus.web.app.vedlikehold.BegrunnelseVasker;
import org.junit.jupiter.api.Test;

class BegrunnelseVaskerTest {

    @Test
    void skal_vaske_begrunnelse() {

        var uvasket =
            "Gått fra \n" + "usammenhengende vikariater med varierende arbeidsomfang,til fast kontrakt med mer stabil og \n" + "høyere inntekt.";


        var forventet =
            "Gått fra " + "usammenhengende vikariater med varierende arbeidsomfang,til fast kontrakt med mer stabil og " + "høyere inntekt.";

        var vasket = BegrunnelseVasker.vask(uvasket);

        assertThat(vasket).isEqualTo(forventet);
    }
}
