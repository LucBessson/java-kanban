package ru.yandex.javacourse.schedule.http.handlers;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.http.handlers.util.GsonFactory;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = GsonFactory.create();

    public EpicHandler(TaskManager manager) {
        this.manager = manager;
    }

    protected void handleDelete(HttpExchange h) throws IOException {
        String[] splitStrings = h.getRequestURI().getPath().split("/");

        if(splitStrings.length == 2) {
            manager.deleteEpics();
            h.sendResponseHeaders(200, 0);
            return;
        }

        int id = Integer.parseInt(splitStrings[2]);
        manager.deleteEpic(id); // если нет — NotFoundException
        h.sendResponseHeaders(200, 0);
    }

    protected void handlePost(HttpExchange h) throws IOException {
        String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic.getId() == 0) {
            manager.addNewEpic(epic);
        } else {
            manager.updateEpic(epic);
        }

        h.sendResponseHeaders(201, -1);
    }

    protected void handleGet(HttpExchange h) throws IOException {
        String[] splitStrings = h.getRequestURI().getPath().split("/");

        if(splitStrings.length == 2) {
            String jsonResponse = gson.toJson(manager.getEpics());
            sendText(h, jsonResponse);
            return;
        }
        int id = Integer.parseInt(splitStrings[2]);
        if(splitStrings.length == 3) {
            Epic epic = manager.getEpic(id); // если нет — NotFoundException
            sendText(h, gson.toJson(epic));
            return;
        }
        if (splitStrings.length == 4 && "subtasks".equals(splitStrings[3])) {
            sendText(h, gson.toJson(manager.getEpicSubtasks(id)));
        }
    }
}
