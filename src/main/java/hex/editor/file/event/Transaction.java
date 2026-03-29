package hex.editor.file.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

public class Transaction extends ChangeEvent {
    ArrayList<Byte> data;
    FileEventType type;
    Integer start;
    Integer end;
    Deque<ByteBlock> blocks = new LinkedList<>();
    Boolean isConstructed = false;

    public Transaction(byte[] data, Integer start, Integer end, FileEventType type, Integer pageIndex) {
        this.data = new ArrayList<>(data.length);
        for (byte b : data) this.data.add(b);
        this.type = type;
        this.start = start;
        this.end = end;
        this.isConstructed = true;
        super.pageIndex = pageIndex;
    }


    public Transaction(FileEventType type) {
        this.type = type;
        this.start = 0;
        this.end = 0;
        this.data = new ArrayList<>();
    }

    public Integer getStart() {
        if(!isConstructed){
            construct();
        }
        return start;
    }

    public Integer getEnd() {
        if(!isConstructed){
            construct();
        }
        return end;
    }

    public ArrayList<Byte> getData() {
        if(!isConstructed){
            construct();
        }
        return data;
    }

    private void construct() {
        int blockSize = blocks.size();
        if(type.equals(FileEventType.INSERT) || type.equals(FileEventType.UPDATE)){
            data = new ArrayList<>(blockSize);
        }
        start = Integer.MAX_VALUE;
        for(ByteBlock block : blocks){
            if(type.equals(FileEventType.INSERT) || type.equals(FileEventType.UPDATE)){
                data.add(block.data);
            }
            if(start > block.index){
                start = block.index;
            }
        }
        end = start + blockSize;
        isConstructed = true;
    }

    public FileEventType getType() {
        return type;
    }

    public Integer getPageIndex() {
        return super.getPageIndex();
    }

    public boolean addBlock(ByteBlock newBlock) {
        if(blocks.isEmpty()){
            type = newBlock.type;
            blocks.push(newBlock);
            return true;
        }
        if(type != newBlock.type){
            return false;
        }
        switch (type){
            case DELETE:
                return addBlockDelete(newBlock);
            case INSERT:
                return addBlockInsert(newBlock);
            case UPDATE:
                return addBlockUpdate(newBlock);
        }
        return false;
    }


    private boolean addBlockDelete(ByteBlock newBlock){
        if(blocks.peekLast() == null){
            return false;
        }
        if(blocks.peekLast().index + 1 == newBlock.index){
            blocks.addLast(newBlock);
            return true;
        }
        return false;
    }

    private boolean addBlockInsert(ByteBlock newBlock) {
        if(blocks.peekFirst() == null){
            return false;
        }
        if(blocks.peekFirst().index - 1 == newBlock.index){
            blocks.addFirst(newBlock); // it is reversed
            return true;
        }
        if(blocks.peekFirst().index.equals(newBlock.index)){
            blocks.addLast(newBlock);
            return true;
        }
        return false;
    }

    private boolean addBlockUpdate(ByteBlock newBlock){
        if(blocks.peekLast() == null){
            return false;
        }
        if(blocks.peekLast().index + 1 == newBlock.index){
            blocks.addLast(newBlock);
            return true;
        } else if (blocks.peekFirst().index - 1 == newBlock.index){
            blocks.addFirst(newBlock);
            return true;
        }
        return false;
    }
}



