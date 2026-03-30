package hex.editor.gui.grid;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.util.List;

public class ByteGrid extends JTable {
    public ByteGrid(TableModel model){
        super(model);
        initUI();
    }

    public void initUI(){
        setRowSelectionAllowed(false);
        TableColumnModel cm = getColumnModel();
        setCellSelectionEnabled(true);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cm.setColumnSelectionAllowed(true);
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
