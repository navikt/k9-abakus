package no.nav.k9.abakus.web.app.diagnostikk.rapportering;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

import no.nav.k9.abakus.web.app.diagnostikk.rapportering.RapportTypeRef.ContainerOfRapportTypeRef;

/**
 * Marker type som implementerer interface {@link RapportGenerator} for å skille ulike implementasjoner av samme steg for ulike rapportTyper
 */
@Repeatable(ContainerOfRapportTypeRef.class)
@Qualifier
@Stereotype
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface RapportTypeRef {

    /**
     * Kode-verdi som skiller ulike implementasjoner for ulike rapport typer.
     */
    RapportType value();

    /**
     * container for repeatable annotations.
     *
     * @see https://docs.oracle.com/javase/tutorial/java/annotations/repeating.html
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
    @Documented
    public @interface ContainerOfRapportTypeRef {
        RapportTypeRef[] value();
    }

    /**
     * AnnotationLiteral som kan brukes ved CDI søk.
     */
    public static class RapportTypeRefLiteral extends AnnotationLiteral<RapportTypeRef> implements RapportTypeRef {

        private RapportType navn;

        public RapportTypeRefLiteral(RapportType rapportType) {
            this.navn = rapportType;
        }

        @Override
        public RapportType value() {
            return navn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            if (!super.equals(o))
                return false;
            var that = (RapportTypeRefLiteral) o;
            return navn == that.navn;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), navn);
        }
    }

    public static final class Lookup {

        private Lookup() {
        }

        public static <I> List<Instance<I>> list(Class<I> cls, Instance<I> instances, RapportType rapportType) {
            Objects.requireNonNull(instances);
            Objects.requireNonNull(rapportType);

            final List<Instance<I>> resultat = new ArrayList<>();
            Consumer<RapportType> search = (RapportType s) -> {
                var inst = select(cls, instances, new RapportTypeRefLiteral(s));
                if (inst.isUnsatisfied()) {
                    return;
                }
                resultat.add(inst);
            };

            search.accept(rapportType);
            return List.copyOf(resultat);
        }

        private static <I> Instance<I> select(Class<I> cls, Instance<I> instances, Annotation anno) {
            return cls != null ? instances.select(cls, anno) : instances.select(anno);
        }
    }
}
