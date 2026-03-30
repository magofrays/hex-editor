package hex.editor.gui.grid;

import hex.editor.config.HexEditorConfig;
import hex.editor.file.controller.FileController;
import hex.editor.file.event.ByteBlock;
import hex.editor.file.event.FileEventType;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ByteGridModel extends AbstractTableModel {
    private List<Byte> flatData;
    private final int tableWidth;
    private final FileController fileController;

    public ByteGridModel(List<Byte> byteList, FileController fileController){
        flatData = byteList;
        tableWidth = HexEditorConfig.getInstance().getInteger("editor.table.width");
        this.fileController = fileController;
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

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (value instanceof Byte) {
            int position = rowIndex * tableWidth + columnIndex;
            byte newValue = (Byte) value;

            fileController.processEvent(new ByteBlock(position, newValue,FileEventType.UPDATE,0));
        }
    }
}
