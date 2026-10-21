/**
 * Unit tests for Queue.java, based on the requirements in
 * assignment_details.pdf (section 7: Queue Class (FIFO)).
 *
 * These tests use Java assert statements, so they must be run with
 * assertions enabled:
 *
 *   ./build && ./run_test QueueTest
 *
 * Note: two tests at the bottom document a bug in peekRear() when the
 * internal array is exactly full -- they will fail until peekRear()
 * uses a non-negative modulo, e.g. (queueHead - 1 + arr.length)
 * % arr.length.
 */
import java.util.ArrayDeque;
import java.util.Random;

class QueueTest {
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
      System.out.println("Run with: ./run_test QueueTest");
    }

    runTest("empty queue returns null on remove/peekFront/peekRear",
        QueueTest::testEmptyReturnsNull);
    runTest("FIFO order", QueueTest::testFifoOrder);
    runTest("peekFront and peekRear", QueueTest::testPeekFrontAndRear);
    runTest("single element", QueueTest::testSingleElement);
    runTest("circular wrap-around", QueueTest::testCircularWrapAround);
    runTest("exact capacity boundary", QueueTest::testExactCapacityBoundary);
    runTest("auto resize", QueueTest::testAutoResize);
    runTest("resize after wrap-around", QueueTest::testResizeAfterWrapAround);
    runTest("alternating insert and remove", QueueTest::testAlternatingInsertRemove);
    runTest("reuse after emptying", QueueTest::testReuseAfterEmptying);
    runTest("toString", QueueTest::testToString);
    runTest("toString after wrap-around", QueueTest::testToStringAfterWrapAround);
    runTest("large volume", QueueTest::testLargeVolume);
    runTest("randomized test against a reference queue",
        QueueTest::testRandomizedAgainstOracle);
    runTest("generic with String items", QueueTest::testGenericWithStrings);
    runTest("display runs without errors", QueueTest::testDisplay);

    // known bug: peekRear() crashes whenever the internal array is
    // exactly full (queueHead has wrapped to 0, so
    // (queueHead - 1) % arr.length == -1)
    runTest("peekRear when queue is exactly full", QueueTest::testPeekRearWhenFull);
    runTest("peekRear after partially draining a full queue",
        QueueTest::testPeekRearAfterPartialDrain);

    System.out.printf("%nQueueTest summary: %d passed, %d failed%n",
        passed, failed);
    if (failed > 0) {
      System.exit(1);
    }
  }

  // 7.c/7.d/7.e: remove(), peekFront() and peekRear() should return
  // null when the queue is empty
  private static void testEmptyReturnsNull() {
    Queue<Integer> queue = new Queue<Integer>(5);
    check(queue.remove() == null, "remove() should return null on an empty queue");
    check(queue.peekFront() == null,
        "peekFront() should return null on an empty queue");
    check(queue.peekRear() == null,
        "peekRear() should return null on an empty queue");
    check(queue.remove() == null,
        "remove() should still return null after repeated calls");
  }

  // 7.b/7.c: first in, first out
  private static void testFifoOrder() {
    Queue<Integer> queue = new Queue<Integer>(5);
    queue.insert(1);
    queue.insert(2);
    queue.insert(3);
    check(queue.remove() == 1, "remove() should return 1 (first inserted) first");
    check(queue.remove() == 2, "remove() should return 2 next");
    check(queue.remove() == 3, "remove() should return 3 last");
    check(queue.remove() == null,
        "remove() should return null once the queue is empty");
  }

  // 7.d/7.e: peeking should not remove anything
  private static void testPeekFrontAndRear() {
    Queue<Integer> queue = new Queue<Integer>(5);
    queue.insert(1);
    queue.insert(2);
    queue.insert(3);
    check(queue.peekFront() == 1,
        "peekFront() should return the first inserted item");
    check(queue.peekRear() == 3,
        "peekRear() should return the last inserted item");
    check(queue.peekFront() == 1, "peekFront() should not remove the item");
    check(queue.peekRear() == 3, "peekRear() should not remove the item");
    check(queue.remove() == 1,
        "remove() should still return items in FIFO order after peeking");
    check(queue.remove() == 2, "remove() should return 2 next");
    check(queue.remove() == 3, "remove() should return 3 last");
  }

  private static void testSingleElement() {
    Queue<Integer> queue = new Queue<Integer>(3);
    queue.insert(99);
    check(queue.peekFront() == 99, "peekFront() should return the only item");
    check(queue.peekRear() == 99, "peekRear() should return the only item");
    check(queue.remove() == 99, "remove() should return the only item");
    check(queue.remove() == null, "remove() should return null after draining");
    check(queue.peekFront() == null, "peekFront() should return null when empty");
    check(queue.peekRear() == null, "peekRear() should return null when empty");
  }

  // the queue is implemented as a circular array, so both indices must
  // wrap around correctly
  private static void testCircularWrapAround() {
    Queue<Integer> queue = new Queue<Integer>(3);
    queue.insert(1);
    queue.insert(2);
    check(queue.remove() == 1, "remove() should return 1");
    queue.insert(3); // queueHead wraps around to index 0
    check(queue.peekFront() == 2,
        "peekFront() should return 2 after the head wrapped around");
    check(queue.remove() == 2,
        "remove() should return 2 after the head wrapped around");
    check(queue.remove() == 3,
        "remove() should return 3 after the head wrapped around");
    queue.insert(4); // queueTail wraps around to index 0
    check(queue.remove() == 4,
        "remove() should return 4 after both indices wrapped around");
    check(queue.remove() == null, "remove() should return null when empty");
  }

  // draining and refilling to exactly the capacity must trigger the
  // resize and still preserve the FIFO order
  private static void testExactCapacityBoundary() {
    Queue<Integer> queue = new Queue<Integer>(3);
    queue.insert(1);
    queue.insert(2);
    queue.insert(3); // exactly full, queueHead wraps to 0
    check(queue.remove() == 1, "boundary test: remove() should return 1");
    queue.insert(4); // fills the queue up again
    check(queue.peekFront() == 2,
        "boundary test: peekFront() should return 2");
    queue.insert(5); // triggers a resize from 3 to 6 elements
    check(queue.remove() == 2, "boundary test: remove() should return 2");
    check(queue.remove() == 3, "boundary test: remove() should return 3");
    check(queue.remove() == 4, "boundary test: remove() should return 4");
    check(queue.remove() == 5, "boundary test: remove() should return 5");
    check(queue.remove() == null,
        "boundary test: remove() should return null when empty");
  }

  // project requirement 3: the internal array should resize automatically
  // when it becomes full
  private static void testAutoResize() {
    Queue<Integer> queue = new Queue<Integer>(2);
    for (int i = 1; i <= 10; i++) {
      queue.insert(i);
    }
    check(queue.peekFront() == 1, "peekFront() should return 1 after resizing");
    check(queue.peekRear() == 10, "peekRear() should return 10 after resizing");
    for (int i = 1; i <= 10; i++) {
      check(queue.remove() == i,
          "remove() should preserve FIFO order after resizing (expected " + i + ")");
    }
    check(queue.remove() == null,
        "remove() should return null after emptying a resized queue");
  }

  // resizing must preserve the FIFO order even when the elements are
  // wrapped around the end of the internal array
  private static void testResizeAfterWrapAround() {
    Queue<Integer> queue = new Queue<Integer>(3);
    queue.insert(1);
    queue.insert(2);
    queue.insert(3);
    check(queue.remove() == 1, "remove() should return 1");
    check(queue.remove() == 2, "remove() should return 2");
    // queueTail = 2 and queueHead = 0, so the next insert triggers a
    // resize while the remaining elements are wrapped around the array
    queue.insert(4);
    queue.insert(5);
    queue.insert(6); // triggers a resize from 3 to 6 elements
    queue.insert(7);
    for (int i = 3; i <= 7; i++) {
      check(queue.remove() == i,
          "remove() should preserve FIFO order across a wrap-around resize "
              + "(expected " + i + ")");
    }
    check(queue.remove() == null, "remove() should return null when empty");
  }

  // repeatedly insert one item and remove it, so that queueHead and
  // queueTail walk around the circular array many times
  private static void testAlternatingInsertRemove() {
    Queue<Integer> queue = new Queue<Integer>(3);
    for (int i = 1; i <= 100; i++) {
      queue.insert(i);
      check(queue.peekFront() == i,
          "alternating test: peekFront() should return " + i);
      check(queue.peekRear() == i,
          "alternating test: peekRear() should return " + i);
      check(queue.remove() == i,
          "alternating test: remove() should return " + i);
      check(queue.remove() == null,
          "alternating test: remove() should return null when empty");
    }
  }

  // the queue should behave correctly after being completely emptied
  private static void testReuseAfterEmptying() {
    Queue<Integer> queue = new Queue<Integer>(2);
    queue.insert(1);
    queue.insert(2);
    check(queue.remove() == 1, "reuse test: remove() should return 1");
    check(queue.remove() == 2, "reuse test: remove() should return 2");
    check(queue.remove() == null, "reuse test: remove() should return null when empty");
    queue.insert(3);
    queue.insert(4);
    queue.insert(5); // forces a resize
    check(queue.peekFront() == 3, "reuse test: peekFront() should return 3");
    check(queue.peekRear() == 5, "reuse test: peekRear() should return 5");
    check(queue.remove() == 3, "reuse test: remove() should return 3");
    check(queue.remove() == 4, "reuse test: remove() should return 4");
    check(queue.remove() == 5, "reuse test: remove() should return 5");
    check(queue.remove() == null, "reuse test: remove() should return null when empty");
  }

  // 7.f: toString should show the queue contents from front to rear
  private static void testToString() {
    Queue<Integer> queue = new Queue<Integer>(5);
    check(queue.toString().equals("[]"),
        "toString() of an empty queue should be \"[]\"");
    queue.insert(1);
    queue.insert(2);
    queue.insert(3);
    check(queue.toString().equals("[1, 2, 3]"),
        "toString() should be \"[1, 2, 3]\" but was \"" + queue.toString() + "\"");
    queue.remove();
    check(queue.toString().equals("[2, 3]"),
        "toString() should be \"[2, 3]\" after one removal but was \""
            + queue.toString() + "\"");
  }

  private static void testToStringAfterWrapAround() {
    Queue<Integer> queue = new Queue<Integer>(3);
    queue.insert(1);
    queue.insert(2);
    queue.insert(3);
    queue.remove(); // 1
    queue.remove(); // 2
    queue.insert(4);
    queue.insert(5); // the queue is full again, wrapped around the array
    check(queue.toString().equals("[3, 4, 5]"),
        "toString() should be \"[3, 4, 5]\" after wrapping around but was \""
            + queue.toString() + "\"");
    queue.insert(6); // triggers a resize
    check(queue.toString().equals("[3, 4, 5, 6]"),
        "toString() should be \"[3, 4, 5, 6]\" after resizing but was \""
            + queue.toString() + "\"");
    queue.remove();
    check(queue.toString().equals("[4, 5, 6]"),
        "toString() should be \"[4, 5, 6]\" after one removal but was \""
            + queue.toString() + "\"");
  }

  // stress the resize logic with many doublings
  private static void testLargeVolume() {
    Queue<Integer> queue = new Queue<Integer>(2);
    int n = 5000;
    for (int i = 0; i < n; i++) {
      queue.insert(i);
    }
    check(queue.peekFront() == 0,
        "large volume test: peekFront() should return the first inserted item");
    check(queue.peekRear() == n - 1,
        "large volume test: peekRear() should return the last inserted item");
    for (int i = 0; i < n; i++) {
      check(queue.remove() == i,
          "large volume test: remove() should return " + i + " in FIFO order");
    }
    check(queue.remove() == null,
        "large volume test: remove() should return null when empty");
  }

  // differential test: perform random insert/remove operations and
  // compare the results against java.util.ArrayDeque after every step
  private static void testRandomizedAgainstOracle() {
    ArrayDeque<Integer> oracle = new ArrayDeque<Integer>();
    Random random = new Random(2026);
    Queue<Integer> queue = new Queue<Integer>(4);
    // tracks the expected internal capacity, which doubles every time
    // a resize is triggered (see CustomUtils.getNextResize)
    int capacity = 4;
    int nextValue = 0;
    for (int step = 0; step < 500; step++) {
      boolean insert = oracle.isEmpty() || random.nextInt(100) < 55;
      if (insert) {
        if (oracle.size() == capacity) {
          capacity *= 2; // the resize happens before the insert
        }
        queue.insert(nextValue);
        oracle.addLast(nextValue);
        nextValue++;
      } else {
        Integer expected = oracle.removeFirst();
        Integer actual = queue.remove();
        check(actual != null && actual.equals(expected),
            "randomized test: remove() should return " + expected
                + " but returned " + actual);
      }
      Integer expectedFront = oracle.peekFirst();
      Integer actualFront = queue.peekFront();
      check(expectedFront == null ? actualFront == null
          : expectedFront.equals(actualFront),
          "randomized test: peekFront() should return " + expectedFront
              + " but returned " + actualFront);
      // peekRear() is only checked while the internal array is not
      // exactly full, because peekRear() currently crashes in that
      // state (see the tests at the bottom of this file)
      if (oracle.size() < capacity) {
        Integer expectedRear = oracle.peekLast();
        Integer actualRear = queue.peekRear();
        check(expectedRear == null ? actualRear == null
            : expectedRear.equals(actualRear),
            "randomized test: peekRear() should return " + expectedRear
                + " but returned " + actualRear);
      }
    }
    while (!oracle.isEmpty()) {
      Integer expected = oracle.removeFirst();
      Integer actual = queue.remove();
      check(actual != null && actual.equals(expected),
          "randomized test: remove() should return " + expected
              + " but returned " + actual);
    }
    check(queue.remove() == null,
        "randomized test: remove() should return null when empty");
  }

  // project requirement 1: the class must be generic
  private static void testGenericWithStrings() {
    Queue<String> queue = new Queue<String>(2);
    queue.insert("a");
    queue.insert("b");
    queue.insert("c"); // forces a resize
    check(queue.peekFront().equals("a"), "peekFront() should return \"a\"");
    check(queue.peekRear().equals("c"), "peekRear() should return \"c\"");
    check(queue.remove().equals("a"), "remove() should return \"a\"");
    check(queue.remove().equals("b"), "remove() should return \"b\"");
    check(queue.remove().equals("c"), "remove() should return \"c\"");
    check(queue.remove() == null, "remove() should return null when empty");
  }

  // 7.g: display() should run without errors
  private static void testDisplay() {
    Queue<Integer> queue = new Queue<Integer>(3);
    queue.insert(1);
    queue.insert(2);
    queue.display();
    queue.remove();
    queue.display();
  }

  // peekRear() must work even when the queue is exactly at capacity
  private static void testPeekRearWhenFull() {
    Queue<Integer> queue = new Queue<Integer>(3);
    queue.insert(1);
    queue.insert(2);
    queue.insert(3); // queue is now exactly full and queueHead wraps to 0
    check(queue.peekFront() == 1, "peekFront() should return 1 on a full queue");
    check(queue.peekRear() == 3,
        "peekRear() should return the last inserted item when the queue is "
            + "exactly full");
  }

  // the same bug affects any state where queueHead has wrapped to 0,
  // even after the queue is no longer full
  private static void testPeekRearAfterPartialDrain() {
    Queue<Integer> queue = new Queue<Integer>(3);
    queue.insert(1);
    queue.insert(2);
    queue.insert(3); // queue is exactly full and queueHead wraps to 0
    queue.remove(); // queueHead is still 0, but the queue is no longer full
    check(queue.peekFront() == 2,
        "peekFront() should return 2 after partially draining a full queue");
    check(queue.peekRear() == 3,
        "peekRear() should work after partially draining a full queue");
  }
}
