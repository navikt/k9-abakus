package no.nav.k9.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag;

import java.util.List;

public record SSGResponse(List<SSGGrunnlag> grunnlag, List<SSGGrunnlag> svalbardGrunnlag, String skatteoppgjoersdato) {
}
