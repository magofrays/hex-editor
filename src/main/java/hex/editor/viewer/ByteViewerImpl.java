package hex.editor.viewer;

import hex.editor.exception.ViewerException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteViewerImpl implements ByteViewer {
    @Override
    public Integer getInt(byte[] bytes) {
        ByteBuffer wrapper = ByteBuffer.wrap(bytes);
        return wrapper.getInt();
    }

    @Override
    public Long getUInt(byte[] bytes) {
        return getInt(bytes) & 0xFFFFFFFFL;
    }

    @Override
    public Long getLong(byte[] bytes) {
        ByteBuffer wrapper = ByteBuffer.wrap(bytes);
        return wrapper.getLong();
    }

    @Override
    public Integer getUShort(byte[] bytes) {
        return (int) (getShort(bytes) & 0xFFFF);
    }

    @Override
    public Short getShort(byte[] bytes) {
        ByteBuffer wrapper = ByteBuffer.wrap(bytes);
        return wrapper.getShort();
    }

    @Override
    public String getULong(byte[] bytes) {
        ByteBuffer wrapper = ByteBuffer.wrap(bytes);
        long signed = wrapper.getLong();
        return Long.toUnsignedString(signed);
    }

    @Override
    public Float getFloat(byte[] bytes) {
        ByteBuffer wrapper = ByteBuffer.wrap(bytes);
        return wrapper.getFloat();
    }

    @Override
    public Double getDouble(byte[] bytes) {
        ByteBuffer wrapper = ByteBuffer.wrap(bytes);
        return wrapper.getDouble();
    }


    private int[] buildPrefixFunction(byte[] mask, boolean[] any) {
        int[] prefix = new int[mask.length];

        for (int i = 1; i < mask.length; i++) {
            int j = prefix[i - 1];

            while (j > 0 && !(any[i] || mask[i] == mask[j])) {
                j = prefix[j - 1];
            }

            if (any[i] || mask[i] == mask[j]) {
                j++;
            }
            prefix[i] = j;
        }

        return prefix;
    }



    @Override
    public SearchResult findByPattern(List<Byte> array, String pattern) {
        if(pattern == null){
            throw new ViewerException("Пустая маска");
        }
        pattern = pattern.trim();
        if(pattern.isEmpty()) {
            throw new ViewerException("Пустая маска");
        }
        String[] parts = pattern.split("\\s+");
        byte[] mask = new byte[parts.length];
        boolean[] any = new boolean[parts.length];
        for (int i = 0; i != parts.length; i++){
            if(parts[i].equals("?") || parts[i].equals("??")){
                any[i] = true;
            }
            try {
                int value = Integer.parseInt(parts[i], 16);
                if (value < 0 || value > 255) {
                    throw new ViewerException("Неправильный формат данных: " + parts[i]);
                }
                mask[i] = (byte) value;
            } catch (NumberFormatException e) {
                throw new ViewerException("Неправильный формат данных: " + parts[i]);
            }
        }

        List<Integer> indexes = getPositions(array, mask, any);
        return new SearchResult(indexes, parts.length);

    }

    private List<Integer> getPositions(List<Byte> array, byte[] mask, boolean[] any) {
        List<Integer> positions = new ArrayList<>();
        int[] prefix = buildPrefixFunction(mask, any);
        int j = 0;
        for (int i = 0; i < array.size(); i++) {
            while (j > 0 && !(any[i] || mask[i] == mask[j])) {
                j = prefix[j - 1];
            }

            if (any[i] || mask[i] == mask[j]) {
                j++;
            }

            if (j == mask.length) {
                positions.add(i - mask.length + 1);
                j = prefix[j - 1];
            }
        }
        return positions;
    }
}
