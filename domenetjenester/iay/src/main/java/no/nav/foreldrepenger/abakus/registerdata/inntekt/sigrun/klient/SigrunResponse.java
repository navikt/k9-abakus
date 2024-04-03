package no.nav.k9.abakus.registerdata.inntekt.sigrun.klient;

import java.time.Year;
import java.util.List;
import java.util.Map;

public record SigrunResponse(Map<Year, List<BeregnetSkatt>> beregnetSkatt) {
}
