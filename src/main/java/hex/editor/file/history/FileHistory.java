package hex.editor.file.history;

import hex.editor.file.event.ChangeEvent;

public interface FileHistory {
    void undoChanges();
    void redoChanges();
    void collectEvent(ChangeEvent event);
}
