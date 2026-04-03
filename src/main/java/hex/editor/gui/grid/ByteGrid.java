package hex.editor.gui.grid;

import hex.editor.adapter.PageChanger;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ByteGrid extends JTable {

    PageChanger pageChanger;

    public ByteGrid(TableModel model, PageChanger pageChanger){
        super(model);
        this.pageChanger = pageChanger;
        initUI();
        JPopupMenu popup = getJPopupMenu();
        addMouseListener(new MouseAdapter() {
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
        setupKeyBindings();
    }

    private JPopupMenu getJPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem removeItem = new JMenuItem("Вырезать (Ctrl+X)");
        removeItem.addActionListener(e -> {
            int row = getSelectedRow();
            int colStart = getSelectedColumn();
            int colEnd = colStart + getSelectedColumnCount();
            if (row < 0 || colStart < 0) return;
            int columnCount = getColumnCount();
            int start = row * columnCount + colStart;
            int end = row * columnCount + colEnd;
            byte[] data = new byte[end-start];
            for(int i = 0; i != end-start; i++){
                data[i] = (byte) getValueAt(row, colStart+i);
            }
            pageChanger.copy(data);
            pageChanger.delete(start, end);
            repaint();
        });
        JMenuItem copyItem = new JMenuItem("Копировать (Ctrl+C)");
        copyItem.addActionListener(e -> {
            int row = getSelectedRow();
            int colStart = getSelectedColumn();
            int colEnd = colStart + getSelectedColumnCount();
            if (row < 0 || colStart < 0) return;
            int columnCount = getColumnCount();
            int start = row * columnCount + colStart;
            int end = row * columnCount + colEnd;
            byte[] data = new byte[end-start];
            for(int i = 0; i != end-start; i++){
                data[i] = (byte) getValueAt(row, colStart+i);
            }
            pageChanger.copy(data);
        });
        JMenuItem pasteItem = new JMenuItem("Вставить (Ctrl+V)");
        pasteItem.addActionListener(e -> {
            int row = getSelectedRow();
            int colStart = getSelectedColumn();
            int columnCount = getColumnCount();
            int start = row * columnCount + colStart;
            pageChanger.paste(start, false);
            repaint();
        });
        JMenuItem pasteWithInsertItem = new JMenuItem("Вставить со смещением (Ctrl+Shift+V)");
        pasteWithInsertItem.addActionListener(e -> {
            int row = getSelectedRow();
            int colStart = getSelectedColumn();
            int columnCount = getColumnCount();
            int start = row * columnCount + colStart;
            pageChanger.paste(start, true);
            repaint();
        });
        JMenuItem saveItem = new JMenuItem("Сохранить (Ctrl+S)");
        saveItem.addActionListener(e -> {
            pageChanger.save();
            repaint();
        });
        popup.add(removeItem);
        popup.add(copyItem);
        popup.add(pasteItem);
        popup.add(pasteWithInsertItem);
        popup.add(saveItem);
        return popup;

    }

    public void initUI(){
        setCellSelectionEnabled(true);
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(60);
            getColumnModel().getColumn(i).setMinWidth(50);
            getColumnModel().getColumn(i).setMaxWidth(100);
        }
        setRowHeight(50);

    }

    private void setupKeyBindings() {
        ActionMap actionMap = getActionMap();
        InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);

        inputMap.put(KeyStroke.getKeyStroke("ctrl S"), "save");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger != null) {
                    pageChanger.save();
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("DELETE"), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;

                int row = getSelectedRow();
                int colStart = getSelectedColumn();
                int colEnd = colStart + getSelectedColumnCount();

                if (row < 0 || colStart < 0) return;

                int start = row * getColumnCount() + colStart;
                int end = row * getColumnCount() + colEnd;

                if (colEnd - colStart == 1) {
                    pageChanger.delete(start);
                } else {
                    pageChanger.delete(start, end);
                }
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("BACK_SPACE"), "backspace");
        actionMap.put("backspace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;

                int row = getSelectedRow();
                int colStart = getSelectedColumn();

                if (row < 0 || colStart < 0) return;

                int start = row * getColumnCount() + colStart;
                pageChanger.delete(start);

                int newCol = colStart - 1;
                int newRow = row;
                if (newCol < 0) {
                    newCol = getColumnCount() - 1;
                    newRow = row - 1;
                }

                if (newRow >= 0 && newCol >= 0) {
                    changeSelection(newRow, newCol, false, false);
                }
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl Z"), "undo");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger != null) {
                    pageChanger.undo();
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl shift Z"), "redo");
        actionMap.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger != null) {
                    pageChanger.redo();
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl C"), "copy");
        actionMap.put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;

                int row = getSelectedRow();
                int colStart = getSelectedColumn();
                int colEnd = colStart + getSelectedColumnCount();

                if (row < 0 || colStart < 0) return;

                byte[] data = new byte[colEnd - colStart];
                for (int i = 0; i < colEnd - colStart; i++) {
                    data[i] = (byte) getValueAt(row, colStart + i);
                }
                pageChanger.copy(data);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl V"), "paste");
        actionMap.put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;

                int row = getSelectedRow();
                int colStart = getSelectedColumn();

                if (row < 0 || colStart < 0) return;

                int start = row * getColumnCount() + colStart;
                pageChanger.paste(start, false);
                repaint();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("ctrl shift V"), "paste with insert");
        actionMap.put("paste with insert", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;

                int row = getSelectedRow();
                int colStart = getSelectedColumn();

                if (row < 0 || colStart < 0) return;

                int start = row * getColumnCount() + colStart;
                pageChanger.paste(start, true);
                repaint();
            }
        });
    }
}
