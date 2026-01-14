package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.handlers.util.GsonFactory;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
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

class HttpTaskServerSubtasksTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson;
    private HttpClient client;
    private Epic epic;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        gson = GsonFactory.create();
        client = HttpClient.newHttpClient();
        server.start();

        epic = new Epic("Epic", "Epic description");
        manager.addNewEpic(epic);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    // ======================
    // POST /subtasks
    // ======================

    @Test
    void shouldCreateSubtask() throws IOException, InterruptedException {
        Subtask subtask = createSubtask(epic.getId());

        HttpResponse<String> response = sendPost("/subtasks", subtask);

        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals(epic.getId(), subtasks.get(0).getEpicId());
    }

    @Test
    void shouldUpdateSubtask() throws IOException, InterruptedException {
        Subtask subtask = createSubtask(epic.getId());
        manager.addNewSubtask(subtask);

        Subtask updated = new Subtask(subtask.getId(), "Updated name", "Updated desc", TaskStatus.IN_PROGRESS, subtask.getStartTime(), subtask.getDuration(), epic.getId());

        HttpResponse<String> response = sendPost("/subtasks", updated);

        assertEquals(201, response.statusCode());

        Subtask saved = manager.getSubtask(subtask.getId());
        assertEquals("Updated name", saved.getName());
        assertEquals(TaskStatus.IN_PROGRESS, saved.getStatus());
    }

    @Test
    void shouldReturn404WhenEpicNotFound() throws IOException, InterruptedException {
        Subtask subtask = createSubtask(999);

        HttpResponse<String> response = sendPost("/subtasks", subtask);

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturn406OnTimeIntersection() throws IOException, InterruptedException {
        Subtask s1 = createSubtask(epic.getId());
        manager.addNewSubtask(s1);

        Subtask s2 = new Subtask("Conflict", "Conflict desc", TaskStatus.NEW, s1.getStartTime(), s1.getDuration(), epic.getId());

        HttpResponse<String> response = sendPost("/subtasks", s2);

        assertEquals(406, response.statusCode());
    }

    // ======================
    // GET /subtasks
    // ======================

    @Test
    void shouldGetAllSubtasks() throws IOException, InterruptedException {
        manager.addNewSubtask(new Subtask("A", "A", TaskStatus.NEW, epic.getId()));
        manager.addNewSubtask(new Subtask("B", "B", TaskStatus.NEW, epic.getId()));

        HttpResponse<String> response = sendGet("/subtasks");

        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }

    @Test
    void shouldGetSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = createSubtask(epic.getId());
        manager.addNewSubtask(subtask);

        HttpResponse<String> response = sendGet("/subtasks/" + subtask.getId());

        assertEquals(200, response.statusCode());

        Subtask fromJson = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), fromJson.getId());
    }

    @Test
    void shouldReturn404WhenSubtaskNotFound() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet("/subtasks/999");

        assertEquals(404, response.statusCode());
    }

    // ======================
    // DELETE /subtasks
    // ======================

    @Test
    void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = createSubtask(epic.getId());
        manager.addNewSubtask(subtask);

        HttpResponse<String> response = sendDelete("/subtasks/" + subtask.getId());

        assertEquals(200, response.statusCode());
        assertTrue(manager.getSubtasks().isEmpty());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void shouldDeleteAllSubtasks() throws IOException, InterruptedException {
        manager.addNewSubtask(new Subtask("A", "A", TaskStatus.NEW, epic.getId()));
        manager.addNewSubtask(new Subtask("B", "B", TaskStatus.NEW, epic.getId()));

        HttpResponse<String> response = sendDelete("/subtasks");

        assertEquals(200, response.statusCode());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    void shouldReturn404WhenDeleteMissingSubtask() throws IOException, InterruptedException {
        HttpResponse<String> response = sendDelete("/subtasks/999");

        assertEquals(404, response.statusCode());
    }

    // ======================
    // helpers
    // ======================

    private Subtask createSubtask(int epicId) {
        return new Subtask("Subtask", "Subtask description", TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epicId);
    }

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
