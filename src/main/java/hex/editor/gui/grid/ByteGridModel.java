package hex.editor.gui.grid;

import hex.editor.config.HexEditorConfig;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ByteGridModel extends AbstractTableModel {
    private List<Byte> flatData;
    private final int tableWidth;

    public ByteGridModel(List<Byte> byteList){
        flatData = byteList;
        tableWidth = HexEditorConfig.getInstance().getInteger("editor.table.width");

    }

    @Override
    public int getRowCount() {
        return flatData.size() / tableWidth;
    }

    @Override
    public int getColumnCount() {
        return tableWidth;
    }

    @Override
    public Byte getValueAt(int rowIndex, int columnIndex) {
        int index = rowIndex * tableWidth + columnIndex;
        if(index > flatData.size()){
            return (byte) 0;
        }
        return flatData.get(index);
    }

    @Override
    public String getColumnName(int column) {
        return String.valueOf(column);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }
    @Override
    public Class<?> getColumnClass(int column) {
        return Byte.class;
    }


}
