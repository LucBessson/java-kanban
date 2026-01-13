package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.handlers.util.GsonFactory;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskManagerHistoryTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer server = new HttpTaskServer(manager);
    Gson gson = GsonFactory.create();
    HttpClient client = HttpClient.newHttpClient();

    public HttpTaskManagerHistoryTest() throws IOException {
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
    void shouldReturnEmptyHistory() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/history");

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> history = gson.fromJson(response.body(), List.class);
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void shouldAddTaskToHistory() throws IOException, InterruptedException {
        Task task = new Task(
                "Task",
                "Desc",
                TaskStatus.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(10)
        );
        manager.addNewTask(task);

        // запрос задачи → попадает в историю
        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        HttpResponse<String> historyResponse =
                client.send(
                        HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/history"))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                );

        List<Task> history =
                gson.fromJson(historyResponse.body(), List.class);

        assertEquals(1, history.size());
    }

    @Test
    void shouldKeepCorrectOrder() throws IOException, InterruptedException {
        Task task1 = new Task("T1", "D1", TaskStatus.NEW,
                 LocalDateTime.now(), Duration.ofMinutes(10));

        Task task2 = new Task("T2", "D2", TaskStatus.NEW,
                 LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(10));

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/" + task1.getId()))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/" + task2.getId()))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> response =
                client.send(HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/history"))
                                .GET().build(),
                        HttpResponse.BodyHandlers.ofString());

        List<Task> history = gson.fromJson(response.body(), List.class);

        assertEquals(2, history.size());
    }

    @Test
    void shouldMoveTaskToEndIfRequestedAgain() throws IOException, InterruptedException {
        Task task1 = new Task("T1", "D1", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));

        Task task2 = new Task("T2", "D2", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(20), Duration.ofMinutes(10));

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/" + task1.getId()))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/" + task2.getId()))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/" + task1.getId()))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> response =
                client.send(HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/history"))
                                .GET().build(),
                        HttpResponse.BodyHandlers.ofString());

        List<Task> history = gson.fromJson(response.body(), List.class);

        assertEquals(2, history.size());
    }

    @Test
    void deletedTaskShouldDisappearFromHistory() throws IOException, InterruptedException {
        Task task = new Task("Task", "Desc", TaskStatus.NEW,
                LocalDateTime.now(), Duration.ofMinutes(10));

        manager.addNewTask(task);

        client.send(HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        manager.deleteTask(task.getId());

        HttpResponse<String> response =
                client.send(HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:8080/history"))
                                .GET().build(),
                        HttpResponse.BodyHandlers.ofString());

        List<Task> history = gson.fromJson(response.body(), List.class);

        assertTrue(history.isEmpty());
    }
}
