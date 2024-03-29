package no.nav.abakus.vedtak.ytelse;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "version")
@JsonSubTypes({@JsonSubTypes.Type(value = YtelseV1.class, name = "1.0"),})
public abstract class Ytelse {

    public abstract Ytelser getYtelse();

    public abstract String getSaksnummer();

    public abstract Aktør getAktør();
}
