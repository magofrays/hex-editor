package hex.editor.gui;

import hex.editor.config.HexEditorConfig;
import hex.editor.file.controller.FileController;
import hex.editor.gui.grid.BytePage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.TreeMap;

public class FileEditor extends JPanel {

    private FileController fileController;
    private final TreeMap<Integer, BytePage> pages = new TreeMap<>();
    private Integer pageSize = HexEditorConfig.getInstance().getInteger("editor.table.height") * HexEditorConfig.getInstance().getInteger("editor.table.width");
    private Long position = 0L;
    private Integer index = 0;

    public FileEditor() {

        try {
            fileController = new FileController(Paths.get("/home/magofrays/java_error_in_idea.hprof"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        setLayout(new BorderLayout(10, 10));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        content.add(list);
        JButton button = new JButton("load new page");
        button.setPreferredSize(new Dimension(1024, 200));
        content.add(button);
        button.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                BytePage page = new BytePage(fileController, position);
                pages.put(index, page);
                list.add(page.getGrid());
                content.revalidate();
                content.repaint();
                pages.put(index, page);
                position += pageSize;
                index++;
            }
        });
        JScrollPane jScrollPane = new JScrollPane(content);
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(new JLabel("Выбрать байт:"));
        NumberFormat format = NumberFormat.getIntegerInstance();
        JFormattedTextField byteField = new JFormattedTextField(format);
        byteField.setValue(0);
        byteField.setColumns(10);
        byteField.setHorizontalAlignment(JTextField.RIGHT);
        JButton goButton = new JButton("Перейти");
        panel.add(byteField);
        panel.add(goButton);
        add(panel, "North");
        add(jScrollPane);


    }
}
