package hex.editor.file.page;

import hex.editor.exception.BadFileEventException;
import hex.editor.exception.FileException;
import hex.editor.file.event.ByteBlock;
import hex.editor.file.event.ChangeEvent;
import hex.editor.file.event.FileEventType;
import hex.editor.file.event.Transaction;

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
    Boolean isLoaded;
    Instant updatedAt;
    FileChannel fileChannel;
    Integer pageSize;
    Long fileSize;
    public FilePage(FileChannel fileChannel, Long position) {
        currentBuffer = new ArrayList<>();
        isSaved = true;
        isLoaded = false;
        this.position = position;
        this.fileChannel = fileChannel;
    }

    public void loadPage(Integer pageSize, Long fileSize){
        this.pageSize = pageSize;
        this.fileSize = fileSize;
        int bufferSize = Math.min(pageSize, Math.toIntExact(fileSize - position));
        if(bufferSize < 0){
            throw new FileException("Error reading page at position " + position, "File size is " + fileSize);
        }
        ByteBuffer buf = ByteBuffer.allocate(bufferSize);
        try {
            this.fileChannel.position(position);
            this.fileChannel.read(buf);
            buf.flip();
            buf.limit(buf.capacity());
        } catch (IOException e) {
            throw new FileException("Error reading page at position " + position, e.getMessage());
        }
        actualBuffer = buf;
        currentBuffer.clear();
        currentBuffer.addAll(bufferToList(buf));
        isLoaded = true;
        this.updatedAt = Instant.now();
    }

    @Override
    public Integer getIndex() {
        if(!isLoaded) return 0;
        return Math.toIntExact(position / pageSize);
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
        if(isSaved){
            return;
        }
        try {
            int bufSize = Math.min(pageSize, Math.toIntExact(fileSize-position));
            bufSize = Math.max(currentBuffer.size(), bufSize);
            ByteBuffer bufferToSave = ByteBuffer.allocate(bufSize);
            for (int i = 0; i < Math.min(currentBuffer.size(), pageSize); i++) {
                bufferToSave.put(currentBuffer.get(i));
            }
            for(int i = currentBuffer.size(); i < bufSize; i++){
                bufferToSave.put((byte) 0);
            }
            bufferToSave.flip();
            fileChannel.position(position);
            int bytesWritten = fileChannel.write(bufferToSave);
            isSaved = true;
            updatedAt = Instant.now();

        } catch (IOException e) {
            throw new FileException("Error saving page at position " + position, e.getMessage());
        }
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
            currentBuffer.set(startIndex + i, transaction.getData().get(i));
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
        List<Byte> data = transaction.getData();
        currentBuffer.addAll(startIndex, data);
    }

    @Override
    public void doInsert(ByteBlock block) {
        isSaved = false;
        int index = block.getIndex();
        currentBuffer.add(index, block.getByte());
    }

    public ByteBlock doByteBlock(ByteBlock block) {
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

    public Transaction doTransaction(Transaction transaction) {
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

    @Override
    public ChangeEvent doChanges(ChangeEvent event) {
        if(event instanceof ByteBlock){
            return doByteBlock((ByteBlock) event);
        }
        if(event instanceof Transaction){
            return doTransaction((Transaction) event);
        }
        throw new BadFileEventException("Bad Event: ", event);
    }
}
