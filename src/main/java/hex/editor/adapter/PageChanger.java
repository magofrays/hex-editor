package hex.editor.adapter;

import hex.editor.file.controller.FileController;
import hex.editor.file.dto.PageResult;
import hex.editor.file.event.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
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

    public void copy(byte[] data){
        StringBuilder sb = new StringBuilder();
        for(byte b : data){
            sb.append(String.format("%02X", b)).append(" ");
        }
        StringSelection selection = new StringSelection(sb.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    public void paste(Integer start, boolean insert){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable t = clipboard.getContents(null);

        if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                String[] data = text.split(" ");
                byte[] bytes = new byte[data.length];
                for (int i = 0; i < data.length; i++) {
                    bytes[i] = (byte) Integer.parseInt(data[i].trim(), 16);
                }
                if(insert) insert(start, bytes);
                else update(start, bytes);
            } catch (UnsupportedFlavorException | IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
