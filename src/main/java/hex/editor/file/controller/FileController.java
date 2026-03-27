package hex.editor.file.controller;

import hex.editor.file.FileChanger;
import hex.editor.file.FileHolder;
import hex.editor.file.FileViewer;
import hex.editor.file.event.FileEvent;
import hex.editor.file.event.HistoryEvent;
import hex.editor.file.event.ChangeEvent;
import hex.editor.file.event.SaveEvent;
import hex.editor.file.history.FileHistory;
import hex.editor.file.history.FileHistoryImpl;

import java.io.IOException;
import java.util.List;

public class FileController {

    FileChanger fileChanger;
    FileViewer fileViewer;
    FileHistory fileHistory;

    public FileController(String path) throws IOException {
        FileHolder fileHolder = new FileHolder(path);
        fileChanger = fileHolder;
        fileViewer = fileHolder;
        fileHistory = new FileHistoryImpl(fileChanger);
    }

    public void processEvent(FileEvent fileEvent){
        if(fileEvent instanceof ChangeEvent){
            fileChanger.doChanges((ChangeEvent) fileEvent);
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
            fileChanger.saveFile();
        }
    }
    public List<Byte> getPage(Long position){
        return fileViewer.viewFile(position);
    }


}
