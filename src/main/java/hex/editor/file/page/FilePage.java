package hex.editor.file.page;

import hex.editor.file.FileChanger;
import hex.editor.file.FileHolder;
import hex.editor.file.history.ByteBlock;
import hex.editor.file.history.PageHistory;
import hex.editor.file.history.Transaction;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FilePage implements PageOperations, FileChanger {
    Long position;
    ByteBuffer actualBuffer;
    List<Byte> currentBuffer;
    Boolean isSaved;
    Instant createdAt;
    final PageHistory pageHistory;
    FileHolder fileHolder;

    public FilePage(FileHolder fileHolder, Long position) {
        isSaved = true;
        pageHistory = new PageHistory(this);
        this.fileHolder = fileHolder;
        this.position = position;
        this.createdAt = Instant.now();
    }

    public void setBuffer(ByteBuffer buffer){
        actualBuffer = buffer;
        currentBuffer = bufferToList(buffer);
    }

    public ArrayList<Byte> bufferToList(ByteBuffer buffer) {
        ArrayList<Byte> list = new ArrayList<>(buffer.remaining());
        buffer.rewind();
        while (buffer.hasRemaining()) {
            list.add(buffer.get());
        }
        return list;
    }

    public List<Byte> getCurrentBuffer() {
        return currentBuffer;
    }

    @Override
    public byte[] doDelete(Transaction transaction) {
        isSaved = false;
        int startIndex = transaction.getStart();
        int endIndex = transaction.getEnd();
        int deleteLength = endIndex - startIndex;
        byte[] deletedData = new byte[deleteLength];
        for (int i = 0; i < deleteLength; i++) {
            deletedData[i] = currentBuffer.get(startIndex + i);
        }
        List<Byte> subList = currentBuffer.subList(startIndex, endIndex);
        subList.clear();
        return deletedData;
    }

    @Override
    public byte doDelete(ByteBlock block) {
        isSaved = false;
        int index = block.getIndex();
        byte deletedData = currentBuffer.get(index);
        currentBuffer.remove(index);
        return deletedData;
    }

    @Override
    public byte[] doUpdate(Transaction transaction) {
        isSaved = false;
        int startIndex = transaction.getStart();
        int endIndex = transaction.getEnd();
        int updateLength = endIndex - startIndex;
        byte[] updatedData = new byte[updateLength];
        for (int i = 0; i < updateLength; i++) {
            updatedData[i] = currentBuffer.get(startIndex + i);
        }
        for (int i = 0; i < updateLength; i++) {
            currentBuffer.set(startIndex + i, updatedData[i]);
        }
        return updatedData;
    }

    @Override
    public byte doUpdate(ByteBlock block) {
        isSaved = false;
        int index = block.getIndex();
        byte deletedData = currentBuffer.get(index);
        currentBuffer.set(index, block.getByte());
        return deletedData;
    }

    @Override
    public void doInsert(Transaction transaction) {
        isSaved = false;
        int startIndex = transaction.getStart();
        ArrayList<Byte> data = transaction.getData();
        List<Byte> bytesToInsert = new ArrayList<>(data.size());
        bytesToInsert.addAll(data);
        currentBuffer.addAll(startIndex, bytesToInsert);
    }

    @Override
    public void doInsert(ByteBlock block) {
        isSaved = false;
        int index = block.getIndex();
        currentBuffer.add(index, block.getByte());
    }

    @Override
    public void doChanges(ByteBlock block) {
        pageHistory.doChanges(block);
    }

    @Override
    public void doChanges(Transaction transaction) {

    }
}
