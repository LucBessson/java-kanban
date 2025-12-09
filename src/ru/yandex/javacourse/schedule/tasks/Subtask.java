package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    protected int epicId;

    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
        this.type = TaskType.subTask;

    }

    public Subtask(int id, String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration, int epicId){
        this(id, name, description, status, epicId);
        this.startTime = startTime;
        this.duration = duration;
    }

    public Subtask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
        this.type = TaskType.subTask;

    }



    @Override
    public String toFileString() {
        String joined = String.join(",", super.toFileString(), String.valueOf(epicId));
        return joined;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", epicId=" + epicId +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                '}';
    }
}
