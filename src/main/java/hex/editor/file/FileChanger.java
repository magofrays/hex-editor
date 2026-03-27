package hex.editor.file;

import hex.editor.file.history.ByteBlock;
import hex.editor.file.history.Transaction;

public interface FileChanger {
    ByteBlock doChanges(ByteBlock block);
    Transaction doChanges(Transaction transaction);
    void saveFile();
}
