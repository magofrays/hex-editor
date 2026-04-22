package unit;


import hex.editor.exception.FileException;
import hex.editor.file.controller.FileController;
import hex.editor.file.event.ByteBlock;
import hex.editor.file.event.FileEventType;
import hex.editor.file.event.HistoryEvent;
import hex.editor.file.event.SaveEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileControllerTest {
    @TempDir
    Path tempDir;

    Path filePath;
    List<Byte> fileContentList;

    FileController fileController;

    @BeforeEach
    void testFileCreation() throws IOException {
        filePath = tempDir.resolve("my-data.txt");
        byte[] fileContent = "Hello, World!".getBytes();
        Files.write(filePath, fileContent);
        assertTrue(Files.exists(filePath));
        fileContentList = new ArrayList<>(fileContent.length);
        for (byte b : fileContent) {
            fileContentList.add(b);
        }
        fileController = new FileController(filePath);
    }


    @Test
    public void getPageTest() {
        List<Byte> page = fileController.getPage(0L).getData();
        assertEquals(fileContentList, page);
    }

    @Test
    public void getPageWithErrorTest() {
        assertThrows(FileException.class, () -> fileController.getPage(1024L));
    }

    @Test
    public void processUpdateAndUndoAndRedoEventTest() {
        ByteBlock byteBlock = new ByteBlock(5, (byte) 1, FileEventType.UPDATE, 0);
        List<Byte> oldPage = new ArrayList<>(fileController.getPage(1L).getData());
        fileController.processEvent(byteBlock);
        List<Byte> newPage = fileController.getPage(1L).getData();
        assertNotEquals(oldPage, newPage);
        fileContentList.set(5, (byte) 1);
        assertEquals(fileContentList, newPage);
        fileController.processEvent(HistoryEvent.UNDO);
        assertEquals(oldPage, newPage);
        fileController.processEvent(HistoryEvent.REDO);
        assertEquals(fileContentList, newPage);
    }

    @Test
    public void processMultipleUpdateAndUndoAndRedoEventTest() {
        ByteBlock byteBlock1 = new ByteBlock(5, (byte) 1, FileEventType.UPDATE, 0);
        ByteBlock byteBlock2 = new ByteBlock(6, (byte) 1, FileEventType.UPDATE, 0);
        ByteBlock byteBlock3 = new ByteBlock(7, (byte) 1, FileEventType.UPDATE, 0);
        List<Byte> oldPage = new ArrayList<>(fileController.getPage(1L).getData());
        fileController.processEvent(byteBlock1);
        fileController.processEvent(byteBlock2);
        fileController.processEvent(byteBlock3);
        List<Byte> newPage = fileController.getPage(1L).getData();
        assertNotEquals(oldPage, newPage);
        fileContentList.set(5, (byte) 1);
        fileContentList.set(6, (byte) 1);
        fileContentList.set(7, (byte) 1);
        assertEquals(fileContentList, newPage);
        fileController.processEvent(HistoryEvent.UNDO);
        assertEquals(oldPage, newPage);
        fileController.processEvent(HistoryEvent.REDO);
        assertEquals(fileContentList, newPage);
    }

    @Test
    public void processDeleteAndUndoAndRedoEventTest() {
        ByteBlock byteBlock = new ByteBlock(4, FileEventType.DELETE, 0);
        List<Byte> oldPage = new ArrayList<>(fileController.getPage(1L).getData());
        fileController.processEvent(byteBlock);
        List<Byte> newPage = fileController.getPage(1L).getData();
        assertNotEquals(oldPage, newPage);
        fileContentList.remove(4);
        assertEquals(fileContentList, newPage);
        fileController.processEvent(HistoryEvent.UNDO);
        assertEquals(oldPage, newPage);
        fileController.processEvent(HistoryEvent.REDO);
        assertEquals(fileContentList, newPage);
    }

    @Test
    public void processMultipleDeleteAndUndoAndRedoEventTest() {
        ByteBlock byteBlock1 = new ByteBlock(2, FileEventType.DELETE, 0);
        ByteBlock byteBlock2 = new ByteBlock(1, FileEventType.DELETE, 0);
        ByteBlock byteBlock4 = new ByteBlock(0, FileEventType.DELETE, 0);
        List<Byte> oldPage = new ArrayList<>(fileController.getPage(1L).getData());
        fileController.processEvent(byteBlock1);
        fileController.processEvent(byteBlock2);
        fileController.processEvent(byteBlock2);
        fileController.processEvent(byteBlock4);
        List<Byte> newPage = fileController.getPage(1L).getData();
        assertNotEquals(oldPage, newPage);
        fileContentList.remove(2);
        fileContentList.remove(1);
        fileContentList.remove(1);
        fileContentList.remove(0);
        assertEquals(fileContentList, newPage);
        fileController.processEvent(HistoryEvent.UNDO);
        assertEquals(oldPage, newPage);
        fileController.processEvent(HistoryEvent.REDO);
        assertEquals(fileContentList, newPage);
    }

    @Test
    public void processInsertAndUndoAndRedoEventTest() {
        ByteBlock byteBlock = new ByteBlock(5, (byte) 4, FileEventType.INSERT, 0);
        List<Byte> oldPage = new ArrayList<>(fileController.getPage(1L).getData());
        fileController.processEvent(byteBlock);
        List<Byte> newPage = fileController.getPage(1L).getData();
        assertNotEquals(oldPage, newPage);
        fileContentList.add(5, (byte) 4);
        assertEquals(fileContentList, newPage);
        fileController.processEvent(HistoryEvent.UNDO);
        assertEquals(oldPage, newPage);
        fileController.processEvent(HistoryEvent.REDO);
        assertEquals(fileContentList, newPage);
    }

    @Test
    public void processMultipleInsertAndUndoAndRedoEventTest() {
        ByteBlock byteBlock1 = new ByteBlock(5, (byte) 1, FileEventType.INSERT, 0);
        ByteBlock byteBlock2 = new ByteBlock(6, (byte) 3, FileEventType.INSERT, 0);
        ByteBlock byteBlock3 = new ByteBlock(7, (byte) 3, FileEventType.INSERT, 0);
        ByteBlock byteBlock4 = new ByteBlock(8, (byte) 7, FileEventType.INSERT, 0);
        List<Byte> oldPage = new ArrayList<>(fileController.getPage(1L).getData());
        fileController.processEvent(byteBlock1);
        fileController.processEvent(byteBlock2);
        fileController.processEvent(byteBlock3);
        fileController.processEvent(byteBlock4);
        List<Byte> newPage = fileController.getPage(1L).getData();
        assertNotEquals(oldPage, newPage);
        fileContentList.add(5, (byte) 1);
        fileContentList.add(6, (byte) 3);
        fileContentList.add(7, (byte) 3);
        fileContentList.add(8, (byte) 7);
        assertEquals(fileContentList, newPage);
        fileController.processEvent(HistoryEvent.UNDO);
        assertEquals(oldPage, newPage);
        fileController.processEvent(HistoryEvent.REDO);
        assertEquals(fileContentList, newPage);
    }

    @Test
    public void processSaveEvent() throws IOException {
        ByteBlock byteBlock = new ByteBlock(5, (byte) 1, FileEventType.UPDATE, 0);
        ByteBlock byteBlock1 = new ByteBlock(5, (byte) 1, FileEventType.INSERT, 0);
        ByteBlock byteBlock2 = new ByteBlock(6, (byte) 3, FileEventType.INSERT, 0);
        ByteBlock byteBlock3 = new ByteBlock(7, (byte) 3, FileEventType.INSERT, 0);
        ByteBlock byteBlock4 = new ByteBlock(8, (byte) 7, FileEventType.INSERT, 0);
        ByteBlock byteBlock5 = new ByteBlock(2, FileEventType.DELETE, 0);
        ByteBlock byteBlock6 = new ByteBlock(1, FileEventType.DELETE, 0);
        ByteBlock byteBlock7 = new ByteBlock(0, FileEventType.DELETE, 0);
        List<Byte> oldPage = new ArrayList<>(fileController.getPage(1L).getData());
        fileController.processEvent(byteBlock);
        fileController.processEvent(byteBlock1);
        fileController.processEvent(byteBlock2);
        fileController.processEvent(byteBlock3);
        fileController.processEvent(byteBlock4);
        fileController.processEvent(byteBlock5);
        fileController.processEvent(byteBlock6);
        fileController.processEvent(byteBlock7);
        fileController.processEvent(new SaveEvent());
        List<Byte> newPage = fileController.getPage(1L).getData();
        assertNotEquals(oldPage, newPage);
        byte[] savedContent = Files.readAllBytes(filePath);
        List<Byte> memoryState = fileController.getPage(1L).getData();
        assertEquals(memoryState.size(), savedContent.length);
        for (int i = 0; i < memoryState.size(); i++) {
            assertEquals(memoryState.get(i), savedContent[i]);
        }
        for (int i = 0; i < 3; i++) {
            fileController.processEvent(HistoryEvent.UNDO);
        }
        assertEquals(oldPage, newPage);
    }
}
