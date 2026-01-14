package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.http.handlers.util.GsonFactory;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = GsonFactory.create();

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    protected void handleGet(HttpExchange h) throws IOException {
        String json = gson.toJson(manager.getPrioritizedTasks());
        sendText(h, json);
    }

    @Override
    protected void handlePost(HttpExchange h) throws IOException {
        h.sendResponseHeaders(405, -1);
    }

    @Override
    protected void handleDelete(HttpExchange h) throws IOException {
        h.sendResponseHeaders(405, -1);
    }
}
