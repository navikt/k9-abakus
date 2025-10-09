package no.nav.k9.abakus.felles.samtidighet;

public class UncheckedInterruptException extends RuntimeException{

    public UncheckedInterruptException(String message, InterruptedException cause) {
        super(message, cause);
    }

}
