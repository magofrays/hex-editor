package hex.editor.gui.grid;


import hex.editor.config.HexEditorConfig;

import javax.swing.*;
import java.awt.*;

public class RowHeaderList extends JList<String> {

    private final int bytesPerRow;
    private final int cellHeight = HexEditorConfig.getInstance().getInteger("editor.cell.height");
    private final int cellWidth = 70;
    private DefaultListModel<String> model;

    public RowHeaderList(int rows, int bytesPerRow, long startOffset) {
        this.bytesPerRow = bytesPerRow;

        model = new DefaultListModel<>();
        for (int row = 0; row < rows; row++) {
            long offset = startOffset + ((long) row * bytesPerRow);
            model.addElement(String.format("%08X", offset));
        }

        setModel(model);

        setFont(new Font("Monospaced", Font.PLAIN, 12));
        setFixedCellHeight(cellHeight);
        setFixedCellWidth(cellWidth);
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