import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    private static File currentFile;
    private static boolean editing = false;

    private static final JFrame frame = new JFrame();
    private static final JEditorPane editor = new JEditorPane();

    public static void main(String[] args) {
        editor.setMargin(new Insets(5, 5, 5, 5));
        editor.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getModifiersEx() != 0) {
                    return;
                }

                frame.setTitle("*" + getCurrentFileName() + " - Notepad");
                editing = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        var keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        editor.getInputMap().put(keyStroke, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        var scrollPane = new JScrollPane(editor);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        var fileChooser = new JFileChooser();

        var menuBar = new JMenuBar();
        var menu = new JMenu("File");
        var openItem = new JMenuItem("Open");
        var saveItem = new JMenuItem("Save");

        openItem.addActionListener(e -> {
            try {
                var result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    currentFile = fileChooser.getSelectedFile();
                    editor.setText(Files.readString(currentFile.toPath()));
                    frame.setTitle(getCurrentFileName() + " - Notepad");
                    editing = false;
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        saveItem.addActionListener(e -> saveFile());

        menu.add(openItem);
        menu.add(saveItem);

        menuBar.add(menu);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                if (!editing) {
                    closeWindow(e.getWindow());
                    return;
                }

                var option = JOptionPane.showConfirmDialog(
                        frame,
                        String.format("Save file \"%s\"?", getCurrentFileName()),
                        "Save",
                        JOptionPane.YES_NO_CANCEL_OPTION);

                if (option == JOptionPane.NO_OPTION) {
                    closeWindow(e.getWindow());
                } else if (option == JOptionPane.YES_OPTION) {
                    saveFile();
                    closeWindow(e.getWindow());
                }
            }
        });
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setTitle(getCurrentFileName() + " - Notepad");
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setJMenuBar(menuBar);
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    private static void saveFile() {
        try {
            if (currentFile != null) {
                Files.writeString(currentFile.toPath(), editor.getText());
                frame.setTitle(getCurrentFileName() + " - Notepad");
                editing = false;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String getCurrentFileName() {
        return currentFile == null ? "Untitled" : currentFile.getName();
    }

    private static void closeWindow(Window window) {
        window.setVisible(false);
        window.dispose();
        System.exit(0);
    }
}
