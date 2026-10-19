class UsingDoublyLinkedList {
  public static void main(String[] args) {
    DoublyLinkedList<Integer> doublyLinkedList = new DoublyLinkedList<Integer>();
    doublyLinkedList.insertLast(0);
    doublyLinkedList.insertLast(1);
    doublyLinkedList.insertLast(2);
    doublyLinkedList.insertFirst(-1);
    doublyLinkedList.insertAt(0, -2);
    doublyLinkedList.insertAt(1, -3);
    doublyLinkedList.insertAt(6, 3);

    doublyLinkedList.displayForward();
    doublyLinkedList.deleteLast();
    doublyLinkedList.deleteFirst();
    doublyLinkedList.deleteAt(0);
    doublyLinkedList.deleteAt(3);
    doublyLinkedList.deleteAt(1);
    doublyLinkedList.displayForward();

    System.out.printf("index of -1: %s\r\n", doublyLinkedList.find(-1));
  }
}
