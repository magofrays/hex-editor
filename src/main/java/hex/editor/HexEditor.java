package hex.editor;


import hex.editor.gui.FileEditor;
import hex.editor.gui.menu.HexEditorMenu;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    public void addClosableTab(String title, Component component) {
        content.addTab(title, component);
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        JLabel label = new JLabel(title);
        JButton closeButton = createCloseButton(content, header);
        header.add(label);
        header.add(closeButton);
        content.setTabComponentAt(content.getTabCount() - 1, header);
    }

    private JButton createCloseButton(JTabbedPane tabbedPane, JPanel header) {
        JButton button = new JButton("x");
        button.setUI(new BasicButtonUI());
        button.setContentAreaFilled(false);
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        button.setFont(new Font("Arial", Font.BOLD, 10));
        button.setForeground(Color.GRAY);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.GRAY);
            }
        });

        button.addActionListener(e -> {
            int index = tabbedPane.indexOfTabComponent(header);
            if (index != -1) {
                int result = JOptionPane.showConfirmDialog(
                        tabbedPane,
                        "Закрыть вкладку \"" + tabbedPane.getTitleAt(index) + "\"?",
                        "Подтвердить",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (result == JOptionPane.YES_OPTION) {
                    tabbedPane.removeTabAt(index);
                }
            }
        });
        return button;
    }

    public void createFileEditor(String path){
        String fileName = path.substring(path.lastIndexOf(File.separator) + 1);
        addClosableTab(fileName, new FileEditor(path));
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        new HexEditor();
    }
}
