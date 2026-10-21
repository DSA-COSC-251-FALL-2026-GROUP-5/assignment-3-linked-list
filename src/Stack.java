class Stack<T extends Comparable<T>> {
  // this array is only internally used

  LinkedList<T> linkedList;

  public Stack() {
    // 3.a this takes O(1) time to initialize
    this.linkedList = new LinkedList<T>();
  }

  public Stack(int arraySize) {
    // NOTE: although the stack was initilized with a size, we don't need anymore
    // because we are using a linked list, but perhaps we would want to maintain
    // compatibility with the previous API
    this.linkedList = new LinkedList<T>();
  }

  public void push(T newItem) {
    // 3.b this takes O(1) time because inserting to the front of the linked list
    // takes O(1) time because we already have a pointer to the first element in the
    // linked list and we can also access the next element
    linkedList.insertFirst(newItem);
  }

  public T pop() {
    // 3.c pop requires O(1) time because peek requires O(1) time, and deletion of
    // the first item also takes O(1) time
    T returnItem = peek();
    if (returnItem == null) {
      return null;
    }
    linkedList.deleteFirst();
    return returnItem;
  }

  public T peek() {
    // 3.d peek requires O(1) time because we're just accessing
    if (linkedList.isEmpty()) {
      return null;
    }
    // if the linked list isn't empty, then just grab the head element and return it
    return linkedList.headLink.data;
  }

  public String toString() {
    // 3.e this takes O(n) time because we must traverse through the linked list
    // (toString takes O(n)) time
    return linkedList.toString();
  }

  public void display() {
    // 6.f this takes O(n) time because it calls toString is O(n), and toString
    // takes o(n) time
    System.out.printf("Current stack: %s\r\n", toString());
    System.out.printf("Next to pop: %s\r\n", peek());
  }
}
