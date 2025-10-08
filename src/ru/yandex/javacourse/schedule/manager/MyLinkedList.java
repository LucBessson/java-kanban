package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MyLinkedList {

    Node first;

    Node last;


    public Node linkLast(Task task) {
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

    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = first;

        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }

        return tasks;
    }


}
