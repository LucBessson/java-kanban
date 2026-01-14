package ru.yandex.javacourse.schedule.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String request = h.getRequestMethod();
            switch (request) {
                case "GET":
                    handleGet(h);
                    break;
                case "POST":
                    handlePost(h);
                    break;
                case "DELETE":
                    handleDelete(h);
                    break;
                default:
                    h.sendResponseHeaders(405, -1);
            }
        } catch (NotFoundException e) {
            sendNotFound(h);
        } catch (IllegalStateException e) {
            sendHasInteractions(h);
        } catch (Exception e) {
            sendInternalError(h);
        } finally {
            h.close();
        }
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        h.sendResponseHeaders(404, -1);
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        h.sendResponseHeaders(406, -1);
    }

    protected void sendInternalError(HttpExchange h) throws IOException {
        h.sendResponseHeaders(500, -1);
    }

    protected abstract void handleGet(HttpExchange h) throws IOException;

    protected abstract void handlePost(HttpExchange h) throws IOException;

    protected abstract void handleDelete(HttpExchange h) throws IOException;
}

