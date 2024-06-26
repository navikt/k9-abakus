package no.nav.k9.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.NaturalytelseType;

@Converter(autoApply = true)
public class NaturalytelseTypeKodeverdiConverter implements AttributeConverter<NaturalytelseType, String> {
    @Override
    public String convertToDatabaseColumn(NaturalytelseType attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public NaturalytelseType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : NaturalytelseType.fraKode(dbData);
    }
}
