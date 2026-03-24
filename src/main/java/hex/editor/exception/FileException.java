package hex.editor.exception;

public class FileException extends RuntimeException {
    String message;
    public FileException(String message, Throwable cause) {
        super(cause.getMessage());
        this.message = message;
    }
}
