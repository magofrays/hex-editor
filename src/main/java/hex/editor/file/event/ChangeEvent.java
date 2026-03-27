package hex.editor.file.event;

public abstract class ChangeEvent implements FileEvent {
    Integer pageIndex;

    public Integer getPageIndex() {
        return pageIndex;
    }
}
