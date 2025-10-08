package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Task;

import java.util.HashMap;
import java.util.List;

/**
 * In-memory history manager.
 *
 * @author Vladimir Ivanov
 * @noinspection checkstyle:Indentation
 */
public class InMemoryHistoryManager implements HistoryManager {

    private final MyLinkedList myLinkedList = new MyLinkedList();
    private final HashMap<Integer, Node> myHashMap = new HashMap<>();

    @Override
    public List<Task> getHistory() {
        return myLinkedList.getTasks();
    }

    /**
     * @noinspection checkstyle:Indentation
     */
    @Override
    public void remove(int id) {
        if (myHashMap.containsKey(id)) {
            removeNode(myHashMap.get(id));
            myHashMap.remove(id);
        }
    }
  public void clear() {
    for (int id : myHashMap.keySet()) {
      removeNode(myHashMap.get(id));
        }
    myHashMap.clear();
    }

    @Override
    public void addTask(Task task) {
        if (task == null) {
            return;
        }

        int id = task.getId();
        if (myHashMap.containsKey(id)) {
            removeNode(myHashMap.get(id));
        }
        Node node = myLinkedList.linkLast(task);
        myHashMap.put(id, node);
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        Node prevNode = node.prev;
        Node nextNode = node.next;

        if (prevNode == null) {
            myLinkedList.first = nextNode;
        } else {
            prevNode.next = nextNode;
        }

        if (nextNode == null) {
            myLinkedList.last = prevNode;
        } else {
            nextNode.prev = prevNode;
        }
    }


}
