package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
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

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerEpicsTest {

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
    // POST /epics
    // ======================

    @Test
    void shouldCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description");

        HttpResponse<String> response = sendPost("/epics", epic);

        assertEquals(201, response.statusCode());

        List<Epic> epics = manager.getEpics();
        assertEquals(1, epics.size());
        assertEquals("Epic 1", epics.get(0).getName());
    }

    @Test
    void shouldUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Old name", "Old description");
        manager.addNewEpic(epic);

        Epic updated = new Epic(
                epic.getId(),
                "New name",
                "New description"
        );

        HttpResponse<String> response = sendPost("/epics", updated);

        assertEquals(201, response.statusCode());

        Epic saved = manager.getEpic(epic.getId());
        assertEquals("New name", saved.getName());
    }

    // ======================
    // GET /epics
    // ======================

    @Test
    void shouldGetAllEpics() throws IOException, InterruptedException {
        manager.addNewEpic(new Epic("A", "A"));
        manager.addNewEpic(new Epic("B", "B"));

        HttpResponse<String> response = sendGet("/epics");

        assertEquals(200, response.statusCode());

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(2, epics.length);
    }

    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);

        HttpResponse<String> response = sendGet("/epics/" + epic.getId());

        assertEquals(200, response.statusCode());

        Epic fromJson = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic.getId(), fromJson.getId());
    }

    @Test
    void shouldReturn404WhenEpicNotFound() throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet("/epics/999");

        assertEquals(404, response.statusCode());
    }

    // ======================
    // DELETE /epics
    // ======================

    @Test
    void shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask(
                "Sub",
                "Sub desc",
                TaskStatus.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(5),
                epic.getId()
        );
        manager.addNewSubtask(subtask);

        HttpResponse<String> response = sendDelete("/epics/" + epic.getId());

        assertEquals(200, response.statusCode());

        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    void shouldDeleteAllEpics() throws IOException, InterruptedException {
        manager.addNewEpic(new Epic("A", "A"));
        manager.addNewEpic(new Epic("B", "B"));

        HttpResponse<String> response = sendDelete("/epics");

        assertEquals(200, response.statusCode());
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void shouldReturn404WhenDeleteMissingEpic() throws IOException, InterruptedException {
        HttpResponse<String> response = sendDelete("/epics/999");

        assertEquals(404, response.statusCode());
    }

    // ======================
    // GET /epics/{id}/subtasks
    // ======================

    @Test
    void shouldGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask(
                "Sub",
                "Sub desc",
                TaskStatus.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(10),
                epic.getId()
        );
        manager.addNewSubtask(subtask);

        HttpResponse<String> response =
                sendGet("/epics/" + epic.getId() + "/subtasks");

        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(1, subtasks.length);
        assertEquals(subtask.getId(), subtasks[0].getId());
    }

    // ======================
    // helpers
    // ======================

    private HttpResponse<String> sendPost(String path, Object body)
            throws IOException, InterruptedException {

        String json = gson.toJson(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendGet(String path)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDelete(String path)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .DELETE()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
