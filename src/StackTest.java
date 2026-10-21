/**
 * Unit tests for Stack.java, based on the requirements in
 * assignment_details.pdf (section 6: Stack Class (LIFO)).
 *
 * These tests use Java assert statements, so they must be run with
 * assertions enabled:
 *
 * ./build && ./run_test StackTest
 */
class StackTest {
  private static int passed = 0;
  private static int failed = 0;

  private static void check(boolean condition, String message) {
    assert condition : message;
  }

  private static void runTest(String name, Runnable test) {
    try {
      test.run();
      passed++;
      System.out.printf("PASS: %s%n", name);
    } catch (AssertionError | RuntimeException e) {
      failed++;
      System.out.printf("FAIL: %s -- %s%n", name, e.getMessage());
    }
  }

  public static void main(String[] args) {
    boolean assertsEnabled = false;
    assert assertsEnabled = true;
    if (!assertsEnabled) {
      System.out.println(
          "WARNING: assertions are disabled, every check will silently pass.");
      System.out.println("Run with: ./run_test StackTest");
    }

    runTest("empty stack returns null on pop/peek",
        StackTest::testEmptyReturnsNull);
    runTest("push then peek", StackTest::testPushAndPeek);
    runTest("LIFO order", StackTest::testLifoOrder);
    runTest("interleaved push and pop", StackTest::testInterleavedPushPop);
    runTest("exact capacity boundary", StackTest::testExactCapacityBoundary);
    runTest("auto resize", StackTest::testAutoResize);
    runTest("alternating push and pop", StackTest::testAlternatingPushPop);
    runTest("reuse after emptying", StackTest::testReuseAfterEmptying);
    runTest("toString", StackTest::testToString);
    runTest("toString down to empty", StackTest::testToStringDownToEmpty);
    runTest("large volume", StackTest::testLargeVolume);
    runTest("generic with String items", StackTest::testGenericWithStrings);
    runTest("display runs without errors", StackTest::testDisplay);

    System.out.printf("%nStackTest summary: %d passed, %d failed%n",
        passed, failed);
    if (failed > 0) {
      System.exit(1);
    }
  }

  // 6.c/6.d: pop() and peek() should return null when the stack is empty
  private static void testEmptyReturnsNull() {
    Stack<Integer> stack = new Stack<Integer>(5);
    check(stack.pop() == null, "pop() should return null on an empty stack");
    check(stack.peek() == null, "peek() should return null on an empty stack");
    check(stack.pop() == null,
        "pop() should still return null after repeated calls");
  }

  // 6.b/6.d: peek() returns the top item without removing it
  private static void testPushAndPeek() {
    Stack<Integer> stack = new Stack<Integer>(5);
    stack.push(42);
    check(stack.peek() == 42, "peek() should return the last pushed item");
    check(stack.peek() == 42, "peek() should not remove the item");
    check(stack.pop() == 42, "pop() should return the item seen by peek()");
    check(stack.peek() == null,
        "peek() should return null after the stack is emptied");
  }

  // 6.b/6.c: last in, first out
  private static void testLifoOrder() {
    Stack<Integer> stack = new Stack<Integer>(5);
    stack.push(1);
    stack.push(2);
    stack.push(3);
    check(stack.pop() == 3, "pop() should return 3 (last pushed) first");
    check(stack.pop() == 2, "pop() should return 2 next");
    check(stack.pop() == 1, "pop() should return 1 (first pushed) last");
    check(stack.pop() == null,
        "pop() should return null once the stack is empty");
  }

  private static void testInterleavedPushPop() {
    Stack<Integer> stack = new Stack<Integer>(5);
    stack.push(1);
    stack.push(2);
    check(stack.pop() == 2, "pop() should return the most recently pushed item");
    stack.push(3);
    check(stack.pop() == 3, "pop() should return the newly pushed item");
    check(stack.pop() == 1, "pop() should return the remaining item");
    check(stack.pop() == null, "pop() should return null when empty");
  }

  // pushing into a stack that is exactly at capacity must trigger the
  // resize and still preserve the stack contents
  private static void testExactCapacityBoundary() {
    Stack<Integer> stack = new Stack<Integer>(3);
    stack.push(1);
    stack.push(2);
    stack.push(3); // exactly full
    check(stack.peek() == 3,
        "boundary test: peek() should return 3 when exactly full");
    stack.push(4); // triggers a resize from 3 to 6 elements
    check(stack.peek() == 4,
        "boundary test: peek() should return 4 after resizing at the boundary");
    check(stack.pop() == 4, "boundary test: pop() should return 4 first");
    check(stack.pop() == 3, "boundary test: pop() should return 3 next");
    check(stack.pop() == 2, "boundary test: pop() should return 2 next");
    check(stack.pop() == 1, "boundary test: pop() should return 1 last");
    check(stack.pop() == null, "boundary test: pop() should return null when empty");
  }

  // project requirement 3: the internal array should resize automatically
  // when it becomes full
  private static void testAutoResize() {
    Stack<Integer> stack = new Stack<Integer>(2);
    for (int i = 1; i <= 10; i++) {
      stack.push(i);
    }
    check(stack.peek() == 10,
        "peek() should return the last pushed item after resizing");
    for (int i = 10; i >= 1; i--) {
      check(stack.pop() == i,
          "pop() should preserve LIFO order after resizing (expected " + i + ")");
    }
    check(stack.pop() == null,
        "pop() should return null after emptying a resized stack");
  }

  // repeatedly fill and empty a single slot so that popIndex oscillates
  // around the boundaries of the internal array
  private static void testAlternatingPushPop() {
    Stack<Integer> stack = new Stack<Integer>(2);
    for (int i = 1; i <= 100; i++) {
      stack.push(i);
      check(stack.peek() == i,
          "alternating test: peek() should return " + i);
      check(stack.pop() == i,
          "alternating test: pop() should return " + i);
      check(stack.pop() == null,
          "alternating test: pop() should return null when the stack is empty");
    }
  }

  // the stack should behave correctly after being completely emptied
  private static void testReuseAfterEmptying() {
    Stack<Integer> stack = new Stack<Integer>(2);
    stack.push(1);
    stack.push(2);
    check(stack.pop() == 2, "reuse test: pop() should return 2");
    check(stack.pop() == 1, "reuse test: pop() should return 1");
    check(stack.pop() == null, "reuse test: pop() should return null when empty");
    stack.push(3);
    stack.push(4);
    stack.push(5);
    check(stack.peek() == 5, "reuse test: peek() should return 5 after reuse");
    check(stack.pop() == 5, "reuse test: pop() should return 5 after reuse");
    check(stack.pop() == 4, "reuse test: pop() should return 4 after reuse");
    check(stack.pop() == 3, "reuse test: pop() should return 3 after reuse");
    check(stack.pop() == null, "reuse test: pop() should return null when empty");
  }

  // 6.e: toString should show the stack contents from bottom to top
  private static void testToString() {
    Stack<Integer> stack = new Stack<Integer>(5);
    check(stack.toString().equals("[]"),
        "toString() of an empty stack should be \"[]\"");
    stack.push(1);
    stack.push(2);
    stack.push(3);
    check(stack.toString().equals("[3, 2, 1]"),
        "toString() should be \"[3, 2, 1]\" but was \"" + stack.toString() + "\"");
    stack.pop();
    check(stack.toString().equals("[2, 1]"),
        "toString() should be \"[2, 1]\" after one pop but was \""
            + stack.toString() + "\"");
  }

  private static void testToStringDownToEmpty() {
    Stack<Integer> stack = new Stack<Integer>(5);
    stack.push(1);
    stack.push(2);
    stack.push(3);
    check(stack.toString().equals("[3, 2, 1]"), "toString() after pushing 1, 2, 3");
    stack.pop();
    check(stack.toString().equals("[2, 1]"), "toString() after popping once");
    stack.pop();
    check(stack.toString().equals("[1]"), "toString() after popping twice");
    stack.pop();
    check(stack.toString().equals("[]"),
        "toString() should be \"[]\" after popping everything");
  }

  // stress the resize logic with many doublings
  private static void testLargeVolume() {
    Stack<Integer> stack = new Stack<Integer>(2);
    int n = 5000;
    for (int i = 0; i < n; i++) {
      stack.push(i);
    }
    check(stack.peek() == n - 1,
        "large volume test: peek() should return the last pushed item");
    for (int i = n - 1; i >= 0; i--) {
      check(stack.pop() == i,
          "large volume test: pop() should return " + i + " in LIFO order");
    }
    check(stack.pop() == null,
        "large volume test: pop() should return null when empty");
  }

  // project requirement 1: the class must be generic
  private static void testGenericWithStrings() {
    Stack<String> stack = new Stack<String>(2);
    stack.push("a");
    stack.push("b");
    stack.push("c"); // forces a resize
    check(stack.peek().equals("c"), "peek() should return \"c\"");
    check(stack.pop().equals("c"), "pop() should return \"c\"");
    check(stack.pop().equals("b"), "pop() should return \"b\"");
    check(stack.pop().equals("a"), "pop() should return \"a\"");
    check(stack.pop() == null, "pop() should return null when empty");
  }

  // 6.f: display() should run without errors
  private static void testDisplay() {
    Stack<Integer> stack = new Stack<Integer>(2);
    stack.push(1);
    stack.push(2);
    stack.display();
    stack.pop();
    stack.display();
  }
}
