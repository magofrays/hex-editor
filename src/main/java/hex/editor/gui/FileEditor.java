package hex.editor.gui;

import hex.editor.config.HexEditorConfig;
import hex.editor.exception.FileException;
import hex.editor.exception.ViewerException;
import hex.editor.file.controller.FileController;
import hex.editor.gui.grid.BytePage;
import hex.editor.gui.grid.ColumnHeaderList;
import hex.editor.viewer.ByteViewer;
import hex.editor.viewer.ByteViewerImpl;
import hex.editor.viewer.SearchResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.List;
import java.util.TreeMap;

public class FileEditor extends JPanel {

    private final FileController fileController;
    private final TreeMap<Integer, BytePage> pages = new TreeMap<>();
    private Integer tableHeight = HexEditorConfig.getInstance().getInteger("editor.table.height");
    private Integer tableWidth = HexEditorConfig.getInstance().getInteger("editor.table.width");
    private Integer pageSize = tableHeight * tableWidth;
    private final JPanel list = new JPanel();
    private final JPanel content = new JPanel();
    private final JPanel columnHeader = new JPanel();
    private Long currentPosition = (long) -pageSize;
    private Integer currentIndex = -1;
    private final ByteViewer byteViewer = new ByteViewerImpl();

    public FileEditor(String path) {
        try {
            fileController = new FileController(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);


        JPanel byteGroup = createByteGroup();
        JPanel sizeGroup = createSizeGroup();

        row1.add(sizeGroup);
        row1.add(byteGroup);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        row2.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton prevPageButton = new JButton("◀ Предыдущая страница");
        JButton nextPageButton = new JButton("Следующая страница ▶");
        row2.add(prevPageButton);
        row2.add(nextPageButton);

        topPanel.add(row1);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(row2);

        JPanel searchGroup = createSearchGroup();

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        columnHeader.setLayout(new BoxLayout(columnHeader, BoxLayout.Y_AXIS));
        content.add(searchGroup);
        content.add(Box.createVerticalStrut(10));
        searchGroup.setAlignmentX(LEFT_ALIGNMENT);
        content.add(columnHeader);
        createColumnHeader();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(list);
        content.add(Box.createVerticalGlue());
        setNextPage();

        JScrollPane jScrollPane = new JScrollPane(content);
        jScrollPane.getVerticalScrollBar().setUnitIncrement(32);
        add(topPanel, BorderLayout.NORTH);
        add(jScrollPane, BorderLayout.CENTER);
        nextPageButton.addActionListener(e -> setNextPage());
        prevPageButton.addActionListener(e -> setPrevPage());


    }

    private JPanel createSearchGroup() {
        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchGroup.setMaximumSize(new Dimension(Integer.MAX_VALUE, searchGroup.getPreferredSize().height));
        searchGroup.add(new JLabel("Маска:"));
        JTextField field = new JTextField(15);
        searchGroup.add(field);
        JButton searchButton = new JButton("Найти по маске");
        searchGroup.add(searchButton);
        JPanel searchResult = new JPanel();
        searchGroup.add(searchResult);
        searchButton.addActionListener(new AbstractAction() {
            String prevValue;
            int index;
            SearchResult searchResult;
            int prevIndex;
            List<Byte> prevArray;
            @Override
            public void actionPerformed(ActionEvent e) {
                String value = field.getText();
                if(prevValue == null || !prevValue.equals(value) || prevIndex != currentIndex){
                    List<Byte> newArray = pages.get(currentIndex).getData();
                    try{
                        searchResult = byteViewer.findByPattern(newArray, value);
                    } catch (ViewerException exception){
                        JOptionPane.showMessageDialog(FileEditor.this, exception.getMessage());
                        return;
                    }
                    prevIndex = currentIndex;
                    prevValue = value;
                    prevArray = newArray;
                    index = 0;
                }
                if(searchResult.getIndexes().isEmpty()){
                    JOptionPane.showMessageDialog(FileEditor.this, "Ничего не найдено");
                    return;
                }
                int current = searchResult.getIndexes().get(index);
                pages.get(currentIndex).selectRange(current, current + searchResult.getMaskSize() - 1);
                index++;
                if(searchResult.getIndexes().size() == index){
                    index = 0;
                }
            }
        });
        return searchGroup;
    }

    private JPanel createSizeGroup() {
        JPanel sizeGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        NumberFormat intFormat = NumberFormat.getIntegerInstance();
        JFormattedTextField width = new JFormattedTextField(intFormat);
        JFormattedTextField height = new JFormattedTextField(intFormat);
        width.setColumns(4);
        height.setColumns(4);
        JButton sizeButton = new JButton("Построить");

        sizeGroup.add(new JLabel("Размер страницы:"));
        sizeGroup.add(new JLabel("Ширина:"));
        sizeGroup.add(width);
        sizeGroup.add(new JLabel("Высота:"));
        sizeGroup.add(height);
        sizeGroup.add(sizeButton);
        sizeButton.addActionListener(e -> {
            Number widthValueNum = (Number) width.getValue();
            Number heightValueNum = (Number) height.getValue();
            if(widthValueNum == null || heightValueNum == null){
                JOptionPane.showMessageDialog(this, "Значения должны быть заполнены");
                return;
            }
            int widthValue = widthValueNum.intValue();
            int heightValue = heightValueNum.intValue();
            if(widthValue > 0 && heightValue > 0 && widthValue <= 1000 && heightValue <= 1000){
                setPageSize(widthValue, heightValue);
            } else {
                JOptionPane.showMessageDialog(this, "Значения должны быть в диапазоне от 1 до 1000");
            }
        });
        return sizeGroup;
    }

    private JPanel createByteGroup() {
        JPanel byteGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField byteField = new JTextField(10);
        byteField.setText("0");
        JButton goButton = new JButton("Перейти");

        byteGroup.add(new JLabel("Перейти к байту:"));
        byteGroup.add(byteField);
        byteGroup.add(goButton);
        goButton.addActionListener(e -> {
            try {
                String text = byteField.getText().trim();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Введите значение");
                    return;
                }
                long byteIndex = Long.parseLong(text);
                if (byteIndex < 0) {
                    JOptionPane.showMessageDialog(this, "Значение не может быть отрицательным");
                    return;
                }
                long prevPosition = currentPosition;
                int prevIndex = currentIndex;
                currentIndex = getPageIndex(byteIndex) - 1;
                currentPosition = ((long) currentIndex * pageSize);
                if(fileController.getFileSize() < currentPosition){
                    currentPosition = prevPosition;
                    currentIndex = prevIndex;
                    JOptionPane.showMessageDialog(null,
                            "Значение превышает размер файла: " + fileController.getFileSize());
                    return;
                }
                list.removeAll();
                setNextPage();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Введите корректное число");
            }
        });

        byteField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });
        return byteGroup;
    }

    private void createColumnHeader(){
        ColumnHeaderList columnHeaderList = new ColumnHeaderList(tableWidth);
        columnHeaderList.setAlignmentX(Component.LEFT_ALIGNMENT);
        columnHeader.add(columnHeaderList);
    }

    private int getPageIndex(long index){
        return Math.toIntExact(index / pageSize);
    }

    public void setNextPage(){
        if(fileController.getFileSize() < currentPosition + pageSize){
            JOptionPane.showMessageDialog(null,
                    "Значение превышает размер файла: " + fileController.getFileSize());
            return;
        }
        currentPosition += pageSize;
        currentIndex++;
        if(!pages.containsKey(currentIndex)) {
            try {
                BytePage page = new BytePage(byteViewer, fileController, currentPosition, tableWidth, tableHeight);
                pages.put(currentIndex, page);
            } catch (FileException ignored) {
            }
        }
        BytePage page = pages.get(currentIndex);
        list.removeAll();
        list.add(page.getGrid());
        content.revalidate();
        content.repaint();
    }

    public void setPrevPage(){
        if(currentPosition <= 0 && currentIndex <= 0) return;
        currentPosition -= pageSize;
        currentIndex--;
        if(!pages.containsKey(currentIndex)) {
            try {
                BytePage page = new BytePage(byteViewer, fileController, currentPosition, tableWidth, tableHeight);
                pages.put(currentIndex, page);
            } catch (FileException e) {
                JOptionPane.showMessageDialog(null,
                        "Значение превышает размер файла: " + fileController.getFileSize());
            }
        }
        BytePage page = pages.get(currentIndex);
        list.removeAll();
        list.add(page.getGrid());
        content.revalidate();
        content.repaint();

    }

    public void setPageSize(int width, int height){
        tableWidth = width;
        tableHeight = height;
        pageSize = height*width;
        fileController.setPageSize(width, height);
        pages.clear();
        columnHeader.removeAll();
        currentIndex = getPageIndex(currentPosition) - 1;
        currentPosition = ((long) currentIndex * pageSize);
        createColumnHeader();
        setNextPage();
    }
}
