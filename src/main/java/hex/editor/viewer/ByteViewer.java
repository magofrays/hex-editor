package hex.editor.viewer;

import java.util.List;

public interface ByteViewer {
    Short getShort(byte[] bytes);
    Integer getUShort(byte[] bytes);
    Integer getInt(byte[] bytes);
    Long getUInt(byte[] bytes);
    Long getLong(byte[] bytes);
    String getULong(byte[] bytes);
    Float getFloat(byte[] bytes);
    Double getDouble(byte[] bytes);
    SearchResult findByPattern(List<Byte> array, String pattern);
}
