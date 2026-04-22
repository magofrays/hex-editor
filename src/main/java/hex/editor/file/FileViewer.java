package hex.editor.file;

import hex.editor.file.dto.PageResult;

public interface FileViewer {
    PageResult viewFile(Long position);

    Long getFileSize();
}
