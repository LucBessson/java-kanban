package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Task;

public class Node {
    Task task;
    Node prev;
    Node next;

    Node(Task task) {

        this.task = task;
    }
}
