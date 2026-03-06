package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum YtelseStatus implements Kodeverdi {

    OPPRETTET("OPPR", "Opprettet"),
    UNDER_BEHANDLING("UBEH", "Under behandling"),
    LØPENDE("LOP", "Løpende"),
    AVSLUTTET("AVSLU", "Avsluttet"),

    UDEFINERT("-", "Ikke definert"),
    ;

    private static final Map<String, YtelseStatus> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }


    private final String navn;

    @JsonValue
    private final String kode;

    YtelseStatus(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static YtelseStatus fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent YtelseStatus: " + kode));
    }

    public static Map<String, YtelseStatus> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public String getNavn() {
        return navn;
    }

    @Override
    public String getKode() {
        return kode;
    }

    // https://github.com/navikt/aap-api-intern/blob/e1e4f20ac39e8cc527fb3bf0b0168db7d712ab6d/kontrakt/src/main/kotlin/SakStatus.kt#L15
    public static YtelseStatus fra(String status) {
        return switch (status) {
            case "AVSLUTTET", "AVSLU" -> YtelseStatus.AVSLUTTET;
            case "LØPENDE", "IVERK" -> YtelseStatus.LØPENDE;
            case "UTREDES", "GODKJ", "INNST", "FORDE", "REGIS", "MOTAT", "KONT" -> YtelseStatus.UNDER_BEHANDLING;
            case "OPPRETTET", "OPPRE" -> YtelseStatus.OPPRETTET;
            case null, default -> YtelseStatus.UDEFINERT;
        };
    }

}
