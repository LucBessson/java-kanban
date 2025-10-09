package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryLinkedList {
    private final Map<Integer, Node> nodeMap = new HashMap<>();
    private Node first;
    private Node last;

    public void addTask(Task task) {
        if (task == null) {
            return;
        }
        int id = task.getId();
        remove(id);
        Node newNode = linkLast(task);
        nodeMap.put(id, newNode);
    }

    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node == null) {
            return;
        }
        Node prev = node.prev;
        Node next = node.next;
        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
        }
        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
        }
    }

    public void clear() {
        first = null;
        last = null;
        nodeMap.clear();
    }

    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = first;
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks;
    }

    private Node linkLast(Task task) {
        Node newNode = new Node(task);
        if (last == null) {
            first = newNode;
        } else {
            last.next = newNode;
            newNode.prev = last;
        }
        last = newNode;
        return newNode;
    }

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }
}
