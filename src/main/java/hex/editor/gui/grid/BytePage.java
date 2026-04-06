package hex.editor.gui.grid;

import hex.editor.adapter.PageChanger;
import hex.editor.file.controller.FileController;
import hex.editor.gui.grid.model.ByteGridModel;

import javax.swing.*;
import java.awt.*;

public class BytePage extends JPanel {
    ByteGrid byteGrid;
    ByteGridModel byteGridModel;
    ByteCellRenderer byteCellRenderer;
    ByteCellEditor byteCellEditor;
    PageChanger pageChanger;
    JPanel gridContent;
    Integer tableWidth;
    Integer tableHeight;

    public BytePage(FileController fileController, Long position, Integer tableWidth, Integer tableHeight){
        this.tableHeight = tableHeight;
        this.tableWidth = tableWidth;
        this.pageChanger = new PageChanger(fileController, position);
        this.byteGridModel = new ByteGridModel(pageChanger, tableWidth, tableHeight);
        gridContent = new JPanel();
        gridContent.setLayout(new BoxLayout(gridContent, BoxLayout.X_AXIS));

        this.byteGrid = new ByteGrid(byteGridModel, pageChanger);
        int rows = byteGrid.getRowCount();
        int columns = byteGrid.getColumnCount();
        gridContent.add(this.byteGrid);
        gridContent.add(new RowHeaderList(rows, columns, position));
        this.byteCellRenderer = new ByteCellRenderer();
        this.byteCellEditor = new ByteCellEditor();
        byteGrid.setDefaultRenderer(Byte.class, byteCellRenderer);
        byteGrid.setDefaultEditor(Byte.class, byteCellEditor);
    }
    public Component getGrid(){
        return gridContent;
    }
}
