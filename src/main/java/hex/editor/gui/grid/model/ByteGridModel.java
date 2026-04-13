package hex.editor.gui.grid.model;

import hex.editor.adapter.PageChanger;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ByteGridModel extends AbstractTableModel {
    private final List<Byte> flatData;
    private final int tableWidth;
    private final PageChanger pageChanger;


    public ByteGridModel(PageChanger pageChanger, Integer tableWidth, Integer tableHeight) {
        this.pageChanger = pageChanger;
        flatData = pageChanger.getData();
        this.tableWidth = tableWidth;
    }

    @Override
    public int getRowCount() {
        return (flatData.size() + tableWidth - 1) / tableWidth;
    }

    @Override
    public int getColumnCount() {
        return tableWidth;
    }

    @Override
    public Byte getValueAt(int rowIndex, int columnIndex) {
        int index = rowIndex * tableWidth + columnIndex;
        if (index >= flatData.size()) {
            return null;
        }
        return flatData.get(index);
    }

    @Override
    public String getColumnName(int column) {
        return String.format("%02X", column);
    }


    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 0 || row * tableWidth + column < flatData.size();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return Byte.class;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            return;
        }
        if (value instanceof Byte) {
            int position = rowIndex * tableWidth + columnIndex;
            byte newValue = (Byte) value;
            pageChanger.update(position, newValue);
        }
    }
}
