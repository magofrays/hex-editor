package hex.editor.adapter;

import hex.editor.file.controller.FileController;
import hex.editor.file.dto.PageResult;
import hex.editor.file.event.*;

import java.util.List;

public class PageChanger {
    FileController fileController;
    PageResult pageResult;

    public PageChanger(FileController fileController, Long position){
        this.fileController = fileController;
        pageResult = fileController.getPage(position);
    }

    public List<Byte> getData(){
        return pageResult.getData();
    }

    public void insert(Integer index, byte data){
        fileController.processEvent(new ByteBlock(index, data, FileEventType.INSERT, pageResult.getPageIndex()));
    }

    public void insert(Integer start, byte[] data){
        fileController.processEvent(new Transaction(data, start, start+data.length, FileEventType.INSERT, pageResult.getPageIndex()));
    }

    public void update(Integer index, byte data){
        fileController.processEvent(new ByteBlock(index, data, FileEventType.UPDATE, pageResult.getPageIndex()));
    }

    public void update(Integer start, byte[] data){
        fileController.processEvent(new Transaction(data, start, start+data.length, FileEventType.UPDATE, pageResult.getPageIndex()));
    }

    public void delete(Integer index){
        fileController.processEvent(new ByteBlock(index, FileEventType.DELETE, pageResult.getPageIndex()));
    }

    public void delete(Integer start, Integer end){
        fileController.processEvent(new Transaction(new byte[]{}, start, end, FileEventType.DELETE, pageResult.getPageIndex()));
    }


    public void save(){
        fileController.processEvent(new SaveEvent());
    }

    public void undo(){
        fileController.processEvent(HistoryEvent.UNDO);
    }

    public void redo() {
        fileController.processEvent(HistoryEvent.REDO);
    }
}
