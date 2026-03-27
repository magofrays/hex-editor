package hex.editor.file.history;


import hex.editor.file.event.FileEvent;
import hex.editor.file.event.FileEventType;

public class ByteBlock implements FileEvent, Comparable<ByteBlock> {

    public byte getByte() {
        return data;
    }

    public FileEventType getType() {
        return type;
    }


    public Long getPageIndex() {
        return pageIndex;
    }

    FileEventType type;
    Integer index;
    Byte data;
    Long pageIndex;

    public Integer getIndex() {
        return index;
    }

    public ByteBlock(Integer index, FileEventType eventType, Long pageIndex) {
        this.pageIndex = pageIndex;
    }

    public ByteBlock(Integer index, byte data, FileEventType type, Long pageIndex) {
        this.index = index;
        this.data = data;
        this.pageIndex = pageIndex;
        this.type = type;
    }

    @Override
    public int compareTo(ByteBlock o) {
        return index.compareTo(o.index);
    }
}
