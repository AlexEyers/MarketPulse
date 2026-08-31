package kafkaredis.marketpulse.exception;

public class WatchlistItemAlreadyExistsException extends RuntimeException {

    public WatchlistItemAlreadyExistsException(String message) {
        super(message);
    }
}
