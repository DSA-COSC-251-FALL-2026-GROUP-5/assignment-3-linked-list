class UsingQueue {
  public static void main(String[] args) {
    Queue<Integer> queue = new Queue<Integer>();
    queue.display();
    queue.insert(0);
    queue.insert(1);
    queue.insert(2);
    queue.insert(3);
    queue.display();
    System.out.println(queue.remove());
    System.out.println(queue.remove());
    System.out.println(queue.remove());
    System.out.println(queue.remove());
    queue.display();
  }
}
