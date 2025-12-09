package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected Duration duration;
    protected LocalDateTime startTime;
    protected int id = 0;
    protected String name;
    protected TaskStatus status;
    protected TaskType type;
    protected String description;
    private boolean isInManager = false;

    public Task(int id, String name, String description, TaskStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.type = TaskType.task;
    }

    public Task(int id, String name, String description, TaskStatus status, LocalDateTime startTime, Duration duration) {
        this(id, name, description, status);
        this.startTime = startTime;
        this.duration = duration;
    }



    public Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.type = TaskType.task;
    }

    public LocalDateTime getEndTime() {
        LocalDateTime endTime;
        if (startTime == null && duration == null) return null;
        endTime = startTime.plus(duration);
        return endTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public String toFileString() {
        String start = (startTime != null) ? startTime.toString() : "???";
        String dur = (duration != null) ? String.valueOf(duration.toMinutes()) : "???";
        LocalDateTime endTime = getEndTime();
        String end = (endTime != null) ? endTime.toString() : "???";
        String[] line = new String[]{String.valueOf(id), String.valueOf(type), name, String.valueOf(status), description, start, dur, end};
        return String.join(",", line);
    }

    public void setInManager() {
        this.isInManager = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {

        if (isInManager) {
            System.out.println("Task already in manager and id cant be changed");
        } else
            this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public String toString() {
        return "Task{" + "id=" + id + ", name='" + name + '\'' + ", status='" + status + '\'' + ", description='" + description + '\'' + '}';
    }

    public void setStartTime(LocalDateTime startTime) {
        if(isInManager && type != TaskType.epic) throw new IllegalStateException("нельзя установить startTime, задача уже находится в менеджере");
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}
