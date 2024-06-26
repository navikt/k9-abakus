package no.nav.k9.abakus.domene.iay.diff;

import java.util.function.Function;

import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;
import no.nav.k9.abakus.felles.diff.TraverseGraph;
import no.nav.k9.abakus.felles.diff.TraverseGraphConfig;
import no.nav.k9.abakus.felles.diff.TraverseJpaEntityGraphConfig;
import no.nav.k9.abakus.felles.jpa.IntervallEntitet;
import no.nav.k9.abakus.kobling.Kobling;

/*
 * Legger denne sammen med RootClass
 */
public final class TraverseEntityGraphFactory {
    private TraverseEntityGraphFactory() {
    }

    public static TraverseGraph build(boolean medChangedTrackedOnly) {
        return build(medChangedTrackedOnly, TraverseGraphConfig.NO_FILTER);
    }

    public static TraverseGraph build(boolean medChangedTrackedOnly, Function<Object, Boolean> inclusionFilter) {
        /* default oppsett for behandlingslager. */
        var config = new TraverseJpaEntityGraphConfig();
        config.setIgnoreNulls(true);
        config.setOnlyCheckTrackedFields(medChangedTrackedOnly);

        config.addLeafClasses(Kodeverdi.class);
        config.addLeafClasses(IntervallEntitet.class);

        config.addRootClasses(Kobling.class);
        config.setInclusionFilter(inclusionFilter);
        return new TraverseGraph(config);
    }

    public static TraverseGraph build() {
        return build(false);
    }
}
