package hex.editor.file.dto;

import hex.editor.file.FileChanger;

import java.util.List;

public class PageResult {
    private List<Byte> data;
    private Integer pageIndex;

    public PageResult(List<Byte> data, Integer pageIndex){
        this.data = data;
        this.pageIndex = pageIndex;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public List<Byte> getData() {
        return data;
    }
}
