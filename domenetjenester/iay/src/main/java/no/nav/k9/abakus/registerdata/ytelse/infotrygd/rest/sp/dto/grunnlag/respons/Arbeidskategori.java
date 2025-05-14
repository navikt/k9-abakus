package no.nav.k9.abakus.registerdata.ytelse.infotrygd.rest.sp.dto.grunnlag.respons;

public record Arbeidskategori(ArbeidskategoriKode kode, String termnavn) implements InfotrygdKode {

    @Override
    public String getKode() {
        return kode().getKode();
    }

}
