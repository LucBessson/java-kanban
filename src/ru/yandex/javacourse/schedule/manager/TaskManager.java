package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.util.List;
import java.util.Optional;

/**
 * Task manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public interface TaskManager {
    List<Task> getTasks();

    //void updateEpicTime(Epic epic);

    boolean hasAnyCrossing();

    boolean isCrossing(Task task1, Task task2);

    List<Task> getPrioritizedTasks();

    List<Subtask> getSubtasks();

    List<Epic> getEpics();

    List<Subtask> getEpicSubtasks(int epicId);

    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    Optional<Task> getOptionalTask(int id);
    Optional<Subtask> getOptionalSubtask(int id);
    Optional<Epic> getOptionalEpic(int id);

    int addNewTask(Task task);

    int addNewEpic(Epic epic);

    Integer addNewSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    void deleteTasks();

    void deleteSubtasks();

    void deleteEpics();

    List<Task> getHistory();
}
