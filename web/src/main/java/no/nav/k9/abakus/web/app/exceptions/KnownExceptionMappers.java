package no.nav.k9.abakus.web.app.exceptions;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import jakarta.ws.rs.ext.ExceptionMapper;

public class KnownExceptionMappers {

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends Throwable>, ExceptionMapper> exceptionMappers = new LinkedHashMap<>();

    public KnownExceptionMappers() {

        // NB pass på rekkefølge dersom exceptions arver (håndter minst spesifikk til slutt)
        register(JsonMappingException.class, new JsonMappingExceptionMapper());
        register(JsonParseException.class, new JsonParseExceptionMapper());
    }

    @SuppressWarnings("rawtypes")
    private void register(Class<? extends Throwable> exception, ExceptionMapper mapper) {
        exceptionMappers.put(exception, mapper);
    }

    @SuppressWarnings("rawtypes")
    public ExceptionMapper getMapper(Throwable exception) {
        for (var m : exceptionMappers.entrySet()) {
            if (m.getKey().isAssignableFrom(exception.getClass())) {
                return m.getValue();
            }
        }
        throw new UnsupportedOperationException("Skal aldri komme hit, mangler ExceptionMapper for:" + exception.getClass());
    }

    @SuppressWarnings("rawtypes")
    public  Collection<ExceptionMapper> getExceptionMappers() {
        return exceptionMappers.values();
    }


}
