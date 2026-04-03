package hex.editor.gui.grid;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ByteCellEditor extends AbstractCellEditor implements TableCellEditor {
    private JTextField field;
    private Byte currentValue;
    private JTable table;
    private int currentRow;
    private int currentColumn;

    public ByteCellEditor() {
        field = new JTextField();
        field.setFont(new Font("Courier new", Font.BOLD, 20));
        field.setBackground(Color.BLUE);
        field.setHorizontalAlignment(JTextField.CENTER);

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                    e.consume();
                    return;
                }

                String text = field.getText();
                if (text.length() >= 2) {
                    e.consume();
                    char upperChar = Character.toUpperCase(c);
                    stopCellEditing();
                    moveToNextCell();

                    SwingUtilities.invokeLater(() -> {
                        if (table != null && table.isEditing()) {
                            Component editor = table.getEditorComponent();
                            if (editor instanceof JTextField) {
                                JTextField nextField = (JTextField) editor;
                                nextField.setText(String.valueOf(upperChar));
                                nextField.setCaretPosition(1);
                                nextField.selectAll();
                            }
                        }
                    });
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    stopCellEditing();
                    moveToNextCell();
                }
            }
        });
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

    private void moveToNextCell() {
        if (table == null) return;

        int nextRow = currentRow;
        int nextColumn = currentColumn + 1;

        if (nextColumn >= table.getColumnCount()) {
            nextColumn = 0;
            nextRow++;
        }

        if (nextRow < table.getRowCount()) {
            table.changeSelection(nextRow, nextColumn, false, false);
            table.editCellAt(nextRow, nextColumn);
            Component editor = table.getEditorComponent();
            if (editor != null) {
                editor.requestFocusInWindow();
            }
        } else {
            table.clearSelection();
            table.requestFocusInWindow();
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

        SwingUtilities.invokeLater(() -> {
            field.selectAll();
            field.requestFocusInWindow();
        });

        return field;
    }

    @Override
    public boolean stopCellEditing() {
        setValueFromText();
        fireEditingStopped();
        return true;
    }

    @Override
    public void cancelCellEditing() {
        super.cancelCellEditing();
    }
}