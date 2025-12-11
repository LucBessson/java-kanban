package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

public class EpicTest {
    @Test
    public void testEqualityById() {
        Epic e0 = new Epic(1, "Test 1", "Testing task 1");
        Epic e1 = new Epic(1, "Test 2", "Testing task 2");
        assertEquals(e0, e1, "task and subentities should be compared by id");
    }

    @Test
    public void testSubtaskUniqueIds() {
        //TaskManager manager = Managers.getDefault();
        Epic epic = new Epic(0, "Epic 1", "Testing epic 1");
        //manager.addNewEpic(epic);
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);
        assertEquals(2, epic.subtaskIds.size(), "should add distinct subtask ids");
        epic.addSubtaskId(1);
        assertEquals(2, epic.subtaskIds.size(), "should not add same subtask id twice");
    }

    @Test
    public void testNotSelfAttaching() {
        Epic epic = new Epic(0, "Epic 1", "Testing epic 1");
        epic.addSubtaskId(0);
        assertEquals(0, epic.subtaskIds.size(), "epic should not add itself as subtask");
    }

    @Test
    void shouldRecalculateEpicTimes() {
        Epic epic = new Epic("Epic", "desc");
        TaskManager manager = manager = Managers.getDefault();
        int epicId = manager.addNewEpic(epic);

        Subtask s1 = new Subtask("s1", "d", NEW, epicId);
        s1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        s1.setDuration(Duration.ofMinutes(60));

        Subtask s2 = new Subtask("s2", "d", NEW, epicId);
        s2.setStartTime(LocalDateTime.of(2025, 1, 1, 12, 0));
        s2.setDuration(Duration.ofMinutes(30));

        manager.addNewSubtask(s1);
        manager.addNewSubtask(s2);

        Epic savedEpic = manager.getEpic(epicId);

        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), savedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2025, 1, 1, 12, 30), savedEpic.getEndTime());
        assertEquals(Duration.ofMinutes(90), savedEpic.getDuration());
    }

}
