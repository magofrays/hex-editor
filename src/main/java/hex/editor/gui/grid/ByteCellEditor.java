package hex.editor.gui.grid;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ByteCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JTextField field;
    private Byte currentValue;
    private JTable table;
    private int currentRow;
    private int currentColumn;

    public ByteCellEditor() {
        field = new JTextField();
        field.setFont(new Font("Courier new", Font.BOLD, 15));
        field.setBackground(Color.BLUE);
        field.setHorizontalAlignment(JTextField.CENTER);
    }


    @Override
    public Object getCellEditorValue() {
        return currentValue;
    }

    private void setValueFromText() {
        String text = field.getText();
        try {
            if (text.length() == 1) {
                currentValue = (byte) Integer.parseInt(text, 16);
            } else if (text.length() >= 2) {
                currentValue = (byte) Integer.parseInt(text.substring(0, 2), 16);
            } else {
                currentValue = 0;
            }
        } catch (NumberFormatException e) {
            currentValue = 0;
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        this.table = table;
        this.currentRow = row;
        this.currentColumn = column;

        if (value instanceof Byte) {
            currentValue = (Byte) value;
            field.setText(String.format("%02X", currentValue));
        } else {
            currentValue = 0;
            field.setText("00");
        }
        field.selectAll();
        field.requestFocusInWindow();
        return field;
    }

    @Override
    public boolean stopCellEditing() {
        setValueFromText();
        fireEditingStopped();
        return true;
    }


}