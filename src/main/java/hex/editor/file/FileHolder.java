package hex.editor.file;

import hex.editor.config.HexEditorConfig;
import hex.editor.exception.FileException;
import hex.editor.file.event.ChangeEvent;
import hex.editor.file.page.FilePage;
import hex.editor.file.page.PageOperations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileHolder implements FileChanger, FileViewer{
    final TreeMap<Integer, PageOperations> pages;
    final Path filePath;
    final FileChannel fileChannel;
    Long fileSize;
    final Integer pageSize;
    final Long lastPageIndex;

    public FileHolder(Path filePath) throws IOException {
        this.filePath = filePath;
        if (Files.notExists(this.filePath)) {
            throw new FileNotFoundException("fileSource: " + filePath + " not found");
        }
        this.fileSize = Files.size(this.filePath);
        this.fileChannel = FileChannel.open(this.filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);
        pages = new TreeMap<>();
        pageSize = HexEditorConfig.getInstance().getInteger("file.page.size");
        lastPageIndex = fileSize/pageSize;
    }


    public Integer getPageSize(){
        return pageSize;
    }

    public Long getFileSize() {
        return fileSize;
    }


    public PageOperations getPage(Long pagePosition){
        Integer index = Math.toIntExact((pagePosition / pageSize));
        long startRead = (long) index *pageSize;
        PageOperations page = pages.get(index);
        if (page != null) {
            return page;
        }
        FilePage newPage = new FilePage(fileChannel, startRead);
        newPage.loadPage(pageSize, fileSize);
        pages.put(index, newPage);
        return newPage;
    }

    public List<Byte> viewFile(Long position){
        PageOperations page = getPage(position);
        return page.readPage();
    }

    @Override
    public ChangeEvent doChanges(ChangeEvent event){
        Integer index = event.getPageIndex();
        FileChanger page = pages.get(index);
        return page.doChanges(event);
    }

    @Override
    public void saveFile() throws IOException {
        for (PageOperations page : pages.descendingMap().values()){
            page.savePage();
        }
        fileSize = Files.size(this.filePath);
        for(PageOperations page : pages.values()){
            page.loadPage(pageSize,fileSize);
        }
    }

}
