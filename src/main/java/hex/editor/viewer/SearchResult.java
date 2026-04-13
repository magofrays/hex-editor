package hex.editor.viewer;

import java.util.List;

public class SearchResult {
    private final List<Integer> indexes;
    private final Integer maskSize;

    public SearchResult(List<Integer> indexes, Integer maskSize){
        this.indexes = indexes;
        this.maskSize = maskSize;
    }

    public Integer getMaskSize() {
        return maskSize;
    }

    public List<Integer> getIndexes() {
        return indexes;
    }
}
