package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        // создаём временный файл и инициализируем менеджер
        tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();
        manager = new FileBackedTaskManager(tempFile.getAbsolutePath());
    }

    @Test
    void shouldSaveAndLoadEmptyFile() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();
        // сохраняем явно (пусть даже нечего)
        assertTrue(tempFile.exists());
        // загружаем обратно
        FileBackedTaskManager loaded = new FileBackedTaskManager(tempFile.getAbsolutePath());
        assertTrue(loaded.getTasks().isEmpty());
        assertTrue(loaded.getEpics().isEmpty());
        assertTrue(loaded.getSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadSingleTask() throws IOException {
        File tempFile = File.createTempFile("task", ".csv");
        tempFile.deleteOnExit();
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile.getAbsolutePath());
        Task task = new Task("Test Task", "Description", TaskStatus.NEW);
        int id = manager.addNewTask(task);
        // загружаем из файла новый экземпляр менеджера
        FileBackedTaskManager loaded = new FileBackedTaskManager(tempFile.getAbsolutePath());
        List<Task> tasks = loaded.getTasks();

        assertEquals(1, tasks.size());
        assertEquals(id, tasks.get(0).getId());
        assertEquals("Test Task", tasks.get(0).getName());
        assertEquals("Description", tasks.get(0).getDescription());
        assertEquals(TaskStatus.NEW, tasks.get(0).getStatus());
    }

    @Test
    void shouldSaveAndLoadMultipleTasksAndSubtasks() throws IOException {
        File tempFile = File.createTempFile("multiple", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile.getAbsolutePath());

        Task task1 = new Task("Task1", "Desc1", TaskStatus.NEW);
        Epic epic = new Epic("Epic1", "Big epic");
        int epicId = manager.addNewEpic(epic);
        Subtask sub1 = new Subtask("Sub1", "Part of epic", TaskStatus.DONE, epicId);

        manager.addNewTask(task1);
        manager.addNewSubtask(sub1);

        // Загружаем обратно
        FileBackedTaskManager loaded = new FileBackedTaskManager(tempFile.getAbsolutePath());

        // Проверяем корректность восстановления
        assertEquals(1, loaded.getTasks().size());
        assertEquals(1, loaded.getEpics().size());
        assertEquals(1, loaded.getSubtasks().size());

        Subtask loadedSub = loaded.getSubtasks().get(0);
        assertEquals(epicId, loadedSub.getEpicId());
        assertEquals("Sub1", loadedSub.getName());
        assertEquals(TaskStatus.DONE, loadedSub.getStatus());
    }

    @Test
    void shouldRewriteFileWithoutDuplicates() throws IOException {
        File tempFile = File.createTempFile("rewrite", ".csv");
        tempFile.deleteOnExit();

        // 1. Создаём менеджер и добавляем одну задачу
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile.getAbsolutePath());
        Task task1 = new Task("First", "Old one", TaskStatus.NEW);
        manager.addNewTask(task1);

        long fileSizeAfterFirstSave = tempFile.length();

        // 2. Добавляем новую задачу (старые данные не должны дублироваться)
        Task task2 = new Task("Second", "New one", TaskStatus.DONE);
        manager.addNewTask(task2);

        long fileSizeAfterSecondSave = tempFile.length();

        // 3. Загружаем менеджер из файла
        FileBackedTaskManager loaded = new FileBackedTaskManager(tempFile.getAbsolutePath());
        List<Task> tasks = loaded.getTasks();

        // Проверяем, что в файле только 2 уникальные задачи
        assertEquals(2, tasks.size(), "Файл должен содержать только две задачи");

        // Проверяем, что файл реально был перезаписан, а не просто дописан
        assertTrue(fileSizeAfterSecondSave > fileSizeAfterFirstSave, "Файл должен обновляться, а не дублироваться");
    }

    @Test
    void shouldNotDuplicateDataAfterMultipleSaves() {
        // создаём задачи разных типов
        Epic epic = new Epic("Epic", "Epic Description");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Subtask Description", TaskStatus.NEW, epicId);
        manager.addNewSubtask(subtask);

        Task task = new Task("Task", "Description", TaskStatus.NEW);
        manager.addNewTask(task);

        // повторно сохраняем (например, вызываем save() через добавление новой задачи)
        Task anotherTask = new Task("AnotherTask", "Extra", TaskStatus.NEW);
        manager.addNewTask(anotherTask);

        // теперь пробуем загрузить файл
        FileBackedTaskManager loaded = Managers.loadFromFile(tempFile.getAbsolutePath());

        // Проверяем, что ничего не продублировалось
        assertEquals(2, loaded.getTasks().size(), "Должно быть ровно 2 обычные задачи");
        assertEquals(1, loaded.getEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, loaded.getSubtasks().size(), "Должна быть 1 подзадача");

        // Проверяем, что данные корректны
        Epic loadedEpic = loaded.getEpics().get(0);
        assertEquals("Epic", loadedEpic.getName(), "Имя эпика должно совпадать");

        Subtask loadedSub = loaded.getSubtasks().get(0);
        assertEquals(epicId, loadedSub.getEpicId(), "У подзадачи должен быть правильный epicId");
    }


}
