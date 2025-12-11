package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

public class Epic extends Task {
    protected ArrayList<Integer> subtaskIds = new ArrayList<>();
    protected LocalDateTime endTime;

    public Epic(int id, String name, String description) {
        super(id, name, description, NEW);
        this.type = TaskType.epic;
    }

    public Epic(int id, String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration, LocalDateTime endTime) {
        this(id, name, description, status);
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = endTime;
    }

    public Epic(String name, String description) {
        super(name, description, NEW);
        this.type = TaskType.epic;
    }

    public Epic(int id, String name, String description, TaskStatus status) {
        super(id, name, description, status);
        this.type = TaskType.epic;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toFileString() {
        String end = (endTime != null) ? endTime.toString() : "???";
        String joined = String.join(",", super.toFileString(), end);
        return joined;

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
        startTime = null;
        endTime = null;
    }

    public void removeSubtask(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    @Override
    public String toString() {
        return "Epic{" + "id=" + id + ", name='" + name + '\'' + ", status=" + status + ", description='" + description + '\'' + ", subtaskIds=" + subtaskIds + '}';
    }

    public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
    }



}
