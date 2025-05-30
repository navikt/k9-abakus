package no.nav.k9.abakus.web.app.diagnostikk.dumps;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import no.nav.k9.abakus.web.app.diagnostikk.CsvOutput;
import no.nav.k9.abakus.web.app.diagnostikk.DebugDump;
import no.nav.k9.abakus.web.app.diagnostikk.DumpKontekst;
import no.nav.k9.abakus.web.app.diagnostikk.DumpOutput;
import no.nav.k9.abakus.kobling.kontroll.YtelseTypeRef;

@ApplicationScoped
@YtelseTypeRef
public class ArbeidsforholdOverstyringerDataDump implements DebugDump {

    private EntityManager entityManager;

    ArbeidsforholdOverstyringerDataDump() {
        //
    }

    @Inject
    public ArbeidsforholdOverstyringerDataDump(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<DumpOutput> dump(DumpKontekst dumpKontekst) {
        var sql = "select" + " k.saksnummer, " + " k.id as kobling_id, " + " cast(k.kobling_referanse as varchar) as kobling_referanse, "
            + " k.ytelse_type," + " cast(k.bruker_aktoer_id as varchar) bruker_aktoer_id,"
            + " cast(k.annen_part_aktoer_id as varchar) annen_part_aktoer_id, " + " k.aktiv as k_aktiv, " + " gr.id as grunnlag_id, "
            + " gr.aktiv as gr_aktiv," + " gr.informasjon_id as gr_informasjon_id," + " cast(gr.grunnlag_referanse as varchar) grunnlag_referanse,"
            + " cast(ov.arbeidsforhold_intern_id_ny as varchar) as ov_arbeidsforhold_intern_id_ny,"
            + " ov.arbeidsgiver_aktor_id as ov_arbeidsgiver_aktor_id," + " ov.arbeidsgiver_orgnr as ov_arbeidsgiver_orgnr,"
            + " ov.begrunnelse as ov_begrunnelse," + " ov.handling_type as ov_handling_type," + " ov.bekreftet_tom_dato as ov_bekreftet_tom_dato,"
            + " ov.arbeidsgiver_navn as ov_arbeidsgiver_navn," + " ov.bekreftet_permisjon_status as ov_bekreftet_permisjon_status,"
            + " ov.bekreftet_permisjon_fom as ov_bekreftet_permisjon_fom," + " ov.bekreftet_permisjon_tom as ov_bekreftet_permisjon_tom,"
            + " ov.stillingsprosent as ov_stillingsprosent," + " replace(cast(ov.opprettet_tid as varchar), ' ', 'T') ov_opprettet_tid"
            + " from gr_arbeid_inntekt gr" + " inner join kobling k on k.id=gr.kobling_id"
            + " inner join iay_arbeidsforhold ov on gr.informasjon_id=ov.informasjon_id "
            + " where k.saksnummer=:saksnummer and gr.informasjon_id is not null"
            + " order by saksnummer, kobling_id, grunnlag_id, gr.informasjon_id, ov_arbeidsgiver_orgnr";

        var query = entityManager.createNativeQuery(sql, Tuple.class).setParameter("saksnummer", dumpKontekst.getSaksnummer().getVerdi());
        String path = "arbeidsforhold-overstyringer-data.csv";

        @SuppressWarnings("unchecked") List<Tuple> results = query.getResultList();

        if (results.isEmpty()) {
            return List.of();
        }

        return CsvOutput.dumpResultSetToCsv(path, results).map(List::of).orElse(List.of());
    }
}
