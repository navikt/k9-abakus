package no.nav.k9.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;

@Converter(autoApply = true)
public class ArbeidTypeKodeverdiConverter implements AttributeConverter<ArbeidType, String> {
    @Override
    public String convertToDatabaseColumn(ArbeidType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public ArbeidType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ArbeidType.fraKode(dbData);
    }
}
