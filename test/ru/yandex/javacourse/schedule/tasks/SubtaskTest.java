package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {
    @Test
    public void testEqualityById() {
        Subtask s0 = new Subtask(1, "Test 1", "Testing task 1", TaskStatus.NEW, 1);
        Subtask s1 = new Subtask(1, "Test 2", "Testing task 2", TaskStatus.IN_PROGRESS, 2);
        assertEquals(s0, s1, "task entities should be compared by id");
    }

    @Test
    public void testNotSelfAttaching() {
        TaskManager manager = Managers.getDefault();
        Subtask subtask = new Subtask(1, "Subtask 1", "Testing subtask 1", TaskStatus.NEW, 1);
        Epic epic = new Epic("Epic #1", "Epic1 description");
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);
        assertNotEquals(subtask.id, subtask.epicId, "subtask cannot be attached to itself");
    }

    @Test
    public void testSubtaskNotAddedWithoutEpic() {
        TaskManager manager = Managers.getDefault();
        Subtask subtask = new Subtask(1, "Subtask 1", "Testing subtask 1", TaskStatus.NEW, 1);
        assertThrows(
                NotFoundException.class,
                () -> manager.addNewSubtask(subtask),
                "subtask should not be added without Epic"
        );
    }


}
