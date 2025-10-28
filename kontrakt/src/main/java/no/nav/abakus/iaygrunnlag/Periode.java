package no.nav.abakus.iaygrunnlag;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Periode med fom/tom dato.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class Periode {

    @JsonProperty(value = "fom")
    private LocalDate fom;

    @JsonProperty(value = "tom")
    private LocalDate tom;

    @JsonCreator
    public Periode(@JsonProperty(value = "fom") LocalDate fom, @JsonProperty(value = "tom") LocalDate tom) {
        validerGyldig(fom, tom);
        this.fom = fom;
        this.tom = tom;
    }

    public Periode(String iso8601) {
        verifiserKanVæreGyldigPeriode(iso8601);
        String[] split = iso8601.split("/");
        this.fom = parseLocalDate(split[0]);
        this.tom = parseLocalDate(split[1]);
        validerGyldig(this.fom, this.tom);
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + getFom() + ", " + getTom() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var other = getClass().cast(obj);
        return Objects.equals(this.getFom(), other.getFom()) && Objects.equals(this.getTom(), other.getTom());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFom(), getTom());
    }

    private static void verifiserKanVæreGyldigPeriode(String iso8601) {
        if (iso8601 == null || iso8601.split("/").length != 2) {
            throw new IllegalArgumentException("Periode på ugylig format '" + iso8601 + "'.");
        }
    }

    private static LocalDate parseLocalDate(String iso8601) {
        if ("..".equals(iso8601))
            return null;
        else
            return LocalDate.parse(iso8601);
    }

    private static void validerGyldig(LocalDate fom, LocalDate tom) {
        if (fom == null && tom == null) {
            throw new IllegalArgumentException("Både fom og tom er null");
        } else if (fom != null && tom != null && fom.isAfter(tom)) {
            throw new IllegalArgumentException("Input data gir umulig periode (fom > tom): [" + fom + ", " + tom + "]");
        }
    }


}
