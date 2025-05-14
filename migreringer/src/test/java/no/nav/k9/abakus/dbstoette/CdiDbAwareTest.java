package no.nav.k9.abakus.dbstoette;

import no.nav.k9.felles.testutilities.cdi.CdiAwareExtension;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(CdiAwareExtension.class)
@ExtendWith(JpaExtension.class)
public @interface CdiDbAwareTest {
}
