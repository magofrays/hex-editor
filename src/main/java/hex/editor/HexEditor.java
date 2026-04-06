package hex.editor;


import hex.editor.gui.FileEditor;
import hex.editor.gui.menu.HexEditorMenu;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class HexEditor extends JFrame {

    private final JTabbedPane content;
    public HexEditor(){
        super("Hex-редактор");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setJMenuBar(new HexEditorMenu(this));
        content = new JTabbedPane();
        setContentPane(content);
        pack();
        setVisible(true);
    }

    public void createFileEditor(String path){
        String fileName = path.substring(path.lastIndexOf(File.separator) + 1);
        content.add(fileName, new FileEditor(path));
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        new HexEditor();
    }
}
