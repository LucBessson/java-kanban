package ru.yandex.javacourse.schedule.tasks;

import java.util.ArrayList;
import java.util.List;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

public class Epic extends Task {
    protected ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(int id, String name, String description) {
        super(id, name, description, NEW);
        this.type = TaskType.epic;
    }

    public Epic(String name, String description) {
        super(name, description, NEW);
        this.type = TaskType.epic;
    }

    public Epic(int id, String name, String description, TaskStatus status) {
        super(id, name, description, status);
        this.type = TaskType.epic;
    }
    public void addSubtaskId(int id) {
        if (!subtaskIds.contains(id) && id != this.id) {
            subtaskIds.add(id);
        }
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void cleanSubtaskIds() {
        subtaskIds.clear();
    }

    public void removeSubtask(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}
