package hex.editor.file.controller;

import hex.editor.file.event.FileEvent;

public interface FileListener {
    void notify(FileEvent fileEvent);
}
