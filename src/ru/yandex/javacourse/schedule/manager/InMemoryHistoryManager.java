package ru.yandex.javacourse.schedule.manager;

import java.util.*;


import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {


	private final MyLinkedList myLinkedList = new MyLinkedList();

	private  final HashMap<Integer, Node> myHashMap = new HashMap<>();



	@Override
	public List<Task> getHistory() {
		return myLinkedList.getTasks();

	}

	@Override
	public void remove(int id) {

		if(myHashMap.containsKey(id)) {
			removeNode(myHashMap.get(id));
			myHashMap.remove(id);
		}

	}

	public void clear(){

		for(int id : myHashMap.keySet() ){

			removeNode(myHashMap.get(id));

		}

		myHashMap.clear();

	}

	@Override
	public void addTask(Task task) {

		if (task == null) {
			return;
		}


		//history.add(task);
		int id = task.getId();
		Node node = myLinkedList.linkLast(task);
		if(myHashMap.containsKey(id)) removeNode(myHashMap.get(id));
		myHashMap.put(id, node);


	}


	private  void removeNode(Node node){

		if (node == null) {
			return;
		}

		Node prevNode = node.prev;
		Node nextNode = node.next;




		if(prevNode == null){
			myLinkedList.first = nextNode;
		} else prevNode.next = nextNode;

		if(nextNode == null){
			myLinkedList.last = prevNode;
		} else nextNode.prev = prevNode;


	}
}

class MyLinkedList {

	Node first;

	Node last;


	public Node linkLast(Task task) {
		Node newNode = new Node(task);


		if (last == null) {
			first = newNode;
		} else {
			last.next = newNode;
			newNode.prev = last;
		}
		last = newNode;
		return newNode;
	}

	public List<Task> getTasks() {
		List<Task> tasks = new ArrayList<>();
		Node current = first;

		while (current != null) {
			tasks.add(current.task);
			current = current.next;
		}

		return tasks;
	}



}


