package no.nav.k9.abakus.domene.iay.inntektsmelding;

import no.nav.k9.abakus.typer.InternArbeidsforholdRef;

public class ForvaltningEndreInternReferanse {

    public static void endreReferanse(Inntektsmelding im, InternArbeidsforholdRef internArbeidsforholdRef) {
        im.setArbeidsforholdId(internArbeidsforholdRef);
    }

}
