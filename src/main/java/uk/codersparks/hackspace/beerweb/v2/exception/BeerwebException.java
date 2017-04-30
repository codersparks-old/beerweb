package uk.codersparks.hackspace.beerweb.v2.exception;

/**
 * TODO: Add Javadoc
 */
public class BeerwebException extends Exception {

    public BeerwebException() {
        super();
    }

    public BeerwebException(String message) {
        super(message);
    }

    public BeerwebException(Throwable throwable) {
        super(throwable);
    }

    public BeerwebException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
