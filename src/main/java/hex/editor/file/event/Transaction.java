package hex.editor.file.event;

import java.util.ArrayList;
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
        if(type.equals(FileEventType.INSERT) || type.equals(FileEventType.UPDATE)){
            data = new ArrayList<>(blocks.size());
        }
        int lastIndex = -1;
        start = Integer.MAX_VALUE;
        end = 0;
        while(!blocks.isEmpty()){
            ByteBlock block = blocks.pollLast();
            if(type.equals(FileEventType.INSERT) || type.equals(FileEventType.UPDATE)){
                if(lastIndex == block.index){
                    data.add(block.index % blocks.size(), block.data);
                } else {
                    data.set(block.index % blocks.size(), block.data);
                }
            }
            if(start > block.index){
                start = block.index;
            }
            if (end < block.index){
                end = block.index;
            }
        }
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
        if(blocks.peek() == null){
            return false;
        }
        if(blocks.peek().index + 1 == newBlock.index){
            blocks.push(newBlock); // it is reversed
            return true;
        }
        return false;
    }

    private boolean addBlockInsert(ByteBlock newBlock) {
        if(blocks.peek() == null){
            return false;
        }
        if((blocks.peek().index - 1) == newBlock.index || blocks.peek().index.equals(newBlock.index)){
            blocks.push(newBlock); // it is reversed
            return true;
        }
        return false;
    }

    private boolean addBlockUpdate(ByteBlock newBlock){
        if(blocks.peek() == null){
            return false;
        }
        if(blocks.peek().index + 1 == newBlock.index){
            blocks.push(newBlock);
            return true;
        }
        return false;
    }
}



