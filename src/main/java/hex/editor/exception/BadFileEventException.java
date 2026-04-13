package hex.editor.exception;

import hex.editor.file.event.FileEvent;

public class BadFileEventException extends RuntimeException {
    String message;
    FileEvent event;

    public BadFileEventException(String message, FileEvent event) {
        super(message);
        this.event = event;
        this.message = message;
    }
}
