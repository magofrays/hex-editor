package hex.editor.gui.grid;

import hex.editor.adapter.PageChanger;
import hex.editor.file.controller.FileController;
import hex.editor.gui.grid.listener.ByteGridKeyListener;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BytePage {
    ByteGrid byteGrid;
    ByteGridModel byteGridModel;
    ByteCellRenderer byteCellRenderer;
    ByteCellEditor byteCellEditor;
    PageChanger pageChanger;
    ByteGridKeyListener byteGridKeyListener;

    public BytePage(FileController fileController, Long position){
        this.pageChanger = new PageChanger(fileController, position);
        this.byteGridModel = new ByteGridModel(pageChanger);
        this.byteGrid = new ByteGrid(byteGridModel);
        this.byteGridKeyListener = new ByteGridKeyListener(byteGrid, pageChanger);
        this.byteCellEditor = new ByteCellEditor();
        this.byteCellRenderer = new ByteCellRenderer();
        byteGrid.addKeyListener(byteGridKeyListener);
        byteGrid.setDefaultRenderer(Byte.class, byteCellRenderer);
        JPopupMenu popup = new JPopupMenu();

        JMenuItem removeItem = new JMenuItem("Вырезать (Ctrl+X)");
        JMenuItem copyItem = new JMenuItem("Копировать (Ctrl+C)");
        JMenuItem pasteItem = new JMenuItem("Вставить (Ctrl+V)");
        JMenuItem saveItem = new JMenuItem("Сохранить (Ctrl+S)");
        popup.add(removeItem);
        popup.add(copyItem);
        popup.add(pasteItem);
        popup.add(saveItem);

        byteGrid.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

    }
    public ByteGrid getGrid(){
        return byteGrid;
    }
}
