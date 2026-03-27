package hex.editor.file.history;

import hex.editor.config.HexEditorConfig;
import hex.editor.file.FileChanger;

import java.util.*;

public class FileHistoryImpl implements FileHistory {
    private final Deque<Transaction> undoStack;
    private final Deque<Transaction> redoStack;
    private Transaction currentTransaction;
    private final Integer historySize;
    private final FileChanger fileChanger;

    public FileHistoryImpl(FileChanger fileHolder) {
        undoStack = new LinkedList<>();
        redoStack = new LinkedList<>();
        historySize = HexEditorConfig.getInstance().getInteger("file.history.size");
        this.fileChanger = fileHolder;
    }

    @Override
    public void collectEvent(ByteBlock block) {
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

    @Override
    public void collectEvent(Transaction transaction) {
        clearRedoStack();
        if(currentTransaction != null){
            undoStack.push(currentTransaction);
            currentTransaction = null;
        }
        undoStack.push(transaction);
        checkSize();
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
        Transaction undoTransaction = undoStack.pollLast();
        Transaction redoTransaction = fileChanger.doChanges(undoTransaction);
        redoStack.push(redoTransaction);
    }

    @Override
    public void redoChanges(){
        if(redoStack.isEmpty()){
            return;
        }
        Transaction redoTransaction = redoStack.pollLast();
        Transaction undoTransaction = fileChanger.doChanges(redoTransaction);
        undoStack.push(undoTransaction);
    }

}