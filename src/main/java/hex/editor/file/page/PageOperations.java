package hex.editor.file.page;

import hex.editor.file.history.ByteBlock;
import hex.editor.file.history.Transaction;

public interface PageOperations {
    byte[] doUpdate(Transaction t);
    byte doUpdate(ByteBlock block);
    void doInsert(Transaction t);
    void doInsert(ByteBlock block);
    byte[] doDelete(Transaction t);
    byte doDelete(ByteBlock block);


}
