package nintaco;

public class MessageException extends RuntimeException {

    public MessageException(String message) {
        super(message);
    }

    public MessageException(String message, Object... args) {
        super(String.format(message, args));
    }
}
