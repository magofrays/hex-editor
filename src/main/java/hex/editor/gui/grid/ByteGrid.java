package hex.editor.gui.grid;

import hex.editor.adapter.PageChanger;
import hex.editor.config.HexEditorConfig;
import hex.editor.viewer.ByteViewer;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

public class ByteGrid extends JTable {

    PageChanger pageChanger;
    Integer cellWidth = HexEditorConfig.getInstance().getInteger("editor.cell.width");
    Integer cellHeight = HexEditorConfig.getInstance().getInteger("editor.cell.height");
    ActionMap actionMap = getActionMap();
    InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
    ByteViewer byteViewer;
    private int selectionStart;
    private int selectionEnd;
    private int anchorIndex;
    private int leadIndex;

    public ByteGrid(TableModel model, PageChanger pageChanger, ByteViewer byteViewer){
        super(model);
        this.pageChanger = pageChanger;
        this.byteViewer = byteViewer;
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
        setupByteViewPopup();
    }

    private void setupByteViewPopup() {
        JPopupMenu byteViewMenu = new JPopupMenu();
        byteViewMenu.setLayout(new FlowLayout());
        byteViewMenu.setVisible(false);
        byteViewMenu.setFocusCycleRoot(false);
        byteViewMenu.setFocusable(false);
        PropertyChangeListener byteView = evt -> {
            if ("selection".equals(evt.getPropertyName())) {
                updateByteViewPosition(byteViewMenu);
            }
        };
        addPropertyChangeListener(byteView);
    }
    private void updateByteViewPosition(JPopupMenu popup) {
        if (selectionStart == -1 || selectionEnd == -1) return;

        int columnStart = selectionStart % getColumnCount();
        int rowStart = selectionStart / getColumnCount();
        java.awt.Rectangle startRect = getCellRect(rowStart, columnStart, true);
        int size = selectionEnd - selectionStart + 1;
        byte[] data = getSelectedBytes();

        popup.removeAll();
        popup.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        if(size < 2){
            addPopupRow(popup, gbc, row++, "int8:", String.valueOf(data[0]));
            addPopupRow(popup, gbc, row++, "uint8:", String.valueOf(byteViewer.getUByte(data[0])));
        }
        if (size >= 2 && size < 4) {
            addPopupRow(popup, gbc, row++, "Int16:", String.valueOf(byteViewer.getShort(data)));
            addPopupRow(popup, gbc, row++, "UInt16:", String.valueOf(byteViewer.getUShort(data)));
        }

        if (size >= 4 && size < 8) {
            addPopupRow(popup, gbc, row++, "Int32:", String.valueOf(byteViewer.getInt(data)));
            addPopupRow(popup, gbc, row++, "UInt32:", String.valueOf(byteViewer.getUInt(data)));
            addPopupRow(popup, gbc, row++, "Float:", String.valueOf(byteViewer.getFloat(data)));
        }

        if (size >= 8) {
            addPopupRow(popup, gbc, row++, "Int64:", String.valueOf(byteViewer.getLong(data)));
            addPopupRow(popup, gbc, row++, "UInt64:", byteViewer.getULong(data));
            addPopupRow(popup, gbc, row, "Double:", String.valueOf(byteViewer.getDouble(data)));
        }
        popup.pack();
        popup.show(this, startRect.x, startRect.y + cellHeight);
    }

    private void addPopupRow(JPopupMenu popup, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel nameLabel = new JLabel(label);
        nameLabel.setForeground(Color.DARK_GRAY);
        popup.add(nameLabel, gbc);

        gbc.gridx = 1;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(Color.BLACK);
        valueLabel.setFont(new Font("Monospaced", Font.BOLD, 11));
        popup.add(valueLabel, gbc);
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
                    setSelection(index);
                }
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

    public void selectRange(int start, int end) {
        int newSelectionStart = Math.min(start, end);
        int newSelectionEnd = Math.max(start, end);
        selectionStart = newSelectionStart;
        selectionEnd = newSelectionEnd;
        leadIndex = selectionEnd;
        firePropertyChange("selection", 0, 1);
        repaint();
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
            firePropertyChange("selection", 0, 1);
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

    public void setSelection(int newPos){

        selectionStart = newPos;
        selectionEnd = newPos;
        anchorIndex = newPos;
        leadIndex = newPos;
        firePropertyChange("selection", 0, 1);
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
                setSelection(selectionStart);
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
                selectRange(selectionStart, selectionEnd);
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
                    setSelection(newPos);
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
                int pasted = pageChanger.paste(selectionStart, false);
                selectRange(selectionStart, selectionStart+pasted-1);
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl shift V"), "paste with insert");
        actionMap.put("paste with insert", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;
                if (selectionStart == -1) return;
                int pasted = pageChanger.paste(selectionStart, true);
                selectRange(selectionStart, selectionStart+pasted-1);
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
                setSelection(selectionStart);
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
                selectRange(selectionStart, selectionEnd);
                repaint();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ctrl 0"), "insert zero");
        actionMap.put("insert zero", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pageChanger == null) return;
                if (selectionStart == -1) return;
                pageChanger.insert(selectionEnd, (byte) 0);

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
                    setSelection(nextPos);
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
                    setSelection(prevPos);
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
                    setSelection(nextPos);
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
                    setSelection(prevPos);
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
                firePropertyChange("selection", selectionStart, 0);
                selectionStart = 0;
                selectionEnd = totalBytes - 1;
                anchorIndex = 0;
                leadIndex = totalBytes - 1;
                firePropertyChange("selection", 0, 1);
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