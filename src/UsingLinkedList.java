class UsingLinkedList {
  public static void main(String[] args) {
    LinkedList<Integer> linkedList = new LinkedList<Integer>();
    linkedList.insertAt(0, 0);
    linkedList.insertAt(1, 1);
    linkedList.insertAt(2, 2);
    // linkedList.deleteFirst();
    // linkedList.deleteFirst();
    linkedList.deleteAt(1);
    // linkedList.insertAt(0, 5);
    /*
     * linkedList.insertLast(1);
     * linkedList.insertLast(2);
     * linkedList.insertLast(3);
     * linkedList.insertLast(4);
     */
    linkedList.display();
  }
}
