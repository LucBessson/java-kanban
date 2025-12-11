package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final String fileHead = "id,type,name,status,description,startTime,duration,endTime,epic";
    private final String fileName;

    public FileBackedTaskManager(String fileName) {
        super();
        this.fileName = fileName;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String head = br.readLine();
            if (head == null) {
                return;
            }
            if (br.ready() && !head.equals(fileHead)) {
                throw new IOException("Неверный заголовок файла");
            }
            while (br.ready()) {
                String line = br.readLine();
                fromFileString(line);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        String fileName = "timedTasks.csv"; // файл сохранения
        FileBackedTaskManager manager = Managers.loadFromFile(fileName);

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
        FileBackedTaskManager loadedManager = Managers.loadFromFile(fileName);
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

    public int fromFileString(String line) {
        String[] lines = line.split(",");
        int id = Integer.parseInt(lines[0]);
        TaskType type = TaskType.valueOf(lines[1]);
        String name = lines[2];
        TaskStatus status = TaskStatus.valueOf(lines[3]);
        String description = lines[4];
        LocalDateTime startTime = lines[5].equals("???") ? null : LocalDateTime.parse(lines[5]);
        Duration duration = lines[6].equals("???") ? null : Duration.ofMinutes(Long.parseLong(lines[6]));
                switch (type) {
            case task: {
                Task task = new Task(id, name, description, status, startTime, duration);
                super.addFileTask(task);
                return id;
            }
            case epic: {
                LocalDateTime endTime = lines[7].equals("???") ? null : LocalDateTime.parse(lines[7]);
                Epic epic = new Epic(id, name, description, status, startTime, duration, endTime);
                super.addFileEpic(epic);
                return id;
            }
            case subTask: {
                int epicId = Integer.parseInt(lines[8]);
                Subtask subtask = new Subtask(id, name, description, status, startTime, duration, epicId);
                super.addFileSubtask(subtask);
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
            System.out.println("Произошла ошибка во время записи файла.\n" + e.getMessage());

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
