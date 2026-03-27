package hex.editor.file.page;

import hex.editor.file.FileChanger;
import hex.editor.file.history.ByteBlock;
import hex.editor.file.history.Transaction;

import java.util.List;

public interface PageOperations extends FileChanger {
    byte[] doUpdate(Transaction t);
    byte doUpdate(ByteBlock block);
    void doInsert(Transaction t);
    void doInsert(ByteBlock block);
    byte[] doDelete(Transaction t);
    byte doDelete(ByteBlock block);
    List<Byte> readPage();
    void savePage();
    default void saveFile(){
        savePage();
    }

}
