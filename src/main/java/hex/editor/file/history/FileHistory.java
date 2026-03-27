package hex.editor.file.history;

public interface FileHistory {
    void undoChanges();
    void redoChanges();
    void collectEvent(Transaction event);
    void collectEvent(ByteBlock event);
}
