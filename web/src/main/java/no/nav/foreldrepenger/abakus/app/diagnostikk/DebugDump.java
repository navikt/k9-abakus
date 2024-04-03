package no.nav.k9.abakus.app.diagnostikk;

import java.util.List;

public interface DebugDump {

    List<DumpOutput> dump(DumpKontekst dumpKontekst);

}
