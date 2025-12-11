package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeEach
    public void initManager() {
        manager = (InMemoryTaskManager) Managers.getDefault();
    }

    @Test
    void changingTaskIdAfterAddLeadsToInconsistency() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        int assigned = manager.addNewTask(task);

        assertEquals(assigned, task.getId(), "ID после добавления должен совпадать с назначенным");
        task.setId(999);
        assertEquals(assigned, task.getId(), "ID после добавления не должен меняться");

        Task oldTaskId = manager.getTask(assigned);
        assertEquals(assigned, oldTaskId.getId(), "ID задачи в менеджере должен соответствовать ключу");
    }
}