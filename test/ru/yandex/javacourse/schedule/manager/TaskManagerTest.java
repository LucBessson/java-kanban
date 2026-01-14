package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
        assertThrows(NotFoundException.class, () -> manager.getTask(id), "Задача не была удалена.");
        assertTrue(manager.getHistory().isEmpty(), "Задача не была удалена из истории.");
    }

    @Test
    void shouldDeleteEpicAndItsSubtasks() {
        Epic epic = createEpic();
        Subtask sub = createSubtask(epic.getId(), NEW);
        manager.getEpic(epic.getId());
        manager.getSubtask(sub.getId());
        manager.deleteEpic(epic.getId());

        assertThrows(NotFoundException.class, () -> manager.getEpic(epic.getId()), "Эпик не был удален");

        assertThrows(NotFoundException.class, () -> manager.getSubtask(sub.getId()), "Подзадача не была удалена");

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
    void shouldThrowExceptionWhenSubtaskHasNoEpic() {
        Subtask subtask = new Subtask("Без эпика", "Не привязан", NEW, 9999);

        assertThrows(NotFoundException.class, () -> manager.addNewSubtask(subtask), "Подзадача должна выкидывать исключение при отсутствии эпика.");
    }

    @Test
    void shouldThrowsExceptionForNonExistentEpicSubtasks() {
        assertThrows(NotFoundException.class, () -> manager.getEpicSubtasks(9999), " должен выкидывать исключение при отсутствии эпика.");
    }

    @Test
    void tasksShouldBeSortedByStartTime() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task t1 = new Task("A", "t1", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(30));

        Task t2 = new Task("B", "t2", TaskStatus.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 0));
        t2.setDuration(Duration.ofMinutes(30));

        Task t3 = new Task("C", "t3", TaskStatus.NEW);
        t3.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0)); // same start as t1
        t3.setDuration(Duration.ofMinutes(15));

        int id2 = manager.addNewTask(t2); // earliest start (09:00) -> id 1
        int id1 = manager.addNewTask(t1); // 10:00 -> id 2
        int id3 = manager.addNewTask(t3); // 10:00 -> id 3

        assertEquals(1, id2);
        assertEquals(2, id1);
        assertEquals(3, id3);

        List<Task> sorted = manager.getPrioritizedTasks();

        assertEquals(3, sorted.size());


        // САМЫЙ РАННИЙ — 9:00 (t2)
        assertEquals(t2.getId(), sorted.get(0).getId());

        // Далее — 10:00. Между t1(id=2) и t3(id=3) сортировка должна идти по id.
        assertEquals(t1.getId(), sorted.get(1).getId());
        assertEquals(t3.getId(), sorted.get(2).getId());
    }

    @Test
    void shouldNotBeAnyCrossingInPrioritizedList() {
        Task t1 = new Task("T1", "D1", NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofHours(2));
        manager.addNewTask(t1);

        Task t2 = new Task("T2", "D2", NEW); // Пересекается с T1
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        t2.setDuration(Duration.ofHours(2));

        assertThrows(IllegalStateException.class, () -> manager.addNewTask(t2), "Должны быть обнаружены пересечения между T1 и T2.");

        Task t3 = new Task("T3", "D3", NEW); // Не пересекается
        t3.setStartTime(LocalDateTime.of(2025, 1, 1, 14, 0));
        t3.setDuration(Duration.ofHours(1));
        manager.addNewTask(t3);

        assertFalse(manager.hasAnyCrossing(), "Пересечений быть не должно.");
    }
}