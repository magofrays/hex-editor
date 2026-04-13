package hex.editor.file.controller;

import hex.editor.exception.FileException;
import hex.editor.file.FileChanger;
import hex.editor.file.FileHolder;
import hex.editor.file.FileViewer;
import hex.editor.file.dto.PageResult;
import hex.editor.file.event.FileEvent;
import hex.editor.file.event.HistoryEvent;
import hex.editor.file.event.ChangeEvent;
import hex.editor.file.event.SaveEvent;
import hex.editor.file.history.FileHistory;
import hex.editor.file.history.FileHistoryImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileController {

    FileChanger fileChanger;
    FileViewer fileViewer;
    FileHistory fileHistory;

    List<FileListener> fileListeners;

    public FileController(Path path) throws IOException {
        FileHolder fileHolder = new FileHolder(path);
        fileChanger = fileHolder;
        fileViewer = fileHolder;
        fileHistory = new FileHistoryImpl(fileChanger);
        fileListeners = new ArrayList<>();
    }

    public void processEvent(FileEvent fileEvent){
        if(fileEvent instanceof ChangeEvent){
            ChangeEvent changes = fileChanger.doChanges((ChangeEvent) fileEvent);
            fileHistory.collectEvent(changes);
        }
        if(fileEvent instanceof HistoryEvent){
            HistoryEvent historyEvent = (HistoryEvent) fileEvent;
            switch (historyEvent){
                case UNDO:
                    fileHistory.undoChanges();
                    break;
                case REDO:
                    fileHistory.redoChanges();
                    break;
            }
        }
        if(fileEvent instanceof SaveEvent){
            try {
                fileChanger.saveFile();
            } catch (IOException e) {
                throw new FileException("File is corrupted!", e.getMessage());
            }
        }
    }
    public PageResult getPage(Long position){
        return fileViewer.viewFile(position);
    }

    public void setPageSize(int width, int height){
        processEvent(new SaveEvent());
        fileChanger.setPageSize(width, height);
    }

    public Long getFileSize() {
        return fileViewer.getFileSize();
    }
}
