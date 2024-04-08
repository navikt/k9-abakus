package no.nav.k9.abakus.domene.iay.diff;

import no.nav.k9.abakus.felles.diff.DiffEntity;
import no.nav.k9.abakus.felles.diff.TraverseGraph;

public class RegisterdataDiffsjekker {
    private DiffEntity diffEntity;
    private TraverseGraph traverseEntityGraph;

    public RegisterdataDiffsjekker() {
        this(true);
    }

    public RegisterdataDiffsjekker(boolean onlyCheckTrackedFields) {
        traverseEntityGraph = TraverseEntityGraphFactory.build(onlyCheckTrackedFields);
        diffEntity = new DiffEntity(traverseEntityGraph);
    }

    public DiffEntity getDiffEntity() {
        return diffEntity;
    }
}
