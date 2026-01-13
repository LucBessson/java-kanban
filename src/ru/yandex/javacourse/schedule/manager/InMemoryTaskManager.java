package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Set<Task> timedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime).thenComparing(Task::getId));
    private int generatorId = 0;

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(timedTasks);
    }

    public boolean hasAnyCrossing() {
        List<Task> tasks = getPrioritizedTasks();

        return IntStream.range(0, tasks.size() - 1)
                .anyMatch(i -> isCrossing(tasks.get(i), tasks.get(i + 1)));
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(this.tasks.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Epic not found: " + epicId);
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .collect(Collectors.toList());
    }

    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Task not found: " + id);
        }
        historyManager.addTask(task);
        return task;
    }


    @Override
    public Subtask getSubtask(int id) {
        final Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Task not found: " + id);
        }
        historyManager.addTask(subtask);
        return subtask;
    }

    @Override
    public Epic getEpic(int epicId) {
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Epic not found: " + epicId);
        }
        historyManager.addTask(epic);
        return epic;
    }

    @Override
    public Optional<Task> getOptionalTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.addTask(task);
        }
        return Optional.ofNullable(task);
    }

    @Override
    public Optional<Subtask> getOptionalSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.addTask(subtask);
        }
        return Optional.ofNullable(subtask);
    }

    @Override
    public Optional<Epic> getOptionalEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.addTask(epic);
        }
        return Optional.ofNullable(epic);
    }

    @Override
    public int addNewTask(Task task) {
        task.setId(++generatorId);
        return addTaskCommon(task);
    }

    protected int addFileTask(Task task) {
        if(task.getId() > generatorId) generatorId = task.getId();
        return addTaskCommon(task);
    }

    @Override
    public int addNewEpic(Epic epic) {
        epic.setId(++generatorId);
        return addEpicCommon(epic);
    }

    protected int addFileEpic(Epic epic) {
        if(epic.getId() > generatorId) generatorId = epic.getId();
        return addEpicCommon(epic);
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Epic not found: " + epicId);
        }
        subtask.setId(++generatorId);
        return addSubtaskCommon(subtask, epic);
    }

    public Integer addFileSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Epic not found: " + epicId);
        }
        if(subtask.getId() > generatorId) generatorId = subtask.getId();
        return addSubtaskCommon(subtask, epic);
    }

    @Override
    public void updateTask(Task task) {
        final int id = task.getId();
        final Task old = tasks.get(id);
        if (old == null) {
            throw new NotFoundException("Task not found: " + id);
        }
        timedTasks.remove(old);
        addToTimedTasks(task);
        if (hasAnyCrossing()) {
            timedTasks.remove(task);
            addToTimedTasks(old);
            throw new IllegalStateException("Задачи пересекаются по времени");
        }

        task.setInManager();
        tasks.put(id, task);
    }

    @Override
    public void updateEpic(Epic epic) {
        int epicId = epic.getId();
        final Epic savedEpic = epics.get(epic.getId());
        if (savedEpic == null) {
            throw new NotFoundException("Epic not found: " + epicId);
        }
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        final int id = subtask.getId();
        final Subtask oldSubtask = subtasks.get(id);
        if (oldSubtask == null) {
            throw new NotFoundException("Subtask not found: " + id);
        }
        final int epicId = subtask.getEpicId();
        final Epic savedEpic = epics.get(epicId);
        if (savedEpic == null) {
            throw new NotFoundException("Epic not found: " + epicId);
        }
        timedTasks.remove(oldSubtask);
        addToTimedTasks(subtask);
        if (hasAnyCrossing()) {
            timedTasks.remove(subtask);
            addToTimedTasks(oldSubtask);
            throw new IllegalStateException("Подзадачи пересекаются по времени");
        }
        subtask.setInManager();
        subtasks.put(id, subtask);
        updateEpicTime(savedEpic);
        updateEpicStatus(epicId);
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task == null) {
            throw new NotFoundException("Task not found: " + id);
        }
        timedTasks.remove(task);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            throw new NotFoundException("Epic not found: " + id);
        }
        historyManager.remove(id);
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.remove(subtaskId);
            if (subtask != null) {
                timedTasks.remove(subtask);
                historyManager.remove(subtaskId);
            }
        }
    }


    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            throw new NotFoundException("subtask not found: " + id);
        }
        timedTasks.remove(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(id);
        historyManager.remove(id);
        updateEpicTime(epic);
        updateEpicStatus(epic.getId());

    }

    @Override
    public void deleteTasks() {
        tasks.clear();
        historyManager.clear();
    }

    @Override
    public void deleteSubtasks() {
        epics.values().stream()
                .peek(Epic::cleanSubtaskIds)
                .forEach(epic -> updateEpicStatus(epic.getId()));

        new ArrayList<>(subtasks.keySet()).stream().forEach(this::deleteSubtask);

        subtasks.clear();
    }

    @Override
    public void deleteEpics() {
        new ArrayList<>(epics.keySet()).stream().forEach(this::deleteEpic);

        new ArrayList<>(subtasks.keySet()).stream().forEach(this::deleteSubtask);

        subtasks.clear();
        epics.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void addToTimedTasks(Task task) {
        if (task.getStartTime() != null) {
            timedTasks.add(task);
        }
    }

    private void updateEpicTime(Epic epic) {
        List<Integer> subIds = epic.getSubtaskIds();
        if (subIds.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }
        List<Subtask> subs = subIds.stream()
                .map(subtasks::get)
                .toList();

        epic.setDuration(
                subs.stream()
                        .map(Subtask::getDuration)
                        .filter(Objects::nonNull)
                        .reduce(Duration.ZERO, Duration::plus));

        epic.setStartTime(
                subs.stream()
                        .map(Subtask::getStartTime)
                        .filter(Objects::nonNull)
                        .min(LocalDateTime::compareTo)
                        .orElse(null));

        epic.setEndTime(
                subs.stream()
                        .map(Subtask::getEndTime)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null));
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        List<Integer> subs = epic.getSubtaskIds();
        if (subs.isEmpty()) {
            epic.setStatus(NEW);
            return;
        }
        Set<TaskStatus> statuses = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .map(Subtask::getStatus)
                .collect(Collectors.toSet());

        // если только NEW
        if (statuses.size() == 1 && statuses.contains(NEW)) {
            epic.setStatus(NEW);
            return;
        }

        if (statuses.size() == 1 && statuses.contains(DONE)) {
            epic.setStatus(DONE);
            return;
        }

        epic.setStatus(IN_PROGRESS);
    }

    private boolean isCrossing(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getEndTime() == null)
            return false;
        if (task2.getStartTime() == null || task2.getEndTime() == null)
            return false;

        return task1.getStartTime().isBefore(task2.getEndTime()) && task2.getStartTime().isBefore(task1.getEndTime());
    }

    private Integer addSubtaskCommon(Subtask subtask, Epic epic) {
        addToTimedTasks(subtask);
        if (hasAnyCrossing()) {
            timedTasks.remove(subtask);
            throw new IllegalStateException("Задачи пересекаются по времени");
        }
        int id = subtask.getId();
        subtask.setInManager();
        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        updateEpicTime(epic);
        updateEpicStatus(epic.getId());
        return id;
    }

    private int addEpicCommon(Epic epic) {
        int id = epic.getId();
        epic.setInManager();
        epics.put(id, epic);
        return id;
    }

    private int addTaskCommon(Task task) {
        addToTimedTasks(task);
        if (hasAnyCrossing()) {
            timedTasks.remove(task);
            throw new IllegalStateException("Задачи пересекаются по времени");
        }
        int id = task.getId();
        task.setInManager();
        tasks.put(id, task);
        return id;
    }
}
