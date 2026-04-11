package hex.editor.viewer;

import java.util.List;

public interface ByteViewer {
    Integer getInt(byte[] bytes);
    Long getLong(byte[] bytes);
    Float getFloat(byte[] bytes);
    Double getDouble(byte[] bytes);
    SearchResult findByPattern(List<Byte> array, String pattern);
}
