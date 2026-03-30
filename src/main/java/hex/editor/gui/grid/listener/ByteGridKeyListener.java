package hex.editor.gui.grid.listener;

import hex.editor.file.controller.FileController;
import hex.editor.file.event.ByteBlock;
import hex.editor.file.event.FileEventType;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ByteGridKeyListener extends KeyAdapter {
    private final JTable table;
    private final FileController fileController;

    public ByteGridKeyListener(JTable table, FileController fileController){
        this.table = table;
        this.fileController = fileController;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();

        if (row < 0 || col < 0) return;

        int position = row * table.getColumnCount() + col;

        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            fileController.processEvent(new ByteBlock(position, FileEventType.DELETE, 0));
            table.repaint();
        }
    }
}
