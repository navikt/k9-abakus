package no.nav.k9.abakus.app.diagnostikk.rapportering;

import java.util.List;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.k9.abakus.app.diagnostikk.DumpOutput;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;

public interface RapportGenerator {

    List<DumpOutput> generer(YtelseType ytelseType, IntervallEntitet periode);
}
