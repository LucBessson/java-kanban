package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final String fileHead = "id,type,name,status,description,epic";
    private final   String fileName;

    public static void main(String[] args) {
        String fileName = "tasks.csv"; // файл сохранения
        FileBackedTaskManager manager = new FileBackedTaskManager(fileName);
        // --- создаём задачи ---
        Task task1 = new Task("Покупки", "Купить продукты в магазине", TaskStatus.NEW);
        Task task2 = new Task("Учёба", "Сделать домашнее задание по Java", TaskStatus.IN_PROGRESS);
        manager.addNewTask(task1);
        manager.addNewTask(task2);
        // --- создаём эпик ---
        Epic epic1 = new Epic("Переезд", "Собрать вещи и переехать");
        int epicId = manager.addNewEpic(epic1);
        // --- создаём подзадачи ---
        Subtask sub1 = new Subtask("Упаковать вещи", "Коробки одежда техника", TaskStatus.NEW, epicId);
        Subtask sub2 = new Subtask("Заказать грузовик", "Найти подходящую компанию", TaskStatus.DONE, epicId);
        manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);

        System.out.println("=== Исходный менеджер ===");
        System.out.println("Задачи: " + manager.getTasks());
        System.out.println("Эпики: " + manager.getEpics());
        System.out.println("Подзадачи: " + manager.getSubtasks());
        // --- загружаем новый менеджер из файла ---
        FileBackedTaskManager loadedManager = new FileBackedTaskManager(fileName);
        System.out.println("\n=== Менеджер, загруженный из файла ===");
        System.out.println("Задачи: " + loadedManager.getTasks());
        System.out.println("Эпики: " + loadedManager.getEpics());
        System.out.println("Подзадачи: " + loadedManager.getSubtasks());
        // --- проверка совпадения данных ---
        boolean tasksEqual = manager.getTasks().equals(loadedManager.getTasks());
        boolean epicsEqual = manager.getEpics().equals(loadedManager.getEpics());
        boolean subsEqual = manager.getSubtasks().equals(loadedManager.getSubtasks());

        System.out.println("\nПроверка совпадения данных:");
        System.out.println("Задачи совпадают: " + tasksEqual);
        System.out.println("Эпики совпадают: " + epicsEqual);
        System.out.println("Подзадачи совпадают: " + subsEqual);
    }

    public FileBackedTaskManager(String fileName) {
        super();
        this.fileName = fileName;
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String head = br.readLine();
            if (head == null) {
                return;
            }
            if(br.ready() && ! head.equals(fileHead)) {
                throw new IOException("Неверный заголовок файла");
            }
            while (br.ready()) {
                String line = br.readLine();
                fromFileString(line);
            }
        } catch (IOException e) {
            System.out.println("Произошла ошибка во время чтения файла.");
        }
    }

    public int fromFileString(String line) {
        String[] lines = line.split(",");
        int id = Integer.parseInt(lines[0]);
        TaskType type = TaskType.valueOf(lines[1]);
        String name = lines[2];
        TaskStatus status = TaskStatus.valueOf(lines[3]);
        String description = lines[4];
        switch (type) {
            case task: {
                Task task = new Task(id, name, description, status);
                super.addOldTask(task);
                return id;
            }
            case epic: {
                Epic epic = new Epic(id, name, description, status);
                super.addOldEpic(epic);
                return id;
            }
            case subTask: {
                int epic = Integer.parseInt(lines[5]);
                Subtask subtask = new Subtask(id, name, description, status, epic);
                super.addOldSubtask(subtask);
                return id;
            }
            default:
                throw new IllegalStateException("Неизвестный тип задачи: " + type);
        }
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer subtaskId = super.addNewSubtask(subtask);
        save();
        return subtaskId;
    }
    @Override
    public int addNewTask(Task task) {
        int taskId = super.addNewTask(task);
        save();
        return taskId;
    }
    public int addNewEpic(Epic epic) {
        int epicId = super.addNewEpic(epic);
        save();
        return epicId;
    }
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(fileHead + "\n");
            List<Task> allTasks = new ArrayList<>();
            allTasks.addAll(super.getTasks());
            allTasks.addAll(super.getEpics());
            allTasks.addAll(super.getSubtasks());
            for (Task value : allTasks) {
                writer.write(value.toFileString() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Произошла ошибка во время записи файла.");
        }
    }

    private void save(Task task) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(task.toFileString() + "\n");
        } catch (IOException e) {
            System.out.println("Произошла ошибка во время записи файла.");
        }
    }
}
