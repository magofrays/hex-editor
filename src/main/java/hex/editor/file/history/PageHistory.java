package hex.editor.file.history;

import hex.editor.file.FileChanger;
import hex.editor.file.page.FilePage;
import hex.editor.file.page.PageOperations;

import java.util.Deque;
import java.util.LinkedList;

public class PageHistory implements FileChanger {

    private Deque<Transaction> undoStack = new LinkedList<>();
    private Deque<Transaction> redoStack = new LinkedList<>();
    private PageOperations page;

    public PageHistory(FilePage page) {
        this.page = page;
    }

    public void doChanges(ByteBlock t){
        switch (t.type){
            case DELETE:
                byte del = page.doDelete(t);
                break;
            case UPDATE:
                page.doUpdate(t);
                break;
            case INSERT:
                page.doInsert(t);
        }
    }

    @Override
    public void doChanges(Transaction transaction) {

    }

    private void undoChanges(){
        Transaction transaction = undoStack.pollLast();
        redoStack.offerLast(transaction);
    }
}
