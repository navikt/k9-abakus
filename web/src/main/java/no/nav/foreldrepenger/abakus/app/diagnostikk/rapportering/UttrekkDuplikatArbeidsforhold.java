package no.nav.foreldrepenger.abakus.app.diagnostikk.rapportering;

import java.util.List;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.app.diagnostikk.CsvOutput;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DumpOutput;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

@ApplicationScoped
@RapportTypeRef(RapportType.DUPLIKAT_ARBEIDSFORHOLD)
public class UttrekkDuplikatArbeidsforhold implements RapportGenerator {

    private EntityManager entityManager;

    UttrekkDuplikatArbeidsforhold() {
        //
    }

    @Inject
    public UttrekkDuplikatArbeidsforhold(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DumpOutput> generer(YtelseType ytelseType, IntervallEntitet periode) {
        String sql = """
                   select
                      k.saksnummer, k.aktoer_id, k.ytelse_type, 
                      ar.informasjon_id, arbeidsgiver_orgnr, ekstern_referanse, 
                      count(distinct ar.id)
                    from IAY_ARBEIDSFORHOLD_REFER ar
                    inner join gr_arbeid_inntekt g on (ar.informasjon_id=g.informasjon_id and g.aktiv='J')
                    inner join kobling k on k.id=g.kobling_id
                    where k.aktiv=true and k.ytelse_type=:ytelseType
                      and (k.opplysning_periode_fom IS NULL OR ( k.opplysning_periode_fom <= :tom AND k.opplysning_periode_tom >=:fom ))
                    group by k.saksnummer, k.aktoer_id, k.ytelse_type, ar.informasjon_id, arbeidsgiver_orgnr, ekstern_referanse
                    having count(distinct ar.id) > 1;
                """;

        var query = entityManager.createNativeQuery(sql, Tuple.class)
            .setParameter("ytelseType", ytelseType.getKode())
            .setParameter("fom", periode.getFomDato())
            .setParameter("tom", periode.getTomDato()) // tar alt overlappende
            .setHint("javax.persistence.query.timeout", 2 * 60 * 1000) // 2:00 min
        ;
        String path = "duplikat-arbeidsforhold.csv";

        try (Stream<Tuple> stream = query.getResultStream()) {
            return CsvOutput.dumpResultSetToCsv(path, stream)
                .map(v -> List.of(v)).orElse(List.of());
        }

    }

}