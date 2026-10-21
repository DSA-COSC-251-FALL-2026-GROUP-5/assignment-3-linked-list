class UsingStack {
  public static void main(String[] args) {
    Stack<Integer> stack = new Stack<Integer>();
    stack.display();
    stack.push(0);
    stack.push(1);
    stack.push(2);
    stack.push(3);
    stack.display();
    System.out.println(stack.pop());
    System.out.println(stack.pop());
    System.out.println(stack.pop());
    System.out.println(stack.pop());
    stack.display();
  }
}
