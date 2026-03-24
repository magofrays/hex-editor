package hex.editor.file.history;

import java.util.*;

public class Transaction implements FileEvent {
    Long pageIndex;
    ArrayList<Byte> data;
    FileEventType type;
    Integer start;
    Integer end;

    TreeMap<Integer, ByteBlock> blocks = new TreeMap<>();

    public Transaction(ArrayList<Byte> data, FileEventType type, Long pageIndex) {
        this.data = data;
        this.type = type;
        this.pageIndex = pageIndex;
    }

    public Transaction(FileEventType type) {
        this.type = type;
        this.start = 0;
        this.end = 0;
        this.data = new ArrayList<>();
    }

    public Integer getStart() {
        return start;
    }

    public Integer getEnd() {
        return end;
    }

    public ArrayList<Byte> getData() {
        return data;
    }

    public FileEventType getType() {
        return type;
    }

    public Long getPageIndex() {
        return pageIndex;
    }

    public boolean addBlock(ByteBlock newBlock) {
        boolean isOkForTransaction = false;
        Integer index = newBlock.getIndex();
        if(blocks.containsKey(newBlock.index)){
            isOkForTransaction = true;
        } else {
            Integer firstKey = blocks.firstKey();
            Integer lastKey = blocks.lastKey();
            if(firstKey - index == 1 || lastKey - index == 1){
                // todo
            }

        }

        return isOkForTransaction;
    }
}
