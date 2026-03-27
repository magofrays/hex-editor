package hex.editor.file.event;


public class ByteBlock extends ChangeEvent implements Comparable<ByteBlock> {

    public byte getByte() {
        return data;
    }

    public FileEventType getType() {
        return type;
    }


    public Integer getPageIndex() {
        return super.getPageIndex();
    }

    FileEventType type;
    Integer index;
    Byte data;

    public Integer getIndex() {
        return index;
    }

    public ByteBlock(Integer index, FileEventType eventType, Integer pageIndex) {
        super.pageIndex = pageIndex;
    }

    public ByteBlock(Integer index, byte data, FileEventType type, Integer pageIndex) {
        this.index = index;
        this.data = data;
        super.pageIndex = pageIndex;
        this.type = type;
    }

    @Override
    public int compareTo(ByteBlock o) {
        return index.compareTo(o.index);
    }
}
