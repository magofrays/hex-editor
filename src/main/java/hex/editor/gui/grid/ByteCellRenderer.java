package hex.editor.gui.grid;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ByteCellRenderer extends DefaultTableCellRenderer {

    private boolean showHex = false;


    public ByteCellRenderer() {
        Font monospacedFont = new Font("Monospaced", Font.PLAIN, 16);
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
            } else {
                setBackground(Color.WHITE);
            }
        }

        return this;
    }




    public void setShowHex(boolean showHex) {
        this.showHex = showHex;
    }


}