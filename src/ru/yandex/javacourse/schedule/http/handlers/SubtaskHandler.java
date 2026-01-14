package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.http.handlers.util.GsonFactory;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = GsonFactory.create();

    public SubtaskHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    protected void handleGet(HttpExchange h) throws IOException {
        String[] path = h.getRequestURI().getPath().split("/");
        if (path.length == 2) {
            sendText(h, gson.toJson(manager.getSubtasks()));
            return;
        }
        int id = Integer.parseInt(path[2]);
        Subtask subtask = manager.getSubtask(id);
        sendText(h, gson.toJson(subtask));
    }

    @Override
    protected void handlePost(HttpExchange h) throws IOException {
        String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);
        if (subtask.getId() == 0) {
            manager.addNewSubtask(subtask);
        } else {
            manager.updateSubtask(subtask);
        }
        h.sendResponseHeaders(201, -1);
    }

    @Override
    protected void handleDelete(HttpExchange h) throws IOException {
        String[] path = h.getRequestURI().getPath().split("/");
        if (path.length == 2) {
            manager.deleteSubtasks();
            h.sendResponseHeaders(200, -1);
            return;
        }
        int id = Integer.parseInt(path[2]);
        manager.deleteSubtask(id); // NotFoundException
        h.sendResponseHeaders(200, -1);
    }
}
