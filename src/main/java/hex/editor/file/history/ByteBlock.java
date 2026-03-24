package hex.editor.file.history;


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

    public ByteBlock(Integer index, Long pageIndex) {
        this.pageIndex = pageIndex;
    }

    public ByteBlock(Integer index, byte data, Long pageIndex) {
        this.index = index;
        this.data = data;
        this.pageIndex = pageIndex;
    }

    @Override
    public int compareTo(ByteBlock o) {
        return index.compareTo(o.index);
    }
}
