package no.nav.k9.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType;

@Converter(autoApply = true)
public class ArbeidsforholdHandlingTypeKodeverdiConverter implements AttributeConverter<ArbeidsforholdHandlingType, String> {
    @Override
    public String convertToDatabaseColumn(ArbeidsforholdHandlingType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public ArbeidsforholdHandlingType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ArbeidsforholdHandlingType.fraKode(dbData);
    }
}
