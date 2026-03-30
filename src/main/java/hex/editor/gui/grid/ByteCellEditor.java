package hex.editor.gui.grid;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ByteCellEditor extends AbstractCellEditor implements TableCellEditor {
    private JTextField field;

    public ByteCellEditor() {
        field = new JTextField();
        field.addActionListener(e -> stopCellEditing());
    }
    @Override
    public Object getCellEditorValue() {
        return Integer.parseInt(field.getText());
    }


    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        field.setText(value.toString());
        return field;
    }
}
