package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldReturnEmptyListForEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой по умолчанию.");
    }

    @Test
    void addTaskShouldMoveToEndAndRemoveDuplicate() {
        Task t1 = new Task(1, "Test 1", "D1", TaskStatus.NEW);
        Task t2 = new Task(2, "Test 2", "D2", TaskStatus.NEW);
        Task t3 = new Task(3, "Test 3", "D3", TaskStatus.NEW);

        historyManager.addTask(t1);
        historyManager.addTask(t2);
        historyManager.addTask(t3);
        historyManager.addTask(t1);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "Не должно быть дубликатов.");
        assertEquals(t2, history.get(0), "t2 должно быть в начале.");
        assertEquals(t1, history.get(2), "t1 должно быть в конце.");
    }

    @Test
    void removeShouldHandleStartNode() {
        Task t1 = new Task(1, "Test 1", "D1", TaskStatus.NEW);
        Task t2 = new Task(2, "Test 2", "D2", TaskStatus.NEW);
        historyManager.addTask(t1);
        historyManager.addTask(t2); // [t1, t2]
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(t2, history.get(0), "t2 должно стать началом.");
    }

    @Test
    void removeShouldHandleMiddleNode() {
        Task t1 = new Task(1, "Test 1", "D1", TaskStatus.NEW);
        Task t2 = new Task(2, "Test 2", "D2", TaskStatus.NEW);
        Task t3 = new Task(3, "Test 3", "D3", TaskStatus.NEW);
        historyManager.addTask(t1);
        historyManager.addTask(t2);
        historyManager.addTask(t3);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(t1, history.get(0));
        assertEquals(t3, history.get(1), "t1 и t3 должны быть связаны.");
    }

    @Test
    void removeShouldHandleEndNode() {
        Task t1 = new Task(1, "Test 1", "D1", TaskStatus.NEW);
        Task t2 = new Task(2, "Test 2", "D2", TaskStatus.NEW);
        historyManager.addTask(t1);
        historyManager.addTask(t2);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(t1, history.get(0), "t1 должно стать концом.");
    }

    @Test
    void addNullIsIgnoredAndRemoveNonexistentDoesNothing() {
        historyManager.addTask(null);
        assertTrue(historyManager.getHistory().isEmpty(), "null не должен быть добавлен.");

        assertDoesNotThrow(() -> historyManager.remove(999), "Удаление несуществующего ID не должно бросать исключения.");
    }

    @Test
    void clearShouldEmptyHistory() {
        Task t1 = new Task(1, "Test 1", "D1", TaskStatus.NEW);
        historyManager.addTask(t1);
        assertFalse(historyManager.getHistory().isEmpty());

        historyManager.clear();

        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть очищена.");
    }
}