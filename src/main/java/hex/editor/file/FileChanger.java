package hex.editor.file;

import hex.editor.file.history.ByteBlock;
import hex.editor.file.history.Transaction;

public interface FileChanger {
    void doChanges(ByteBlock block);
    void doChanges(Transaction transaction);
}
