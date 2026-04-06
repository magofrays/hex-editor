package hex.editor.gui.grid;

import hex.editor.HexEditor;
import hex.editor.adapter.PageChanger;
import hex.editor.config.HexEditorConfig;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.*;
import java.util.EventObject;

public class ByteGrid extends JTable {

    PageChanger pageChanger;
    Integer cellWidth = HexEditorConfig.getInstance().getInteger("editor.cell.width");
    Integer cellHeight = HexEditorConfig.getInstance().getInteger("editor.cell.height");
    ActionMap actionMap = getActionMap();

    public ByteGrid(TableModel model, PageChanger pageChanger){
        super(model);
        this.pageChanger = pageChanger;
        initUI();

        setupKeyBindings();

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
    }

    private JPopupMenu getJPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem cutItem = new JMenuItem("Вырезать (Ctrl+X)");
        cutItem.addActionListener(actionMap.get("cut"));
        JMenuItem cutWithOffsetItem = new JMenuItem("Вырезать со смещением (Ctrl+Shift+X)");
        cutWithOffsetItem.addActionListener(actionMap.get("cut with offset"));
        JMenuItem copyItem = new JMenuItem("Копировать (Ctrl+C)");
        copyItem.addActionListener(actionMap.get("copy"));
        JMenuItem pasteItem = new JMenuItem("Вставить (Ctrl+V)");
        pasteItem.addActionListener(actionMap.get("paste"));
        JMenuItem pasteWithInsertItem = new JMenuItem("Вставить со смещением (Ctrl+Shift+V)");
        pasteWithInsertItem.addActionListener(actionMap.get("paste with insert"));
        JMenuItem saveItem = new JMenuItem("Сохранить (Ctrl+S)");
        saveItem.addActionListener(actionMap.get("save"));
        JMenuItem undoItem = new JMenuItem("Отменить действие (Ctrl+Z)");
        undoItem.addActionListener(actionMap.get("undo"));
        JMenuItem redoItem = new JMenuItem("Вернуть действие (Ctrl+Shift+Z)");
        redoItem.addActionListener(actionMap.get("redo"));
        JMenuItem deleteItem = new JMenuItem("Удалить (Del)");
        deleteItem.addActionListener(actionMap.get("delete"));
        JMenuItem deleteWithOffsetItem = new JMenuItem("Удалить со смещением (Shift+Del)");
        deleteWithOffsetItem.addActionListener(actionMap.get("delete with offset"));
        JMenuItem insertZeroItem = new JMenuItem("Вставить нулевой байт (Ctrl+O)");
        insertZeroItem.addActionListener(actionMap.get("insert zero"));
        popup.add(cutItem);
        popup.add(cutWithOffsetItem);
        popup.add(copyItem);
        popup.add(pasteItem);
        popup.add(pasteWithInsertItem);
        popup.add(saveItem);
        popup.add(undoItem);
        popup.add(redoItem);
        popup.add(deleteItem);
        popup.add(deleteWithOffsetItem);
        popup.add(insertZeroItem);
        return popup;

    }

    public void initUI(){
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        for (int i = 0; i < getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(cellWidth);
            getColumnModel().getColumn(i).setMinWidth(10);
            getColumnModel().getColumn(i).setMaxWidth(30);
        }
        setRowHeight(cellHeight);

    }
    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        if (e instanceof MouseEvent) {
            return false;
        }

        return super.editCellAt(row, column, e);
    }
    private void setupKeyBindings() {

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

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK), "delete with offset");
        actionMap.put("delete with offset", new AbstractAction() {
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
                    pageChanger.update(start, (byte)0);
                } else {
                    byte[] val = new byte[end-start];
                    for(int i = 0; i != end-start; i++){
                        val[i] = 0;
                    }
                    pageChanger.update(start, val);
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

        inputMap.put(KeyStroke.getKeyStroke("ctrl shift X"), "cut with offset");
        actionMap.put("cut with offset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl X"), "cut");
        actionMap.put("cut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = getSelectedRow();
                int colStart = getSelectedColumn();
                int colEnd = colStart + getSelectedColumnCount();
                if (row < 0 || colStart < 0) return;
                int columnCount = getColumnCount();
                int start = row * columnCount + colStart;
                int end = row * columnCount + colEnd;
                byte[] data = new byte[end-start];
                byte[] val = new byte[end-start];
                for(int i = 0; i != end-start; i++){
                    data[i] = (byte) getValueAt(row, colStart+i);
                    val[i] = 0;
                }
                pageChanger.copy(data);
                pageChanger.update(start, val);
                repaint();
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("Ctrl 0"), "insert zero"); // todo
        actionMap.put("insert zero", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = getSelectedRow();
                int col = getSelectedColumn();
                if (row < 0 && col < 0) return;
                int position = row * getColumnCount() + col;
                pageChanger.insert(position, (byte) 0);
                repaint();
                int nextCol = col + 1;
                int nextRow = row;
                if (nextCol >= getColumnCount()) {
                    nextCol = 0;
                    nextRow = row + 1;
                }
                if (nextRow < getRowCount()) {
                    changeSelection(nextRow, nextCol, false, false);

            }
            }
        });
    }
}
