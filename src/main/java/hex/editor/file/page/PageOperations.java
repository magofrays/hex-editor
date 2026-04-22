package hex.editor.file.page;

import hex.editor.file.FileChanger;
import hex.editor.file.event.ByteBlock;
import hex.editor.file.event.Transaction;

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

    void loadPage(Integer pageSize, Long fileSize);

    Integer getIndex();

    default void saveFile() {
        savePage();
    }

}
