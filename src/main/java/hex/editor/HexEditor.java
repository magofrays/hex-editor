package hex.editor;


import hex.editor.gui.FileEditor;

import javax.swing.*;

public class HexEditor extends JFrame {

    public HexEditor(){
        super("Hex-редактор");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        ((JPanel) this.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new FileEditor());
        setVisible(true);
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        new HexEditor();
    }
}
