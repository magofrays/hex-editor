package hex.editor.gui.grid;

import hex.editor.adapter.PageChanger;
import hex.editor.config.HexEditorConfig;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

public class ByteGrid extends JTable {

    PageChanger pageChanger;
    Integer cellWidth = HexEditorConfig.getInstance().getInteger("editor.cell.width");
    Integer cellHeight = HexEditorConfig.getInstance().getInteger("editor.cell.height");
    ActionMap actionMap = getActionMap();
    InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
    private int selectionStart;
    private int selectionEnd;
    private int anchorIndex;
    private int leadIndex;

    public ByteGrid(TableModel model, PageChanger pageChanger){
        super(model);
        this.pageChanger = pageChanger;
        initUI();
        setupCustomSelection();
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
        selectionStart = 0;
        selectionEnd = 0;
        anchorIndex = 0;
        leadIndex = 0;
    }

    private void setupCustomSelection() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return;

                int index = row * getColumnCount() + col;

                if (e.isShiftDown() && anchorIndex != -1) {
                    selectRange(anchorIndex, index);
                } else {
                    selectionStart = index;
                    selectionEnd = index;
                    anchorIndex = index;
                }
                leadIndex = index;
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return;
                int index = row * getColumnCount() + col;
                selectRange(anchorIndex, index);
                leadIndex = index;
                repaint();
            }
        });
    }

    private void selectRange(int start, int end) {
        selectionStart = Math.min(start, end);
        selectionEnd = Math.max(start, end);
    }

    public List<Integer> getSelectedPositions() {
        List<Integer> positions = new ArrayList<>();
        if (selectionStart != -1 && selectionEnd != -1) {
            for (int i = selectionStart; i <= selectionEnd; i++) {
                positions.add(i);
            }
        }
        return positions;
    }

    public byte[] getSelectedBytes() {
        byte[] result = new byte[selectionEnd - selectionStart + 1];
        List<Byte> data = pageChanger.getData();
        for (int i = selectionStart; i < selectionEnd+1; i++) {
            result[i - selectionStart] = data.get(i);
        }
        return result;
    }

    public void clearSelected() {
        if (pageChanger != null && !pageChanger.getData().isEmpty()) {
            selectionStart = 0;
            selectionEnd = 0;
            anchorIndex = 0;
            leadIndex = 0;
        } else {
            selectionStart = -1;
            selectionEnd = -1;
            anchorIndex = -1;
            leadIndex = -1;
        }
        repaint();
    }

    public boolean isCellSelected(int row, int col) {
        int index = row * getColumnCount() + col;
        return selectionStart != -1 && index >= selectionStart && index <= selectionEnd;
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
        setCellSelectionEnabled(false);
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);
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
            MouseEvent me = (MouseEvent) e;
            if (me.getClickCount() < 2) {
                return false;
            }
        }
        if (selectionStart == -1) {
            return false;
        }
        int actualRow = selectionStart / getColumnCount();
        int actualCol = selectionStart % getColumnCount();
        return super.editCellAt(actualRow, actualCol, e);
    }

    private void setupKeyBindings() {
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

        inputMap.put(KeyStroke.getKeyStroke("shift DELETE"), "delete with offset");
        actionMap.put("delete with offset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;
                if (selectionStart == -1) return;
                if(selectionStart != selectionEnd){
                    pageChanger.delete(selectionStart, selectionEnd + 1);
                } else{
                    pageChanger.delete(selectionStart);
                }
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("DELETE"), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;
                if (selectionStart == -1 && selectionEnd == -1) return;
                byte[] data = new byte[selectionEnd-selectionStart+1];
                for (int i = 0; i != selectionEnd - selectionStart + 1; i++) {
                    data[i] = 0;
                }
                pageChanger.update(selectionStart,  data);
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("BACK_SPACE"), "backspace");
        actionMap.put("backspace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;
                if(selectionStart == -1) return;
                if(selectionStart != selectionEnd){
                    pageChanger.delete(selectionStart, selectionEnd);
                } else {
                    pageChanger.delete(selectionStart);
                }
                int newPos = selectionStart - 1;
                if (newPos >= 0) {
                    selectionStart = newPos;
                    selectionEnd = newPos;
                    anchorIndex = newPos;
                    leadIndex = newPos;
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

                byte[] data = getSelectedBytes();
                if (data.length > 0) {
                    pageChanger.copy(data);
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl V"), "paste");
        actionMap.put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;
                if (selectionStart == -1) return;
                pageChanger.paste(selectionStart, false);
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl shift V"), "paste with insert");
        actionMap.put("paste with insert", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;
                if (selectionStart == -1) return;
                pageChanger.paste(selectionStart, true);
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl shift X"), "cut with offset");
        actionMap.put("cut with offset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;
                if (selectionStart == -1) return;
                byte[] data = getSelectedBytes();
                pageChanger.copy(data);
                pageChanger.delete(selectionStart, selectionEnd + 1);
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl X"), "cut");
        actionMap.put("cut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;
                if (selectionStart == -1) return;
                byte[] data = getSelectedBytes();
                pageChanger.copy(data);
                Arrays.fill(data, (byte) 0);
                pageChanger.update(selectionStart, data);
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl 0"), "insert zero");
        actionMap.put("insert zero", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;
                if (selectionStart == -1) return;
                pageChanger.insert(selectionStart, (byte) 0);
                repaint();
            }
        });
        selectionKeyBindings();
    }

    private void selectionKeyBindings(){
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
        actionMap.put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectionEnd == -1) return;
                int nextPos = selectionEnd + 1;
                int maxPos = pageChanger.getData().size() - 1;
                if (nextPos <= maxPos) {
                    selectionStart = nextPos;
                    selectionEnd = nextPos;
                    anchorIndex = nextPos;
                    leadIndex = nextPos;
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        actionMap.put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectionStart == -1) return;
                int prevPos = selectionStart - 1;
                if (prevPos >= 0) {
                    selectionStart = prevPos;
                    selectionEnd = prevPos;
                    anchorIndex = prevPos;
                    leadIndex = prevPos;
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        actionMap.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectionEnd == -1) return;
                int columns = getColumnCount();
                int nextPos = selectionEnd + columns;
                int maxPos = pageChanger.getData().size() - 1;
                if (nextPos <= maxPos) {
                    selectionStart = nextPos;
                    selectionEnd = nextPos;
                    anchorIndex = nextPos;
                    leadIndex = nextPos;
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("UP"), "moveUp");
        actionMap.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectionStart == -1) return;
                int columns = getColumnCount();
                int prevPos = selectionStart - columns;
                if (prevPos >= 0) {
                    selectionStart = prevPos;
                    selectionEnd = prevPos;
                    anchorIndex = prevPos;
                    leadIndex = prevPos;
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("shift RIGHT"), "extendRight");
        actionMap.put("extendRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (anchorIndex == -1) return;
                int currentPos = leadIndex != -1 ? leadIndex : anchorIndex;
                int nextPos = currentPos + 1;
                int maxPos = pageChanger.getData().size() - 1;

                if (nextPos <= maxPos) {
                    selectRange(anchorIndex, nextPos);
                    leadIndex = nextPos;
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("shift LEFT"), "extendLeft");
        actionMap.put("extendLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (anchorIndex == -1) return;

                int currentPos = leadIndex != -1 ? leadIndex : anchorIndex;
                int prevPos = currentPos - 1;

                if (prevPos >= 0) {
                    selectRange(anchorIndex, prevPos);
                    leadIndex = prevPos;
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("shift DOWN"), "extendDown");
        actionMap.put("extendDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (anchorIndex == -1) return;

                int currentPos = leadIndex != -1 ? leadIndex : anchorIndex;
                int columns = getColumnCount();
                int nextPos = currentPos + columns;
                int maxPos = pageChanger.getData().size() - 1;

                if (nextPos <= maxPos) {
                    selectRange(anchorIndex, nextPos);
                    leadIndex = nextPos;
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("shift UP"), "extendUp");
        actionMap.put("extendUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (anchorIndex == -1) return;

                int currentPos = leadIndex != -1 ? leadIndex : anchorIndex;
                int columns = getColumnCount();
                int prevPos = currentPos - columns;

                if (prevPos >= 0) {
                    selectRange(anchorIndex, prevPos);
                    leadIndex = prevPos;
                    repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl A"), "selectAll");
        actionMap.put("selectAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int totalBytes = pageChanger.getData().size();
                selectionStart = 0;
                selectionEnd = totalBytes - 1;
                anchorIndex = 0;
                leadIndex = totalBytes - 1;
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "clearSelection");
        actionMap.put("clearSelection", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearSelected();
                repaint();
            }
        });
    }
}