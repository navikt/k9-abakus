package no.nav.k9.abakus.web.app.jackson;

import jakarta.validation.constraints.Pattern;

class Patternklasse {

    @Pattern(regexp = "[Aa]")
    private String fritekst;
}
