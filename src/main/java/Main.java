import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

public class Main {
    public static void main(String[] args) {
        Path path = Paths.get("test.bin");
        try(FileChannel fileChannel = FileChannel.open(path, WRITE, READ)){
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            int bytesRead;
            fileChannel.position(5);
            long oldPosition = 5;
            while((bytesRead = fileChannel.read(byteBuffer)) != -1){
                byteBuffer.flip();
                System.out.println("have been read: " + bytesRead);
                byteBuffer.put(3, (byte)'a');
                for (int i = 0; i != bytesRead; i++){
                    byte elem = byteBuffer.get();
                    System.out.print((char)elem);
                }
                System.out.println();
                byteBuffer.rewind();
                fileChannel.position(oldPosition);
                fileChannel.write(byteBuffer);
                byteBuffer.clear();
                oldPosition = fileChannel.position();
            }

        } catch (IOException exception){

        }
    }
}
