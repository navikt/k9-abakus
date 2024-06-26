package no.nav.k9.abakus.iay.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import no.nav.abakus.iaygrunnlag.kodeverk.BekreftetPermisjonStatus;


@Converter(autoApply = true)
public class BekreftetPermisjonStatusKodeverdiConverter implements AttributeConverter<BekreftetPermisjonStatus, String> {
    @Override
    public String convertToDatabaseColumn(BekreftetPermisjonStatus attribute) {
        return attribute == null ? null : attribute.getKode();
    }

    @Override
    public BekreftetPermisjonStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BekreftetPermisjonStatus.fraKode(dbData);
    }
}
