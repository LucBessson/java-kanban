package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    public abstract void initManager();

    protected Epic createEpic() {
        Epic epic = new Epic("Тестовый Эпик", "Описание Эпика");
        manager.addNewEpic(epic);
        return epic;
    }

    protected Subtask createSubtask(int epicId, TaskStatus status) {
        Subtask sub = new Subtask("Тестовая Подзадача", "Описание", status, epicId);
        manager.addNewSubtask(sub);
        return sub;
    }

    @Test
    void shouldAddNewTask() {
        Task task = new Task("Test 1", "Testing task 1", NEW);
        int id = manager.addNewTask(task);
        assertNotNull(manager.getTask(id), "Задача не найдена по ID.");
        assertEquals(task, manager.getTask(id), "Добавленная задача не совпадает с полученной.");
    }

    @Test
    void shouldAddNewEpicAndSubtask() {
        Epic epic = createEpic();
        Subtask subtask = new Subtask("Подзадача 1", "К эпику 1", NEW, epic.getId());
        Integer subId = manager.addNewSubtask(subtask);

        assertNotNull(manager.getEpic(epic.getId()), "Эпик не был добавлен.");
        assertNotNull(manager.getSubtask(subId), "Подзадача не была добавлена.");
        assertTrue(manager.getEpicSubtasks(epic.getId()).contains(subtask), "Подзадача не привязана к Эпику.");
    }

    @Test
    void shouldUpdateTask() {
        Task original = new Task("Original", "Original Desc", NEW);
        int id = manager.addNewTask(original);

        Task updated = new Task(id, "Updated Name", "New Desc", DONE);
        manager.updateTask(updated);

        assertEquals("Updated Name", manager.getTask(id).getName());
        assertEquals(DONE, manager.getTask(id).getStatus());
    }

    @Test
    void shouldDeleteTaskAndRemoveFromHistory() {
        Task task = new Task("Test", "Desc", NEW);
        int id = manager.addNewTask(task);
        manager.getTask(id);

        manager.deleteTask(id);

        assertNull(manager.getTask(id), "Задача не была удалена.");
        assertTrue(manager.getHistory().isEmpty(), "Задача не была удалена из истории.");
    }

    @Test
    void shouldDeleteEpicAndItsSubtasks() {
        Epic epic = createEpic();
        Subtask sub = createSubtask(epic.getId(), NEW);
        manager.getEpic(epic.getId());
        manager.getSubtask(sub.getId());

        manager.deleteEpic(epic.getId());

        assertNull(manager.getEpic(epic.getId()), "Эпик не был удален.");
        assertNull(manager.getSubtask(sub.getId()), "Подзадача не была удалена.");
        assertFalse(manager.getHistory().contains(epic), "Эпик не удален из истории.");
        assertTrue(manager.getHistory().isEmpty(), "Подзадача не удалена из истории."); // Если было только 2 элемента

    }

    @Test
    void shouldBeNewWhenAllSubtasksAreNew() {
        Epic epic = createEpic();
        createSubtask(epic.getId(), NEW);
        createSubtask(epic.getId(), NEW);

        assertEquals(NEW, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void shouldBeDoneWhenAllSubtasksAreDone() {
        Epic epic = createEpic();
        createSubtask(epic.getId(), DONE);
        createSubtask(epic.getId(), DONE);

        assertEquals(DONE, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void shouldBeInProgressWhenStatusesAreMixedNewAndDone() {
        Epic epic = createEpic();
        createSubtask(epic.getId(), NEW);
        createSubtask(epic.getId(), DONE);

        assertEquals(IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void shouldBeInProgressWhenAnySubtaskIsInProgress() {
        Epic epic = createEpic();
        createSubtask(epic.getId(), NEW);
        Subtask inProgressSub = createSubtask(epic.getId(), IN_PROGRESS);
        manager.updateSubtask(inProgressSub);

        assertEquals(IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void shouldBeNewWhenNoSubtasks() {
        Epic epic = createEpic();
        assertEquals(NEW, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void shouldReturnNullWhenSubtaskHasNoEpic() {
        Subtask subtask = new Subtask("Без эпика", "Не привязан", NEW, 9999);
        Integer id = manager.addNewSubtask(subtask);
        assertNull(id, "Подзадача должна возвращать null при отсутствии эпика.");
    }

    @Test
    void shouldReturnEmptyListForNonExistentEpicSubtasks() {
        assertNull(manager.getEpicSubtasks(9999), "Должен вернуть null для несуществующего Epic.");
    }

    @Test
    void shouldDetectCrossingWhenTask2StartsBeforeTask1Ends() {
        Task task1 = new Task("T1", "D1", NEW);
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(2)); // [10:00 - 12:00]

        Task task2 = new Task("T2", "D2", NEW);
        task2.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        task2.setDuration(Duration.ofHours(2)); // [11:00 - 13:00]


        manager.addNewTask(task1);
        manager.addNewTask(task2);

        assertTrue(manager.isCrossing(task1, task2), "Интервалы [10:00-12:00] и [11:00-13:00] должны пересекаться.");
    }

    @Test
    void shouldNotDetectCrossingWhenIntervalsAreSeparate() {
        Task task1 = new Task("T1", "D1", NEW);
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1)); // [10:00 - 11:00]

        Task task2 = new Task("T2", "D2", NEW);
        task2.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0).plusMinutes(1));
        task2.setDuration(Duration.ofHours(1)); // [11:01 - 12:01]

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        assertFalse(manager.isCrossing(task1, task2), "Интервалы [10:00-11:00] и [11:01-12:01] не должны пересекаться.");
    }

    @Test
    void shouldDetectCrossingWhenOneTaskIsContainedInAnother() {
        Task task1 = new Task("T1", "D1", NEW);
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(4)); // [10:00 - 14:00]

        Task task2 = new Task("T2", "D2", NEW);
        task2.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        task2.setDuration(Duration.ofHours(1)); // [11:00 - 12:00]

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        assertTrue(manager.isCrossing(task1, task2), "Интервал [11:00-12:00] содержится в [10:00-14:00] и должен пересекаться.");
    }

    @Test
    void shouldNotDetectCrossingAtStrictBoundary() {
        Task task1 = new Task("T1", "D1", NEW);
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1)); // [10:00 - 11:00]

        Task task2 = new Task("T2", "D2", NEW);
        task2.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        task2.setDuration(Duration.ofHours(1)); // [11:00 - 12:00]

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        assertFalse(manager.isCrossing(task1, task2), "Интервалы [10:00-11:00] и [11:00-12:00] не должны пересекаться (строгое неравенство).");
    }

    @Test
    void shouldDetectAnyCrossingInPrioritizedList() {
        Task t1 = new Task("T1", "D1", NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofHours(2));
        manager.addNewTask(t1);

        Task t2 = new Task("T2", "D2", NEW); // Пересекается с T1
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        t2.setDuration(Duration.ofHours(2));
        manager.addNewTask(t2);

        Task t3 = new Task("T3", "D3", NEW); // Не пересекается
        t3.setStartTime(LocalDateTime.of(2025, 1, 1, 14, 0));
        t3.setDuration(Duration.ofHours(1));
        manager.addNewTask(t3);

        assertTrue(manager.hasAnyCrossing(), "Должны быть обнаружены пересечения между T1 и T2.");

        manager.deleteTask(t2.getId());
        assertFalse(manager.hasAnyCrossing(), "Пересечений быть не должно.");
    }

    @Test
    void shouldHandleNullTimeTasksInCrossingCheck() {
        Task t1 = new Task("T1", "D1", NEW);
        Task t2 = new Task("T2", "D2", NEW);
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t2.setDuration(Duration.ofHours(1));

        manager.addNewTask(t1);
        manager.addNewTask(t2);

        assertFalse(manager.isCrossing(t1, t2), "Задачи без времени не должны пересекаться.");
    }
}