package hex.editor.gui.grid;

import hex.editor.adapter.PageChanger;
import hex.editor.file.controller.FileController;
import hex.editor.gui.grid.model.ByteGridModel;

import javax.swing.*;

public class BytePage extends JPanel {
    ByteGrid byteGrid;
    ByteGridModel byteGridModel;
    ByteCellRenderer byteCellRenderer;
    ByteCellEditor byteCellEditor;
    PageChanger pageChanger;

    public BytePage(FileController fileController, Long position){
        this.pageChanger = new PageChanger(fileController, position);
        this.byteGridModel = new ByteGridModel(pageChanger);
        this.byteGrid = new ByteGrid(byteGridModel, pageChanger);
        this.byteCellRenderer = new ByteCellRenderer();
        byteGrid.setDefaultRenderer(Byte.class, byteCellRenderer);

    }
    public ByteGrid getGrid(){
        return byteGrid;
    }
}
