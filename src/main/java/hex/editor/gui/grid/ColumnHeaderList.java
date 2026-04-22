package hex.editor.gui.grid;

import hex.editor.config.HexEditorConfig;

import javax.swing.*;
import java.awt.*;

public class ColumnHeaderList extends JList<String> {

    private final int cellWidth = HexEditorConfig.getInstance().getInteger("editor.cell.width");
    private final int cellHeight = HexEditorConfig.getInstance().getInteger("editor.cell.height");
    private DefaultListModel<String> model;

    public ColumnHeaderList(int columns) {
        model = new DefaultListModel<>();
        for (int col = 0; col < columns; col++) {
            model.addElement(String.format("%02X", col));
        }

        setModel(model);
        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(1);

        setFont(new Font("Monospaced", Font.BOLD, 12));
        setFixedCellWidth(cellWidth);
        setFixedCellHeight(cellHeight);
        setBackground(Color.LIGHT_GRAY);
        setForeground(Color.BLACK);
        setFocusable(false);

        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(new Font("Monospaced", Font.BOLD, 12));
                label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                label.setBackground(Color.LIGHT_GRAY);
                label.setForeground(Color.BLACK);
                label.setOpaque(true);
                return label;
            }
        };
        setCellRenderer(renderer);
    }

}