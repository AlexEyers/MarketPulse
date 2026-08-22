package kafkaredis.marketpulse.exception;

public class InvalidSymbolException extends RuntimeException {

    public InvalidSymbolException(String message) {
        super(message);
    }
}
