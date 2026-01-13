package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.handlers.util.GsonFactory;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerPrioritizedTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer server = new HttpTaskServer(manager);
    Gson gson = GsonFactory.create();
    HttpClient client = HttpClient.newHttpClient();

    public HttpTaskManagerPrioritizedTest() throws IOException {
    }

    @BeforeEach
    void start() {
        manager.deleteTasks();
        manager.deleteEpics();
        manager.deleteSubtasks();
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    @Test
    void shouldReturnEmptyListIfNoTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasks = gson.fromJson(response.body(), List.class);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void shouldReturnTasksSortedByStartTime() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                Duration.ofMinutes(30));

        Task task2 = new Task("Task 2", "Desc",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 1, 1, 8, 0),
                Duration.ofMinutes(30));

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        HttpResponse<String> response =
                client.send(HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/prioritized"))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString());

        List<Task> tasks = gson.fromJson(response.body(), List.class);

        assertEquals(2, tasks.size());
    }

    @Test
    void taskWithoutStartTimeShouldNotBeReturned() throws IOException, InterruptedException {
        Task task = new Task("Task", "Desc", TaskStatus.NEW);
        manager.addNewTask(task);

        HttpResponse<String> response =
                client.send(HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/prioritized"))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString());

        List<Task> tasks = gson.fromJson(response.body(), List.class);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void subtaskShouldBeReturned() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask(
                "Sub",
                "Desc",
                TaskStatus.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(15),
                epic.getId()
        );

        manager.addNewSubtask(subtask);

        HttpResponse<String> response =
                client.send(HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/prioritized"))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString());

        List<Task> tasks = gson.fromJson(response.body(), List.class);
        assertEquals(1, tasks.size());
    }

    @Test
    void epicWithoutTimeShouldNotBeReturned() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);

        HttpResponse<String> response =
                client.send(HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/prioritized"))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString());

        List<Task> tasks = gson.fromJson(response.body(), List.class);
        assertTrue(tasks.isEmpty());
    }
}
