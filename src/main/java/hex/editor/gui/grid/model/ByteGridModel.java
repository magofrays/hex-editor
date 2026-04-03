package hex.editor.gui.grid.model;

import hex.editor.adapter.PageChanger;
import hex.editor.config.HexEditorConfig;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ByteGridModel extends AbstractTableModel {
    private List<Byte> flatData;
    private final int tableWidth;
    private final int tableHeight;
    private final PageChanger pageChanger;


    public ByteGridModel(PageChanger pageChanger){
        this.pageChanger = pageChanger;
        flatData = pageChanger.getData();
        tableWidth = HexEditorConfig.getInstance().getInteger("editor.table.width");
        tableHeight = HexEditorConfig.getInstance().getInteger("editor.table.height");
    }

    @Override
    public int getRowCount() {
        return (flatData.size()+ tableWidth -1) / tableWidth;
    }

    @Override
    public int getColumnCount() {
        return tableWidth;
    }

    @Override
    public Byte getValueAt(int rowIndex, int columnIndex) {
        int index = rowIndex * tableWidth + columnIndex;
        if(index >= flatData.size()){
            return null;
        }
        return flatData.get(index);
    }

    @Override
    public String getColumnName(int column) {
        return String.valueOf(column);
    }


    @Override
    public boolean isCellEditable(int row, int column) {
        return row * tableWidth + column < flatData.size();
    }
    @Override
    public Class<?> getColumnClass(int column) {
        return Byte.class;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (value instanceof Byte) {
            int position = rowIndex * tableWidth + columnIndex;
            byte newValue = (Byte) value;
            pageChanger.update(position, newValue);
        }
    }
}
