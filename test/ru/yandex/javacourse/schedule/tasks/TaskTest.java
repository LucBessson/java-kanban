package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {
    @Test
    public void testEqualityById() {
        Task t0 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task t1 = new Task(1, "Test 2", "Testing task 2", TaskStatus.IN_PROGRESS);
        assertEquals(t0, t1, "task entities should be compared by id");
    }

    @Test
    void shouldCalculateEndTimeForTask() {
        Task task = new Task("Test", "desc", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task.setDuration(Duration.ofMinutes(90));

        assertEquals(LocalDateTime.of(2025, 1, 1, 11, 30), task.getEndTime());
    }


}
