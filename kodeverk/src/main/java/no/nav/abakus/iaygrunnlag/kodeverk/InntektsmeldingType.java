package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektsmeldingType implements Kodeverdi {
    ORDINÆR("ORDINÆR", "Ordinær"),
    OMSORGSPENGER_REFUSJON("OMSORGSPENGER_REFUSJON", "Omsorgspenger refusjon"),
    ARBEIDSGIVERINITIERT_NYANSATT("ARBEIDSGIVERINITIERT_NYANSATT", "Arbeidsgiverinitiert nyansatt"),
    ARBEIDSGIVERINITIERT_UREGISTRERT("ARBEIDSGIVERINITIERT_UREGISTRERT", "Arbeidsgiverinitiert uregistrert"),
    UDEFINERT("-", "Ikke definert");

    private static final Map<String, InntektsmeldingType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;
    private final String navn;

    InntektsmeldingType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static InntektsmeldingType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent InntektsmeldingType: " + kode));
    }

    public static Map<String, InntektsmeldingType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getKode() {
        return kode;
    }

    public String getNavn() {
        return navn;
    }
}
