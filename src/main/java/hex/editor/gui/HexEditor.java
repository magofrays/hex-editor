package hex.editor.gui;

import hex.editor.file.controller.FileController;
import hex.editor.file.controller.FileListener;
import hex.editor.file.event.FileEvent;
import hex.editor.file.event.SaveEvent;
import hex.editor.gui.grid.ByteCellRenderer;
import hex.editor.gui.grid.ByteGrid;
import hex.editor.gui.grid.ByteGridModel;
import hex.editor.gui.grid.listener.ByteGridKeyListener;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;

public class HexEditor extends JFrame implements FileListener {

    private FileController fileController;
    private final TreeMap<Integer, ByteGridModel> pages = new TreeMap<>();


    public HexEditor(){
        super("Hex-редактор");
        try {
            fileController = new FileController(Paths.get("/home/magofrays/test.txt"));
            fileController.subscribeListener(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JPanel)this.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        List<Byte> data = fileController.getPage(0L);
        TableModel gridModel = new ByteGridModel(data);
        JTable grid = new ByteGrid(gridModel);
        grid.setDefaultRenderer(Byte.class, new ByteCellRenderer());
        ByteGridKeyListener keyListener = new ByteGridKeyListener(grid, fileController);
        grid.addKeyListener(keyListener);
        add(new JScrollPane(grid));
        setVisible(true);
    }

    @Override
    public void notify(FileEvent fileEvent) {

    }
}
