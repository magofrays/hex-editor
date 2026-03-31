package hex.editor.gui;

import hex.editor.config.HexEditorConfig;
import hex.editor.file.controller.FileController;
import hex.editor.file.controller.FileListener;
import hex.editor.file.event.FileEvent;
import hex.editor.gui.grid.BytePage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.TreeMap;

public class HexEditor extends JFrame implements FileListener {

    private FileController fileController;
    private final TreeMap<Integer, BytePage> pages = new TreeMap<>();
    private Long pageSize = HexEditorConfig.getInstance().getLong("file.page.size");
    private Long position = 0L;
    private Integer index = 0;

    public HexEditor() {
        super("Hex-редактор");
        try {
            fileController = new FileController(Paths.get("/home/magofrays/java_error_in_idea.hprof"));
            fileController.subscribeListener(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) this.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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

        add(jScrollPane);

        setVisible(true);
    }


    @Override
    public void notify(FileEvent fileEvent) {

    }
}
