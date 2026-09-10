package kafkaredis.marketpulse.exception;

public class PriceAlertAlreadyExistsException extends RuntimeException {
    public PriceAlertAlreadyExistsException(String message) {
        super(message);
    }
}
