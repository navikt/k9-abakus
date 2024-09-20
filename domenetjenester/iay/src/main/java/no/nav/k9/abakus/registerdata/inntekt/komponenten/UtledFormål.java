package no.nav.k9.abakus.registerdata.inntekt.komponenten;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

class UtledFormål {

    static InntektsFormål utledFormålFraYtelse(YtelseType ytelse) {
        return switch (ytelse) {
            case OMSORGSPENGER -> InntektsFormål.FORMAAL_OMSORSGPENGER;
            case PLEIEPENGER_SYKT_BARN -> InntektsFormål.FORMAAL_PLEIEPENGER_SYKT_BARN;
            case PLEIEPENGER_NÆRSTÅENDE -> InntektsFormål.FORMAAL_PLEIEPENGER_NÆRSTÅENDE;
            case OPPLÆRINGSPENGER -> InntektsFormål.FORMAAL_OPPLÆRINGSPENGER;
            case FRISINN -> InntektsFormål.FORMAAL_OMSORSGPENGER; // Har ikkje eget formål for frisinn
            case UNGDOMSYTELSE -> InntektsFormål.FORMAAL_OMSORSGPENGER; // TODO: Bruk riktig formål
            default -> throw new IllegalArgumentException("Fant ingen passende formål for innhenting av inntekt for " + ytelse.getKode());
        };
    }


}
