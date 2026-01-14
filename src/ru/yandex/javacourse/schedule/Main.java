package ru.yandex.javacourse.schedule;

import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.util.List;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

public class Main {
    public static void main(String[] args) {
/*
        TaskManager manager = Managers.getDefault();

        // Создание
        Task task1 = new Task("Task #1", "Task1 description", NEW);
        Task task2 = new Task("Task #2", "Task2 description", IN_PROGRESS);
        final int taskId1 = manager.addNewTask(task1);
        final int taskId2 = manager.addNewTask(task2);

        Epic epic1 = new Epic("Epic #1", "Epic1 description");
        Epic epic2 = new Epic("Epic #2", "Epic2 description");
        final int epicId1 = manager.addNewEpic(epic1);
        final int epicId2 = manager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, epicId1);
        Subtask subtask2 = new Subtask("Subtask #2-1", "Subtask1 description", NEW, epicId1);
        Subtask subtask3 = new Subtask("Subtask #3-2", "Subtask1 description", DONE, epicId2);
        manager.addNewSubtask(subtask1);
        final Integer subtaskId2 = manager.addNewSubtask(subtask2);
        final Integer subtaskId3 = manager.addNewSubtask(subtask3);

        printAllTasks(manager);

        // Обновление
        final Task task = manager.getTask(taskId2);
        task.setStatus(DONE);
        manager.updateTask(task);
        System.out.println("CHANGE STATUS: Task2 IN_PROGRESS->DONE");
        System.out.println("Задачи:");
        for (Task t : manager.getTasks()) {
            System.out.println(t);
        }

        Subtask subtask = manager.getSubtask(subtaskId2);
        subtask.setStatus(DONE);
        manager.updateSubtask(subtask);
        System.out.println("CHANGE STATUS: Subtask2 NEW->DONE");
        subtask = manager.getSubtask(subtaskId3);
        subtask.setStatus(NEW);
        manager.updateSubtask(subtask);
        System.out.println("CHANGE STATUS: Subtask3 DONE->NEW");
        System.out.println("Подзадачи:");
        for (Task t : manager.getSubtasks()) {
            System.out.println(t);
        }

        System.out.println("Эпики:");
        for (Task e : manager.getEpics()) {
            System.out.println(e);
            for (Task t : manager.getEpicSubtasks(e.getId())) {
                System.out.println("--> " + t);
            }
        }
        final Epic epic = manager.getEpic(epicId1);
        epic.setStatus(NEW);
        manager.updateEpic(epic);
        System.out.println("CHANGE STATUS: Epic1 IN_PROGRESS->NEW");
        printAllTasks(manager);

        System.out.println("Эпики:");
        for (Task e : manager.getEpics()) {
            System.out.println(e);
            for (Task t : manager.getEpicSubtasks(e.getId())) {
                System.out.println("--> " + t);
            }
        }

        // Удаление
        System.out.println("DELETE: Task1");
        manager.deleteTask(taskId1);
        System.out.println("DELETE: Epic1");
        manager.deleteEpic(epicId1);
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);
//			System.out.println("--> Подзадачи эпика:");
            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
 */

        TaskManager manager = Managers.getDefault();

        // Создание задач и эпиков
        Task task1 = new Task("Task 1", "First simple task", NEW);
        Task task2 = new Task("Task 2", "Second simple task", NEW);
        int task1Id = manager.addNewTask(task1);
        int task2Id = manager.addNewTask(task2);

        Epic epic1 = new Epic("Epic 1", "Epic with 3 subtasks");
        Epic epic2 = new Epic("Epic 2", "Epic without subtasks");
        int epic1Id = manager.addNewEpic(epic1);
        int epic2Id = manager.addNewEpic(epic2);

        Subtask sub1 = new Subtask("Subtask 1", "Part 1", NEW, epic1Id);
        Subtask sub2 = new Subtask("Subtask 2", "Part 2", NEW, epic1Id);
        Subtask sub3 = new Subtask("Subtask 3", "Part 3", NEW, epic1Id);
        int sub1Id = manager.addNewSubtask(sub1);
        int sub2Id = manager.addNewSubtask(sub2);
        int sub3Id = manager.addNewSubtask(sub3);

        System.out.println("Created tasks and epics");
        System.out.println("Tasks: " + manager.getTasks());
        System.out.println("Epics: " + manager.getEpics());
        System.out.println("Subtasks: " + manager.getSubtasks());
        System.out.println();

        // Запрашиваем задачи в разном порядке и проверяем историю
        manager.getOptionalTask(task1Id);
        manager.getOptionalEpic(epic1Id);
        manager.getOptionalSubtask(sub2Id);
        manager.getOptionalSubtask(sub3Id);
        manager.getOptionalEpic(epic2Id);
        manager.getOptionalTask(task2Id);
        manager.getOptionalSubtask(sub1Id);
        manager.getOptionalEpic(epic1Id); // повторный запрос
        manager.getOptionalTask(task1Id); // повторный запрос

        printHistory(manager.getHistory(), "История после запросов в разном порядке");

        // Удаляем задачу, которая есть в истории
        manager.deleteTask(task1Id);
        printHistory(manager.getHistory(), "После удаления Task 1");

        // Удаляем эпик с тремя подзадачами
        manager.deleteEpic(epic1Id);
        printHistory(manager.getHistory(), "После удаления Epic 1 и его подзадач");


    }

    private static void printHistory(List<Task> history, String title) {
        System.out.println("------------ " + title + " ------------");
        if (history.isEmpty()) {
            System.out.println("История пуста.");
        } else {
            for (Task t : history) {
                System.out.println(t);
            }
        }
        System.out.println();
    }
}