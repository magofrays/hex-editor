package hex.editor.file.controller;

import hex.editor.file.FileChanger;
import hex.editor.file.FileHolder;
import hex.editor.file.FileViewer;
import hex.editor.file.event.FileEvent;
import hex.editor.file.history.FileHistory;
import hex.editor.file.history.FileHistoryImpl;

import java.io.IOException;

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
        // todo
    }


}
