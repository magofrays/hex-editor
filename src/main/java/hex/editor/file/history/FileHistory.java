package hex.editor.file.history;

import hex.editor.config.HexEditorConfig;
import hex.editor.file.FileChanger;

import java.util.*;

import javax.swing.Timer;
import java.util.*;

public class FileHistory implements FileChanger {
    private final Deque<Transaction> undoStack = new LinkedList<>();
    private final Deque<Transaction> redoStack = new LinkedList<>();
    private Transaction currentTransaction;
    private Integer maxHistorySize = HexEditorConfig.getInstance().getInteger("")

    public FileHistory() {

    }

    @Override
    public void doChanges(ByteBlock block) {
        if (currentTransaction == null) {
            currentTransaction = new Transaction(block.getType());
        }
        if(!currentTransaction.addBlock(block)){
            undoStack.push(currentTransaction);
            currentTransaction = null;
        }
    }

    @Override
    public void doChanges(Transaction transaction) {
        if(currentTransaction != null){
            undoStack.push(currentTransaction);
            currentTransaction = null;
        }
        undoStack.push(transaction);
    }

}