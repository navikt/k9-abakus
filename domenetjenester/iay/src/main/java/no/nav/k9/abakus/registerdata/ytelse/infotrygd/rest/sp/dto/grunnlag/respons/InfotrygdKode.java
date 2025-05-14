package no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.respons;

public interface InfotrygdKode {

    Enum<?> kode();

    default String getKode() {
        return kode() != null ? kode().name() : null;
    }

    String termnavn();

}
