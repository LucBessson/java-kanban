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

class HttpTaskServerTasksTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        gson = GsonFactory.create();
        client = HttpClient.newHttpClient();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    // ======================
    // POST /tasks
    // ======================

    @Test
    void shouldCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Description", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10));

        HttpResponse<String> response = sendPost("/tasks", task);

        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size());
        assertEquals("Task 1", tasks.get(0).getName());
    }

    @Test
    void shouldUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Old name", "Description", TaskStatus.NEW);
        manager.addNewTask(task);

        Task updated = new Task(task.getId(), "New name", "Updated description", TaskStatus.IN_PROGRESS);

        HttpResponse<String> response = sendPost("/tasks", updated);

        assertEquals(201, response.statusCode());

        Task saved = manager.getTask(task.getId());
        assertEquals("New name", saved.getName());
        assertEquals(TaskStatus.IN_PROGRESS, saved.getStatus());
    }

    @Test
    void shouldReturn406WhenTasksIntersect() throws IOException, InterruptedException {
        Task t1 = new Task("Task 1", "Desc", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10));

        Task t2 = new Task("Task 2", "Desc", TaskStatus.NEW, LocalDateTime.now().plusMinutes(5), Duration.ofMinutes(10));

        sendPost("/tasks", t1);
        HttpResponse<String> response = sendPost("/tasks", t2);

        assertEquals(406, response.statusCode());
        assertEquals(1, manager.getTasks().size());
    }

    // ======================
    // GET /tasks
    // ======================

    @Test
    void shouldGetAllTasks() throws IOException, InterruptedException {
        manager.addNewTask(new Task("A", "A", TaskStatus.NEW));
        manager.addNewTask(new Task("B", "B", TaskStatus.NEW));

        HttpResponse<String> response = sendGet("/tasks");

        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length);
    }

    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task", "Desc", TaskStatus.NEW);
        manager.addNewTask(task);

        HttpResponse<String> response = sendGet("/tasks/" + task.getId());

        assertEquals(200, response.statusCode());

        Task fromJson = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), fromJson.getId());
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet("/tasks/999");

        assertEquals(404, response.statusCode());
    }

    // ======================
    // DELETE /tasks
    // ======================

    @Test
    void shouldDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task", "Desc", TaskStatus.NEW);
        manager.addNewTask(task);

        HttpResponse<String> response = sendDelete("/tasks/" + task.getId());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void shouldDeleteAllTasks() throws IOException, InterruptedException {
        manager.addNewTask(new Task("A", "A", TaskStatus.NEW));
        manager.addNewTask(new Task("B", "B", TaskStatus.NEW));

        HttpResponse<String> response = sendDelete("/tasks");

        assertEquals(200, response.statusCode());
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void shouldReturn404WhenDeleteMissingTask() throws IOException, InterruptedException {
        HttpResponse<String> response = sendDelete("/tasks/999");

        assertEquals(404, response.statusCode());
    }

    // ======================
    // helpers
    // ======================

    private HttpResponse<String> sendPost(String path, Object body) throws IOException, InterruptedException {

        String json = gson.toJson(body);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080" + path)).POST(HttpRequest.BodyPublishers.ofString(json)).build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080" + path)).GET().build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDelete(String path) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080" + path)).DELETE().build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
