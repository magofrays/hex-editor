package hex.editor.gui.menu;

import hex.editor.HexEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HexEditorMenu extends JMenuBar {
    private JFileChooser fileChooser;
    private JMenu file;
    private HexEditor hexEditor;
    public HexEditorMenu(HexEditor hexEditor){
        this.hexEditor = hexEditor;
        JMenu file = new JMenu("Файл");
        add(file);
        add(new JMenu("Правка"));
        add(Box.createHorizontalGlue());
        JMenuItem open = new JMenuItem("Открыть");
        file.add(open);
        fileChooser = new JFileChooser();
        open.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle("Выберите файл");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int res = fileChooser.showOpenDialog(HexEditorMenu.this);
                if(res == JFileChooser.APPROVE_OPTION){
                    hexEditor.createFileEditor(fileChooser.getSelectedFile().getPath());
                }
            }
        });
    }
}
