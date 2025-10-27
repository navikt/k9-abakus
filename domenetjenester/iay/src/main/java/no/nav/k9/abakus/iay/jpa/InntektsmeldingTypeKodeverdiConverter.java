package no.nav.k9.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektsmeldingType;

@Converter(autoApply = true)
public class InntektsmeldingTypeKodeverdiConverter implements AttributeConverter<InntektsmeldingType, String> {
    @Override
    public String convertToDatabaseColumn(InntektsmeldingType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public InntektsmeldingType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : InntektsmeldingType.fraKode(dbData);
    }
}
