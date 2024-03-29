package no.nav.abakus.iaygrunnlag.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.inntekt.v1.InntekterDto;
import no.nav.abakus.iaygrunnlag.ytelse.v1.YtelserDto;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class InntektArbeidYtelseAggregatRegisterDto extends InntektArbeidYtelseAggregatDto<InntektArbeidYtelseAggregatRegisterDto> {

    @JsonProperty(value = "inntekter")
    @Valid
    private List<InntekterDto> inntekt;

    @JsonProperty(value = "ytelser")
    @Valid
    private List<YtelserDto> ytelser;

    protected InntektArbeidYtelseAggregatRegisterDto() {
        // default ctor
    }

    public InntektArbeidYtelseAggregatRegisterDto(LocalDateTime tidspunkt, UuidDto aggregatReferanse) {
        super(tidspunkt, aggregatReferanse);
    }

    public InntektArbeidYtelseAggregatRegisterDto(LocalDateTime tidspunkt, UUID aggregatReferanse) {
        super(tidspunkt, aggregatReferanse);
    }

    public InntektArbeidYtelseAggregatRegisterDto(OffsetDateTime tidspunkt, UUID aggregatReferanse) {
        super(tidspunkt, aggregatReferanse);
    }

    public InntektArbeidYtelseAggregatRegisterDto(OffsetDateTime tidspunkt, String aggregatReferanse) {
        super(tidspunkt, UUID.fromString(aggregatReferanse));
    }

    public List<InntekterDto> getInntekt() {
        return inntekt;
    }

    public void setInntekt(List<InntekterDto> inntekt) {
        this.inntekt = inntekt;
    }

    public InntektArbeidYtelseAggregatRegisterDto medInntekt(List<InntekterDto> inntekt) {
        this.inntekt = inntekt;
        return this;
    }

    public List<YtelserDto> getYtelse() {
        return ytelser;
    }

    public void setYtelse(List<YtelserDto> ytelse) {
        this.ytelser = ytelse;
    }

    public InntektArbeidYtelseAggregatRegisterDto medYtelse(List<YtelserDto> ytelse) {
        this.ytelser = ytelse;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!super.equals(obj)) {
            return false;
        }
        InntektArbeidYtelseAggregatRegisterDto other = (InntektArbeidYtelseAggregatRegisterDto) obj;
        return Objects.equals(inntekt, other.inntekt) && Objects.equals(ytelser, other.ytelser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), inntekt, ytelser);
    }
}
