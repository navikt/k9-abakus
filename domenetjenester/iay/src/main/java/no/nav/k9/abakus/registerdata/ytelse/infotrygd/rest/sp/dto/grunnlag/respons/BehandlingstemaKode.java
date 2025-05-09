package no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.respons;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum BehandlingstemaKode {
    @JsonEnumDefaultValue UKJENT,
    AP,
    FP,
    FU,
    FØ,
    SV,
    SP,
    OM,
    PB,
    OP,
    PP,
    PI,
    PN
}
