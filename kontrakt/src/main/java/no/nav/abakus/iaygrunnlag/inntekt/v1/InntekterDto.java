package no.nav.abakus.iaygrunnlag.inntekt.v1;

import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.PersonIdent;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class InntekterDto {

    /**
     * Bruker som har inntektene.
     */
    @JsonProperty("person")
    @Valid
    private PersonIdent person;

    @JsonProperty("utbetalinger")
    @NotNull
    @Valid
    private List<UtbetalingDto> utbetalinger;

    protected InntekterDto() {
        // default ctor
    }

    public InntekterDto(PersonIdent person) {
        Objects.requireNonNull(person, "person");
        this.person = person;
    }

    public PersonIdent getPerson() {
        return person;
    }

    public List<UtbetalingDto> getUtbetalinger() {
        return utbetalinger;
    }

    public void setUtbetalinger(List<UtbetalingDto> utbetalinger) {
        this.utbetalinger = utbetalinger;
    }

    public InntekterDto medUtbetalinger(List<UtbetalingDto> utbetalinger) {
        setUtbetalinger(utbetalinger);
        return this;
    }
}
