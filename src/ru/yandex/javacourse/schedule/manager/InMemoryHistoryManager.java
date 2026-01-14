package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Task;

import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final HistoryLinkedList historyLinkedList = new HistoryLinkedList();

    @Override
    public List<Task> getHistory() {
        return historyLinkedList.getTasks();
    }

    @Override
    public void remove(int id) {
        historyLinkedList.remove(id);
    }

    @Override
    public void addTask(Task task) {
        historyLinkedList.addTask(task);
    }

    @Override
    public void clear() {
        historyLinkedList.clear();
    }
}
