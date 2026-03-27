package hex.editor.file.page;

import hex.editor.exception.FileException;
import hex.editor.file.FileChanger;
import hex.editor.file.FileHolder;
import hex.editor.file.event.FileEventType;
import hex.editor.file.history.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FilePage implements PageOperations {
    Long position;
    ByteBuffer actualBuffer;
    List<Byte> currentBuffer;
    Boolean isSaved;
    Instant createdAt;
    FileChannel fileChannel;

    public FilePage(FileChannel fileChannel, Long position, Integer pageSize) {
        isSaved = true;
        this.position = position;
        this.fileChannel = fileChannel;
        ByteBuffer buf = ByteBuffer.allocate(pageSize);
        try {
            this.fileChannel.position(position);
            this.fileChannel.read(buf);
            buf.flip();
            buf.limit(buf.capacity());
        } catch (IOException e) {
            throw new FileException("Error reading page at position " + position, e);
        }
        actualBuffer = buf;
        currentBuffer = bufferToList(buf);
        this.createdAt = Instant.now();
    }


    public ArrayList<Byte> bufferToList(ByteBuffer buffer) {
        ArrayList<Byte> list = new ArrayList<>(buffer.remaining());
        buffer.rewind();
        while (buffer.hasRemaining()) {
            list.add(buffer.get());
        }
        return list;
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
    public List<Byte> readPage() {
        return currentBuffer;
    }

    @Override
    public void savePage() {
        // todo
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
    public ByteBlock doChanges(ByteBlock block) {
        switch (block.getType()){
            case DELETE:
                byte deletedData = doDelete(block);
                return new ByteBlock(block.getIndex(), deletedData, FileEventType.INSERT, block.getPageIndex());

            case UPDATE:
                byte updatedData = doUpdate(block);
                return new ByteBlock(block.getIndex(), updatedData, FileEventType.UPDATE, block.getPageIndex());

            case INSERT:
                doInsert(block);
                return new ByteBlock(block.getIndex(), FileEventType.DELETE, block.getPageIndex());
        }
        return block;
    }

    @Override
    public Transaction doChanges(Transaction transaction) {
        switch (transaction.getType()){
            case DELETE:
                byte[] deletedData = doDelete(transaction);
                return new Transaction(
                        deletedData,
                        transaction.getStart(),
                        transaction.getEnd(),
                        FileEventType.INSERT,
                        transaction.getPageIndex());
            case UPDATE:
                byte[] updatedData = doUpdate(transaction);
                return new Transaction(
                        updatedData,
                        transaction.getStart(),
                        transaction.getEnd(),
                        FileEventType.UPDATE,
                        transaction.getPageIndex()
                );
            case INSERT:
                doInsert(transaction);
                return new Transaction(
                        new byte[]{},
                        transaction.getStart(),
                        transaction.getEnd(),
                        FileEventType.DELETE,
                        transaction.getPageIndex()
                );
        }
        return transaction;
    }
}
