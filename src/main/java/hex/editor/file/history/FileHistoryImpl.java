package hex.editor.file.history;

import hex.editor.config.HexEditorConfig;
import hex.editor.file.FileChanger;
import hex.editor.file.event.ByteBlock;
import hex.editor.file.event.ChangeEvent;
import hex.editor.file.event.Transaction;

import java.util.*;

public class FileHistoryImpl implements FileHistory {
    private final Deque<ChangeEvent> undoStack;
    private final Deque<ChangeEvent> redoStack;
    private Transaction currentTransaction;
    private final Integer historySize;
    private final FileChanger fileChanger;

    public FileHistoryImpl(FileChanger fileHolder) {
        undoStack = new LinkedList<>();
        redoStack = new LinkedList<>();
        historySize = HexEditorConfig.getInstance().getInteger("file.history.size");
        this.fileChanger = fileHolder;
    }


    public void collectByteBlock(ByteBlock block) {
        if (currentTransaction == null) {
            clearRedoStack();
            currentTransaction = new Transaction(block.getType());
        }
        if(!currentTransaction.addBlock(block)){
            undoStack.push(currentTransaction);
            checkSize();
            currentTransaction = null;
        }
    }


    public void collectTransaction(Transaction transaction) {

        if(currentTransaction != null){
            undoStack.push(currentTransaction);
            currentTransaction = null;
        }
        undoStack.push(transaction);
        checkSize();
    }

    @Override
    public void collectEvent(ChangeEvent event){
        clearRedoStack();
        if(event instanceof ByteBlock){
            collectByteBlock((ByteBlock) event);
        }
        if (event instanceof Transaction){
            collectTransaction((Transaction) event);
        }
    }

    public void checkSize(){
        while (undoStack.size() > historySize){
            undoStack.pollFirst();
        }
    }
    public void clearRedoStack(){
        if(!redoStack.isEmpty()){
            redoStack.clear();
        }
    }

    @Override
    public void undoChanges(){
        if(undoStack.isEmpty()){
            return;
        }
        ChangeEvent undoTransaction = undoStack.pollLast();
        ChangeEvent redoTransaction = fileChanger.doChanges(undoTransaction);
        redoStack.push(redoTransaction);
    }

    @Override
    public void redoChanges(){
        if(redoStack.isEmpty()){
            return;
        }
        ChangeEvent redoTransaction = redoStack.pollLast();
        ChangeEvent undoTransaction = fileChanger.doChanges(redoTransaction);
        undoStack.push(undoTransaction);
    }

}