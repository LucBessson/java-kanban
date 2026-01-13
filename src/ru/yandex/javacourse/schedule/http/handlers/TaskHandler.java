package ru.yandex.javacourse.schedule.http.handlers;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.http.handlers.util.GsonFactory;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson = GsonFactory.create();

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
    }
    protected void handleDelete(HttpExchange h) throws IOException {
        String[] splitStrings = h.getRequestURI().getPath().split("/");

        if(splitStrings.length == 2) {
            manager.deleteTasks();
            h.sendResponseHeaders(200, 0);
            return;
        }
        int id = Integer.parseInt(splitStrings[2]);
        manager.deleteTask(id); // если нет — NotFoundException
        h.sendResponseHeaders(200, 0);
    }
    protected void handlePost(HttpExchange h) throws IOException {
        String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);

        if (task.getId() == 0) {
            manager.addNewTask(task);
        } else {
            manager.updateTask(task); // если нет — NotFoundException
        }
        h.sendResponseHeaders(201, 0);
    }
    protected void handleGet(HttpExchange h) throws IOException {
        String[] splitStrings = h.getRequestURI().getPath().split("/");

        if(splitStrings.length == 2) {
            String jsonResponse = gson.toJson(manager.getTasks());
            sendText(h, jsonResponse);
            return;
        }

        int id = Integer.parseInt(splitStrings[2]);
        Task task = manager.getTask(id);
        sendText(h, gson.toJson(task));
    }
}
