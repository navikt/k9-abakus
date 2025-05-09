package no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.respons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record Prosent(Integer prosent) {

    @JsonCreator
    public static Prosent forValue(Integer value) {
        return new Prosent(value);
    }

    @JsonValue
    public Integer prosent() {
        return prosent;
    }
}
