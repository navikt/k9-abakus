package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface SP {

}
