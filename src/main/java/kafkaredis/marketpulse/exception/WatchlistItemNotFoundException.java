package kafkaredis.marketpulse.exception;

public class WatchlistItemNotFoundException extends RuntimeException {

    public WatchlistItemNotFoundException(String message) {
        super(message);
    }
}
