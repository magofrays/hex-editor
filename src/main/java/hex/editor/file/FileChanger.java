package hex.editor.file;

import hex.editor.file.event.ChangeEvent;

public interface FileChanger {
    ChangeEvent doChanges(ChangeEvent event);
    void saveFile();
}
