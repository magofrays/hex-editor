package hex.editor.gui.grid.listener;

import hex.editor.file.controller.FileController;
import hex.editor.file.event.ByteBlock;
import hex.editor.file.event.FileEventType;
import hex.editor.file.event.HistoryEvent;
import hex.editor.file.event.SaveEvent;

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
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
            fileController.processEvent(new SaveEvent());
            e.consume();
            return;
        }


        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            fileController.processEvent(new ByteBlock(position, FileEventType.DELETE, 0));
            table.repaint();
            e.consume();

        }
        else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            fileController.processEvent(new ByteBlock(position, FileEventType.DELETE, 0));

            int newCol = col - 1;
            int newRow = row;
            if (newCol < 0) {
                newCol = table.getColumnCount() - 1;
                newRow = row - 1;
            }

            if (newRow >= 0 && newCol >= 0) {
                table.changeSelection(newRow, newCol, false, false);
            }
            table.repaint();
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            fileController.processEvent(new ByteBlock(position, (byte) 0,FileEventType.INSERT, 0));
            table.repaint();
            e.consume();
        }
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
            fileController.processEvent(HistoryEvent.UNDO);
            table.repaint();
            e.consume();
        }
        if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_Z) {
            fileController.processEvent(HistoryEvent.REDO);
            table.repaint();
            e.consume();
        }
    }
}
