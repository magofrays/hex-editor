package hex.editor.gui.grid;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ByteCellRenderer extends DefaultTableCellRenderer {

    private boolean showHex = true;


    public ByteCellRenderer() {
        Font monospacedFont = new Font("Courier new", Font.BOLD, 15);
        setHorizontalAlignment(CENTER);
        setFont(monospacedFont);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (value instanceof Byte) {
            byte byteValue = (Byte) value;
            int unsignedValue = byteValue & 0xFF;

            if (showHex) {
                setText(String.format("%02X", unsignedValue));
            } else {
                setText(String.format("%3d", unsignedValue));
            }

            if (isSelected) {
                setBackground(Color.BLUE);
                setForeground(Color.WHITE);
            } else if (unsignedValue == 0) {
                setBackground(Color.DARK_GRAY);
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
            }
        } else {
            setBackground(Color.RED);
            setText("");
        }

        return this;
    }


}