class UsingLinkedList {
  public static void main(String[] args) {
    LinkedList<Integer> linkedList = new LinkedList<Integer>();
    linkedList.insertFirst(1);
    linkedList.insertFirst(2);
    linkedList.insertFirst(3);
    linkedList.insertLast(4);
    linkedList.display();
    linkedList.deleteKey(4);
    linkedList.deleteKey(2);
    System.out.println(linkedList.deleteKey(1));
    linkedList.display();
  }
}
