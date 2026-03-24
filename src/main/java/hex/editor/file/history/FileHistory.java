package hex.editor.file.history;

import hex.editor.config.HexEditorConfig;

import java.util.*;

import javax.swing.Timer;
import java.util.*;

public class FileHistory {
    private Deque<Transaction> undoStack = new LinkedList<>();
    private Deque<Transaction> redoStack = new LinkedList<>();
    private Map<Long, PageHistory> pages = new HashMap<>();
    private Transaction currentTransaction;
    private Timer transactionTimer;
    private long transactionTimeoutMs = HexEditorConfig.getInstance().getLong("file.transaction.timeout.ms");

    public FileHistory() {
        transactionTimer = new Timer((int) transactionTimeoutMs, e -> commitTransaction());
        transactionTimer.setRepeats(false);
    }

    public void doChanges(ByteBlock block) {
        if (currentTransaction == null) {
            currentTransaction = new Transaction(block.getType());
        }

        currentTransaction.addBlock(block);
        PageHistory pageHistory = pages.get(block.getPageIndex());
        pageHistory.doChanges(block);
        transactionTimer.restart();
    }

    private void commitTransaction() {
        if (currentTransaction != null && !currentTransaction.isEmpty()) {
            undoStack.push(currentTransaction);
            redoStack.clear();
            currentTransaction = null;
        }
    }

    public void flush() {
        transactionTimer.stop();
        commitTransaction();
    }




}