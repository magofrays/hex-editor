package hex.editor.file;

import hex.editor.config.HexEditorConfig;
import hex.editor.exception.FileException;
import hex.editor.file.history.ByteBlock;
import hex.editor.file.history.FileHistory;
import hex.editor.file.history.Transaction;
import hex.editor.file.page.FilePage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.TreeMap;

public class FileHolder implements FileChanger {
    Map<Long, FileChanger> pages = new TreeMap<>();
    final Path filePath;
    final FileChannel fileChannel;
    final FileHistory history;
    Long fileSize;
    final Integer pageSize = HexEditorConfig.getInstance().getInteger("file.page.size");
    final Long lastPageIndex;

    public FileHolder(String filePath) throws IOException {
        this.filePath = Paths.get(filePath);
        if (Files.notExists(this.filePath)) {
            throw new FileNotFoundException("fileSource: " + filePath + " not found");
        }
        this.history = new FileHistory();
        this.fileSize = Files.size(this.filePath);
        this.fileChannel = FileChannel.open(this.filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);
        lastPageIndex = fileSize/pageSize;
    }

    public FileHistory getHistory() {
        return history;
    }

    public Integer getPageSize(){
        return pageSize;
    }

    public Long getFileSize() {
        return fileSize;
    }


    public FileChanger getPage(Long pagePosition){
        Long index = (pagePosition/pageSize);
        long startRead = index*pageSize;
        FileChanger page = pages.get(index);
        if (page != null) {
            return page;
        }
        FilePage newPage = new FilePage(this, startRead);
        ByteBuffer buf = ByteBuffer.allocate(Math.toIntExact(pageSize));
        try {
            fileChannel.position(startRead);
            fileChannel.read(buf);
            buf.flip();
            buf.limit(buf.capacity());
        } catch (IOException e) {
            throw new FileException("Error reading page: " + index, e);
        }
        newPage.setBuffer(buf);
        pages.put(index, newPage);
        return newPage;
    }

    @Override
    public void doChanges(ByteBlock block){
        Long index = block.getPageIndex();
        FileChanger page = pages.get(index);
        page.doChanges(block);
    }

    @Override
    public void doChanges(Transaction transaction) {
        Long index = transaction.getPageIndex();
        FileChanger page = pages.get(index);
        page.doChanges(transaction);
    }


}
