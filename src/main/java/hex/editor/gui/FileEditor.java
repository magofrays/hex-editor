package hex.editor.gui;

import hex.editor.config.HexEditorConfig;
import hex.editor.exception.FileException;
import hex.editor.file.controller.FileController;
import hex.editor.gui.grid.BytePage;
import hex.editor.gui.grid.ColumnHeaderList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.TreeMap;

public class FileEditor extends JPanel {

    private final FileController fileController;
    private final TreeMap<Integer, BytePage> pages = new TreeMap<>();
    private Integer tableHeight = HexEditorConfig.getInstance().getInteger("editor.table.height");
    private Integer tableWidth = HexEditorConfig.getInstance().getInteger("editor.table.width");
    private final Integer pageSize = tableHeight * tableWidth;
    private final JPanel list = new JPanel();
    private final JPanel content = new JPanel();
    private final JPanel columnHeader = new JPanel();
    private Long currentPosition = 0L;
    private Integer currentIndex = 0;

    public FileEditor(String path) {
        try {
            fileController = new FileController(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setLayout(new BorderLayout(10, 10));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        columnHeader.setLayout(new BoxLayout(columnHeader, BoxLayout.Y_AXIS));
        content.add(columnHeader);
        createColumnHeader();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(list);
        addNewPage();
        JButton button = new JButton("Load new page");
        button.setMinimumSize(new Dimension(600, 600));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(button);
        button.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewPage();
            }
        });
        JScrollPane jScrollPane = new JScrollPane(content);
        jScrollPane.getVerticalScrollBar().setUnitIncrement(32);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JPanel sizePanel = new JPanel();
        NumberFormat intFormat = NumberFormat.getIntegerInstance();
        JFormattedTextField width = new JFormattedTextField(intFormat);
        JFormattedTextField height = new JFormattedTextField(intFormat);
        width.setColumns(4);
        height.setColumns(4);
        sizePanel.add(new JLabel("Ширина:"));
        sizePanel.add(width);
        sizePanel.add(new JLabel("Высота:"));
        sizePanel.add(height);
        JButton sizeButton = new JButton("Построить");
        sizePanel.add(sizeButton);
        sizeButton.addActionListener(e -> {
            Number widthValueNum = (Number) width.getValue();
            Number heightValueNum = (Number) height.getValue();
            if(widthValueNum == null || heightValueNum == null){
                JOptionPane.showMessageDialog(null, "Значения должны быть заполнены");
                return;
            }
            int widthValue = widthValueNum.intValue();
            int heightValue = heightValueNum.intValue();
            if(widthValue > 0 && heightValue > 0 && widthValue < 1000 && heightValue < 1000){
                setPageSize(widthValue, heightValue);
            } else {
                JOptionPane.showMessageDialog(null, "Значения должны быть в диапазоне от 1 до 1000");
            }
        });

        JPanel bytePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bytePanel.add(new JLabel("Выбрать байт:"));
        JTextField byteField = new JTextField(15);
        bytePanel.add(byteField);
        byteField.setHorizontalAlignment(JTextField.RIGHT);
        byteField.setText("0");
        byteField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });
        JButton goButton = new JButton("Перейти");
        goButton.addActionListener(e -> {
            try {
                String text = byteField.getText().trim();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Введите значение");
                    return;
                }
                long byteIndex = Long.parseLong(text);
                if (byteIndex < 0) {
                    JOptionPane.showMessageDialog(null, "Значение не может быть отрицательным");
                    return;
                }
                currentIndex = getPageIndex(byteIndex);
                currentPosition = ((long) currentIndex * pageSize);
                list.removeAll();
                addNewPage();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Введите корректное число");
            }
        });
        bytePanel.add(goButton);
        panel.add(sizePanel);
        panel.add(bytePanel);
        add(panel, "North");
        add(jScrollPane);
    }

    private void createColumnHeader(){
        ColumnHeaderList columnHeaderList = new ColumnHeaderList(tableWidth);
        columnHeaderList.setAlignmentX(Component.LEFT_ALIGNMENT);
        columnHeader.add(columnHeaderList);
    }

    private int getPageIndex(long index){
        return Math.toIntExact(index / pageSize);
    }

    public void addNewPage(){
        if(!pages.containsKey(currentIndex)) {
            try {
                BytePage page = new BytePage(fileController, currentPosition, tableWidth, tableHeight);
                pages.put(currentIndex, page);
            } catch (FileException e) {
                JOptionPane.showMessageDialog(null,
                        "Значение превышает размер файла: " + fileController.getFileSize());
            }
        }
        BytePage page = pages.get(currentIndex);
        list.add(page.getGrid());
        content.revalidate();
        content.repaint();
        currentPosition += pageSize;
        currentIndex++;
    }

    public void setPageSize(int width, int height){
        fileController.setPageSize(width, height);
        pages.clear();
        list.removeAll();
        columnHeader.removeAll();
        currentIndex = 0;
        currentPosition = 0L;
        tableWidth = width;
        tableHeight = height;
        createColumnHeader();
        addNewPage();
    }
}
