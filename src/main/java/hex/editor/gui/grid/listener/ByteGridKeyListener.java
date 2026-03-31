package hex.editor.gui.grid.listener;

import hex.editor.adapter.PageChanger;
import hex.editor.gui.grid.ByteGrid;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ByteGridKeyListener extends KeyAdapter {
    private final JTable table;
    private final PageChanger pageChanger;

    public ByteGridKeyListener(ByteGrid grid, PageChanger pageChanger){
        this.table = grid;
        this.pageChanger = pageChanger;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int row = table.getSelectedRow();
        int colStart = table.getSelectedColumn();
        int colEnd = colStart + table.getSelectedColumnCount();

        if (row < 0 || colStart < 0) return;

        int start = row * table.getColumnCount() + colStart;
        int end = row * table.getColumnCount() + colEnd;
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
            pageChanger.save();
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            if(colEnd-colStart == 1){
                pageChanger.delete(start);
            } else {
                pageChanger.delete(start, end);
            }
            table.repaint();
            e.consume();

        }
        else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            pageChanger.delete(start);
            int newCol = colStart - 1;
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
        else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
            pageChanger.undo();
            table.repaint();
            e.consume();
        }
        else if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_Z) {
            pageChanger.redo();
            table.repaint();
            e.consume();
        }

        else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C){
            StringBuilder data = new StringBuilder();
            for(int i = start; i != end; i++){
                data.append(((Byte) table.getValueAt(row, i)).toString());
                if(i != end-1){
                    data.append(",");
                }
            }
            StringSelection selection = new StringSelection(data.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        }
        else if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V){
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable t = clipboard.getContents(null);

            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                    String[] data = text.split("\t");
                    byte[] bytes = new byte[data.length];
                    for (int i = 0; i < data.length; i++) {
                        bytes[i] = Byte.parseByte(data[i].trim());
                    }
                    pageChanger.update(start, bytes);
                    table.repaint();
                } catch (UnsupportedFlavorException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
