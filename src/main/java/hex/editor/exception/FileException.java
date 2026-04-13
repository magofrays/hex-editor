package hex.editor.exception;

public class FileException extends RuntimeException {
    String message;
    String causeMessage;

    public FileException(String message, String causeMessage) {
        this.causeMessage = causeMessage;
        this.message = message;
    }

    public String getCauseMessage() {
        return causeMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
