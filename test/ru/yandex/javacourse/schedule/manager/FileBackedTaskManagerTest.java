package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    @BeforeEach
    public void initManager() {
        try {
            tempFile = File.createTempFile("tasks", ".csv");
            tempFile.deleteOnExit();
            manager = Managers.loadFromFile(tempFile.getAbsolutePath());
        } catch (IOException e) {
            fail("Не удалось создать временный файл для тестов: " + e.getMessage());
        }
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loaded = Managers.loadFromFile(tempFile.getAbsolutePath());
        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
        assertTrue(loaded.getHistory().isEmpty());
    }

    @Test
    void shouldHandleFileLoadException() {
        File badFile = new File("bad_header.csv");
        try {
            Files.writeString(badFile.toPath(), "bad_header\n");

            assertDoesNotThrow(() -> Managers.loadFromFile(badFile.getAbsolutePath()), "Загрузка из файла с неверным заголовком не должна вызывать критического исключения.");

            badFile.delete();
        } catch (IOException e) {
            fail("Ошибка при создании файла для теста исключений.");
        }
    }


}