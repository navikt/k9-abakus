package no.nav.k9.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.Landkode;

@Converter(autoApply = true)
public class LandKodeKodeverdiConverter implements AttributeConverter<Landkode, String> {
    @Override
    public String convertToDatabaseColumn(Landkode attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public Landkode convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Landkode.fraKode(dbData);
    }
}
