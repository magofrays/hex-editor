package hex.editor.gui.grid;

import hex.editor.adapter.PageChanger;
import hex.editor.file.controller.FileController;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class ByteGrid extends JTable {

    PageChanger pageChanger;

    public ByteGrid(TableModel model){
        super(model);
        initUI();
    }

    public void initUI(){
        TableColumnModel cm = getColumnModel();
        setCellSelectionEnabled(true);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cm.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(60);
            getColumnModel().getColumn(i).setMinWidth(50);
            getColumnModel().getColumn(i).setMaxWidth(100);
        }
        setRowHeight(50);
    }
}
