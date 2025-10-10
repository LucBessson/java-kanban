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
    public void initHistoryManager(){
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testHistoricVersions(){
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should not be added");
    }

    @Test
    public void testHistoricVersionsByPointer(){
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "historic task should be stored");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(TaskStatus.IN_PROGRESS, historyManager.getHistory().get(0).getStatus(), "historic task should be changed");
    }

    @Test
    void addTaskMovesToEndAndNoDuplicates() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Should not have duplicates");
        assertEquals(2, history.get(0).getId(), "Task in wrong place");
        assertEquals(1, history.get(1).getId(), "Task in wrong place");
    }

    @Test
    void removeAndClearWorks() {
        Task task1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);
        // remove middle
        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Task has not removed");
        assertFalse(history.contains(task2), "Task has not removed");
        // clear
        historyManager.clear();
        assertTrue(historyManager.getHistory().isEmpty(),"History manager is not empty");
    }

    @Test
    void addNullIsIgnoredAndRemoveNonexistentDoesNothing() {
        historyManager.addTask(null);
        assertTrue(historyManager.getHistory().isEmpty(), "null cant be added");
        historyManager.remove(999);// removing not generate exception
    }

    @Test
    void linkedListIntegrityAfterRemovals() {
        Task t1 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task t2 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW);
        Task t3 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW);
        historyManager.addTask(t1);
        historyManager.addTask(t2);
        historyManager.addTask(t3);
        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(t1, t3), history, "Middle node must be removed correctly");
        historyManager.remove(3);
        historyManager.remove(1);
        assertTrue(historyManager.getHistory().isEmpty(), "All nodes should be removed");
    }
}
