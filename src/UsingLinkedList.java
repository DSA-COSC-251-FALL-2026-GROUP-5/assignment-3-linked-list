class UsingLinkedList {
  public static void main(String[] args) {
    LinkedList<Integer> linkedList = new LinkedList<Integer>();
    /*
     * linkedList.insertFirst(1);
     * linkedList.insertFirst(2);
     * linkedList.insertFirst(3);
     */
    linkedList.insertLast(4);
    linkedList.insertLast(5);
    linkedList.insertAt(0, 0);
    linkedList.insertAt(1, 1);
    linkedList.insertAt(2, 2);
    linkedList.insertAt(6, 6);
    linkedList.insertAt(5, 5);
    linkedList.display();
    linkedList.deleteAt(10);
    linkedList.display();
    System.out.println(linkedList.find(6));
  }
}
