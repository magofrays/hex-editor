package hex.editor.file;

import java.util.List;
import java.util.Map;

public interface FileViewer {
    List<Byte> viewFile(Long position);
}
