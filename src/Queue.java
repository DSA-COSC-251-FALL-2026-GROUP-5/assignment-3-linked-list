class Queue<T extends Comparable<T>> {

  LinkedList<T> linkedList;

  public Queue() {
    // 4.a. this takes O(1) time to instantiate. Also, because a linked list has
    // dynamic size, we won't use the arraySize variable
    linkedList = new LinkedList<T>();
  }

  public Queue(int arraySize) {
    // NOTE: Also, because a linked list has
    // dynamic size, we won't use the arraySize variable, this is just to maintain
    // compatibility
    linkedList = new LinkedList<T>();
  }

  // the plan is that we will insert to the end of the linked list which takes
  // O(1) time, and then pop from the front, which also takes O(1) time

  public void insert(T newItem) {
    // 4.b. insertion takes O(1) time because we're inserting it to the back of the
    // linked list
    this.linkedList.insertLast(newItem);
  }

  public T remove() {
    // 4.c. this also takes O(1) time because deletion of the head of the linked
    // list is still O(1)

    if (linkedList.isEmpty()) {
      return null;
    }
    T returnItem = peekFront();
    this.linkedList.deleteFirst();
    return returnItem;
  }

  public T peekFront() {
    // 4.d this takes O(1) because we're just accessing the head pointer's data
    if (linkedList.isEmpty()) {
      return null;
    }
    // NOTE: the front of the queue is the next to be popped out
    return linkedList.headLink.data;
  }

  public T peekRear() {
    // 4.e this takes O(1) becauese we're just accessing the tail pointer's data
    if (linkedList.isEmpty()) {
      return null;
    }
    return linkedList.tailLink.data;
  }

  public String toString() {
    // 4.f. to string traverses through the whole linked list, as such it takes O(n)
    // time
    return this.linkedList.toString();
  }

  public void display() {
    // 4.g. display calls toString which takes O(n) time, therefore, it's O(n) since
    // the other prints takes place in relatively constant time
    System.out.printf("Current Queue: %s\r\n", toString());
    System.out.printf("peekFront: %s\r\n", peekFront());
    System.out.printf("peekRear: %s\r\n", peekRear());
  }
}
