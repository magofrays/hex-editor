package hex.editor.gui.grid;

import hex.editor.adapter.PageChanger;
import hex.editor.file.controller.FileController;
import hex.editor.gui.grid.model.ByteGridModel;
import hex.editor.viewer.ByteViewer;

import javax.swing.*;
import java.awt.Component;
import java.util.List;

public class BytePage extends JPanel {
    ByteGrid byteGrid;
    ByteGridModel byteGridModel;
    ByteCellRenderer byteCellRenderer;
    ByteCellEditor byteCellEditor;
    PageChanger pageChanger;
    JPanel gridContent;
    Integer tableWidth;
    Integer tableHeight;

    public BytePage(ByteViewer byteViewer, FileController fileController, Long position, Integer tableWidth, Integer tableHeight){
        this.tableHeight = tableHeight;
        this.tableWidth = tableWidth;
        this.pageChanger = new PageChanger(fileController, position);
        this.byteGridModel = new ByteGridModel(pageChanger, tableWidth, tableHeight);
        gridContent = new JPanel();
        gridContent.setLayout(new BoxLayout(gridContent, BoxLayout.X_AXIS));

        this.byteGrid = new ByteGrid(byteGridModel, pageChanger, byteViewer);
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

    public List<Byte> getData(){
        return pageChanger.getData();
    }

    public void selectRange(int start, int end){
        byteGrid.selectRange(start, end);
    }
}
