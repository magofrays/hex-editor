package hex.editor;

import hex.editor.gui.HexEditor;

public class Main {
    public static void main(String[] args) {

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        new HexEditor();
    }
}
