package hex.editor.file;

import hex.editor.file.event.ChangeEvent;

import java.io.IOException;

public interface FileChanger {
    ChangeEvent doChanges(ChangeEvent event);

    void saveFile() throws IOException;

    default void setPageSize(int width,
                             int height) {

    }
}
