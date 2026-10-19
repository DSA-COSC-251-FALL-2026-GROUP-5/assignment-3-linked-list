import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rigorous unit tests for the singly linked {@link LinkedList} class.
 *
 * <p>These tests are dependency free and are meant to be run through the
 * project's existing harness:
 *
 * <pre>
 *   ./build
 *   ./run_test LinkedListTest
 * </pre>
 *
 * <p>The suite deliberately encodes the <em>intended</em> semantics of the
 * assignment (constant-time double-ended inserts, value based lookup, etc.), so
 * a failure is a real defect in the implementation rather than a quirk of the
 * test. Every list mutation is also followed by a structural invariant check
 * (reachable size, tail pointer correctness, no cycles) so that a broken
 * pointer is caught immediately.
 */
public class LinkedListTest {

  // ------------------------------------------------------------------
  // Minimal assertion harness (no JUnit / no external dependencies)
  // ------------------------------------------------------------------
  private static int checks = 0;
  private static int failures = 0;

  private static void test(String name, Runnable body) {
    System.out.println("== " + name);
    int before = failures;
    try {
      body.run();
    } catch (Throwable t) {
      failures++;
      System.out.println("    UNEXPECTED EXCEPTION: " + t);
      t.printStackTrace(System.out);
    }
    System.out.println(failures == before ? "   [PASS]" : "   [FAIL]");
  }

  private static void check(boolean condition, String message) {
    checks++;
    if (!condition) {
      failures++;
      System.out.println("    FAIL: " + message);
    }
  }

  private static void assertEquals(Object expected, Object actual, String message) {
    boolean ok = expected == null ? actual == null : expected.equals(actual);
    check(ok, message + "  (expected=" + expected + ", actual=" + actual + ")");
  }

  private static String capture(Runnable body) {
    PrintStream original = System.out;
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    System.setOut(new PrintStream(buffer));
    try {
      body.run();
    } finally {
      System.setOut(original);
    }
    return buffer.toString();
  }

  // ------------------------------------------------------------------
  // Helpers / structural invariants
  // ------------------------------------------------------------------

  private static LinkedList<Integer> listOf(int... values) {
    LinkedList<Integer> list = new LinkedList<>();
    for (int value : values) {
      list.insertLast(value);
    }
    return list;
  }

  /** Number of nodes reachable from head (guards against cycles). */
  private static int size(LinkedList<?> list) {
    int count = 0;
    for (Link<?> node = list.headLink; node != null; node = node.next) {
      if (++count > 100000) {
        throw new AssertionError("cycle detected while measuring size");
      }
    }
    return count;
  }

  /**
   * Verifies the fundamental invariant of the double-ended singly linked list:
   * head/tail null-ness, reachable size, tail pointing at the true last node and
   * tail.next == null. Also detects cycles.
   */
  private static void checkInvariants(LinkedList<?> list, int expectedSize, String context) {
    if (expectedSize == 0) {
      check(list.headLink == null, context + ": head must be null when list is empty");
      check(list.tailLink == null, context + ": tail must be null when list is empty");
      return;
    }

    check(list.headLink != null, context + ": head must not be null for a non-empty list");
    check(list.tailLink != null, context + ": tail must not be null for a non-empty list");

    Link<?> node = list.headLink;
    Link<?> last = null;
    int count = 0;
    while (node != null) {
      last = node;
      count++;
      if (count > 100000) {
        check(false, context + ": cycle detected in next pointers");
        return;
      }
      node = node.next;
    }

    check(count == expectedSize, context + ": size should be " + expectedSize + " but was " + count);
    check(last == list.tailLink, context + ": tail must point at the last reachable node");
    check(list.tailLink.next == null, context + ": tail.next must be null");
  }

  // ------------------------------------------------------------------
  // Individual test cases
  // ------------------------------------------------------------------

  private static void emptyList() {
    LinkedList<Integer> list = new LinkedList<>();
    check(list.isEmpty(), "a new list must be empty");
    check(list.headLink == null, "new list head must be null");
    check(list.tailLink == null, "new list tail must be null");
    assertEquals("[]", list.toString(), "empty list toString");
    assertEquals(-1, list.find(0), "find on empty list");
    check(!list.deleteFirst(), "deleteFirst on empty list must return false");
    check(!list.deleteLast(), "deleteLast on empty list must return false");
    check(!list.deleteAt(0), "deleteAt(0) on empty list must return false");
    check(!list.deleteAt(1), "deleteAt(1) on empty list must return false");
    check(!list.deleteAt(-1), "deleteAt(-1) on empty list must return false");
    check(!list.deleteAt(99), "deleteAt(99) on empty list must return false");
    check(!list.deleteKey(0), "deleteKey on empty list must return false");
    checkInvariants(list, 0, "empty list after failed operations");
  }

  private static void insertFirstOrder() {
    LinkedList<Integer> list = new LinkedList<>();
    list.insertFirst(1);
    assertEquals("[1]", list.toString(), "after a single insertFirst");
    checkInvariants(list, 1, "single element");
    check(list.headLink == list.tailLink, "one element: head and tail are the same node");

    list.insertFirst(2);
    list.insertFirst(3);
    assertEquals("[3, 2, 1]", list.toString(), "insertFirst prepends (reverse order)");
    checkInvariants(list, 3, "three elements inserted at the front");
    assertEquals(0, list.find(3), "find newest front element");
    assertEquals(1, list.find(2), "find middle element");
    assertEquals(2, list.find(1), "find oldest element");
    check(list.headLink.data == 3, "head holds 3");
    check(list.tailLink.data == 1, "tail holds 1");
  }

  private static void insertLastOrder() {
    LinkedList<Integer> list = new LinkedList<>();
    list.insertLast(1);
    list.insertLast(2);
    list.insertLast(3);
    assertEquals("[1, 2, 3]", list.toString(), "insertLast appends in order");
    checkInvariants(list, 3, "three elements inserted at the back");
    assertEquals(0, list.find(1), "find first element");
    assertEquals(2, list.find(3), "find last element");
    check(list.headLink.data == 1, "head holds 1");
    check(list.tailLink.data == 3, "tail holds 3");
    check(list.tailLink.next == null, "tail.next is null");
  }

  private static void mixedEnds() {
    LinkedList<Integer> list = new LinkedList<>();
    list.insertLast(2);
    list.insertFirst(1);
    list.insertLast(3);
    list.insertFirst(0);
    assertEquals("[0, 1, 2, 3]", list.toString(), "interleaved insertFirst/insertLast");
    checkInvariants(list, 4, "interleaved ends");
  }

  private static void insertAtPositions() {
    LinkedList<Integer> list = listOf(1, 2, 3);
    list.insertAt(0, 0);
    assertEquals("[0, 1, 2, 3]", list.toString(), "insertAt(0) inserts at the front");
    checkInvariants(list, 4, "after insertAt(0)");

    list.insertAt(4, 4);
    assertEquals("[0, 1, 2, 3, 4]", list.toString(), "insertAt(size) appends");
    checkInvariants(list, 5, "after insertAt(size)");

    list.insertAt(2, 99);
    assertEquals("[0, 1, 99, 2, 3, 4]", list.toString(), "insertAt in the middle");
    checkInvariants(list, 6, "after middle insert");

    list.insertAt(1, 88);
    assertEquals("[0, 88, 1, 99, 2, 3, 4]", list.toString(), "insertAt the second position");
    checkInvariants(list, 7, "after second-position insert");

    list.insertAt(7, 77);
    assertEquals("[0, 88, 1, 99, 2, 3, 4, 77]", list.toString(), "insertAt(size) appends again");
    checkInvariants(list, 8, "after final append");
  }

  private static void insertAtOnEmpty() {
    LinkedList<Integer> list = new LinkedList<>();
    list.insertAt(0, 5);
    assertEquals("[5]", list.toString(), "insertAt(0) on an empty list");
    checkInvariants(list, 1, "after insertAt(0) on empty");

    list.insertAt(1, 6);
    assertEquals("[5, 6]", list.toString(), "insertAt(size) on a one element list appends");
    checkInvariants(list, 2, "after insertAt(size) on one element");
  }

  private static void insertAtOutOfRange() {
    LinkedList<Integer> list = listOf(1, 2, 3);
    list.insertAt(100, 9);
    checkInvariants(list, size(list), "after far out-of-range insert");
    // The assignment does not define out-of-range inserts. What we require is
    // that data is never silently lost and the structure stays valid.
    assertEquals(0, list.find(1), "existing element 1 survives out-of-range insert");
    assertEquals(1, list.find(2), "existing element 2 survives out-of-range insert");
    assertEquals(2, list.find(3), "existing element 3 survives out-of-range insert");
    check(!list.isEmpty(), "list must not be emptied by an out-of-range insert");

    LinkedList<Integer> negative = listOf(1, 2, 3);
    negative.insertAt(-5, 42);
    checkInvariants(negative, size(negative), "after negative-index insert");
    check(negative.find(1) >= 0 && negative.find(2) >= 0 && negative.find(3) >= 0,
        "no original data lost on a negative-index insert");
  }

  private static void deleteFirstBehaviour() {
    LinkedList<Integer> one = listOf(5);
    check(one.deleteFirst(), "deleteFirst on a single element returns true");
    check(one.isEmpty(), "list is empty after deleting the only element");
    check(one.headLink == null && one.tailLink == null, "head and tail cleared");
    checkInvariants(one, 0, "cleared single element list");
    check(!one.deleteFirst(), "deleteFirst again on empty returns false");

    LinkedList<Integer> list = listOf(1, 2, 3);
    check(list.deleteFirst(), "deleteFirst on the head");
    assertEquals("[2, 3]", list.toString(), "head removed");
    checkInvariants(list, 2, "after deleteFirst");
    check(list.headLink.data == 2 && list.tailLink.data == 3, "head/tail correct after deleteFirst");

    check(list.deleteFirst(), "deleteFirst again");
    assertEquals("[3]", list.toString(), "two removed");
    check(list.headLink == list.tailLink, "single node: head == tail");

    check(list.deleteFirst(), "deleteFirst last element");
    assertEquals("[]", list.toString(), "list now empty");
    check(list.headLink == null && list.tailLink == null, "head/tail null after last deleteFirst");
  }

  private static void deleteLastBehaviour() {
    LinkedList<Integer> one = listOf(5);
    check(one.deleteLast(), "deleteLast on a single element returns true");
    check(one.isEmpty() && one.headLink == null && one.tailLink == null,
        "list cleared after deleting the only element");
    check(!one.deleteLast(), "deleteLast again on empty returns false");

    LinkedList<Integer> list = listOf(1, 2, 3);
    check(list.deleteLast(), "deleteLast on the tail");
    assertEquals("[1, 2]", list.toString(), "tail removed");
    checkInvariants(list, 2, "after deleteLast");
    check(list.tailLink.data == 2 && list.tailLink.next == null, "tail pointer updated");
    check(list.headLink.data == 1, "head unchanged");

    check(list.deleteLast(), "deleteLast again");
    assertEquals("[1]", list.toString(), "one element left");
    check(list.headLink == list.tailLink, "single node: head == tail");

    check(list.deleteLast(), "deleteLast last element");
    assertEquals("[]", list.toString(), "list now empty");
    check(list.headLink == null && list.tailLink == null, "head/tail null after last deleteLast");
  }

  private static void deleteAtIndex() {
    LinkedList<Integer> list = listOf(1, 2, 3, 4, 5);
    check(!list.deleteAt(5), "deleteAt(size) must fail");
    check(!list.deleteAt(100), "deleteAt(far out of range) must fail");
    check(!list.deleteAt(-1), "deleteAt(-1) must fail");
    assertEquals("[1, 2, 3, 4, 5]", list.toString(), "list unchanged after failed deleteAt");
    checkInvariants(list, 5, "after failed deleteAt calls");

    check(list.deleteAt(2), "deleteAt in the middle");
    assertEquals("[1, 2, 4, 5]", list.toString(), "middle element removed");
    checkInvariants(list, 4, "after middle deleteAt");

    check(list.deleteAt(0), "deleteAt the front");
    assertEquals("[2, 4, 5]", list.toString(), "front element removed");
    checkInvariants(list, 3, "after front deleteAt");

    check(list.deleteAt(2), "deleteAt the last index");
    assertEquals("[2, 4]", list.toString(), "last element removed");
    checkInvariants(list, 2, "after last deleteAt");

    check(list.deleteAt(1), "deleteAt the current last index");
    assertEquals("[2]", list.toString(), "one element left");
    checkInvariants(list, 1, "one element left");

    check(list.deleteAt(0), "deleteAt(0) the only element");
    assertEquals("[]", list.toString(), "list emptied");
    check(!list.deleteAt(0), "deleteAt on an emptied list must fail");
  }

  private static void deleteKeyBehaviour() {
    LinkedList<Integer> list = listOf(1, 2, 3, 4, 5);
    check(list.deleteKey(1), "deleteKey removes the head");
    assertEquals("[2, 3, 4, 5]", list.toString(), "head removed by key");
    checkInvariants(list, 4, "after deleteKey(head)");

    check(list.deleteKey(5), "deleteKey removes the tail");
    assertEquals("[2, 3, 4]", list.toString(), "tail removed by key");
    checkInvariants(list, 3, "after deleteKey(tail)");

    check(list.deleteKey(3), "deleteKey removes a middle element");
    assertEquals("[2, 4]", list.toString(), "middle removed by key");
    checkInvariants(list, 2, "after deleteKey(middle)");

    check(!list.deleteKey(99), "deleteKey of an absent key must return false");
    assertEquals("[2, 4]", list.toString(), "list unchanged after failed deleteKey");

    check(list.deleteKey(2), "deleteKey first remaining");
    assertEquals("[4]", list.toString(), "one element left");
    check(list.deleteKey(4), "deleteKey last element");
    assertEquals("[]", list.toString(), "list emptied");
    check(!list.deleteKey(4), "deleteKey on empty list must return false");
  }

  private static void deleteKeyDuplicates() {
    LinkedList<Integer> list = listOf(1, 2, 1, 3, 1);
    check(list.deleteKey(1), "deleteKey duplicates: first removal");
    assertEquals("[2, 1, 3, 1]", list.toString(), "only the first duplicate is removed");
    checkInvariants(list, 4, "after first duplicate removal");

    check(list.deleteKey(3), "deleteKey a non-duplicate in the middle");
    assertEquals("[2, 1, 1]", list.toString(), "3 removed");

    check(list.deleteKey(1), "deleteKey duplicates: second removal");
    assertEquals("[2, 1]", list.toString(), "one duplicate left");
    check(list.deleteKey(1), "deleteKey duplicates: third removal");
    assertEquals("[2]", list.toString(), "no duplicates left");
    check(!list.deleteKey(1), "no more matching keys");

    check(list.deleteKey(2), "deleteKey the final element");
    assertEquals("[]", list.toString(), "list emptied");
  }

  private static void findIndices() {
    LinkedList<Integer> list = listOf(10, 20, 30, 40);
    assertEquals(0, list.find(10), "find first element");
    assertEquals(1, list.find(20), "find second element");
    assertEquals(2, list.find(30), "find third element");
    assertEquals(3, list.find(40), "find last element");
    assertEquals(-1, list.find(50), "find absent element");

    LinkedList<Integer> duplicates = listOf(7, 7, 7);
    assertEquals(0, duplicates.find(7), "find returns the index of the first duplicate");
  }

  /**
   * Exposes the {@code data == key} comparison used by find/deleteKey. Reference
   * identity is not value equality: any Integer outside the boxing cache or any
   * equal-but-distinct String will not be found.
   */
  private static void valueEquality() {
    LinkedList<Integer> big = new LinkedList<>();
    big.insertLast(200);
    assertEquals(0, big.find(200),
        "find must use value equality for Integer values outside the boxing cache (>127)");
    check(big.deleteKey(200), "deleteKey must match Integer 200 by value");
    check(big.isEmpty(), "list empty after deleteKey(200)");
    checkInvariants(big, 0, "after deleting a large value");

    LinkedList<String> strings = new LinkedList<>();
    strings.insertLast(new String("hello"));
    strings.insertLast(new String("world"));
    assertEquals(0, strings.find("hello"),
        "find must use value equality for equal-but-distinct Strings");
    check(strings.deleteKey("hello"), "deleteKey must match equal-but-distinct Strings");
    assertEquals("[world]", strings.toString(), "only the matching String was removed");
  }

  private static void toStringFormat() {
    assertEquals("[]", new LinkedList<Integer>().toString(), "empty list format");
    assertEquals("[7]", listOf(7).toString(), "single element format");
    assertEquals("[1, 2, 3]", listOf(1, 2, 3).toString(), "multi element format");
    assertEquals("[100, 200, 300]", listOf(100, 200, 300).toString(), "large multi element format");
  }

  /** Model based randomized test using small (boxed-cache safe) values. */
  private static void randomizedModelSmallValues() {
    Random random = new Random(12345);
    LinkedList<Integer> list = new LinkedList<>();
    List<Integer> model = new ArrayList<>();

    for (int step = 0; step < 2000; step++) {
      int operation = random.nextInt(8);
      switch (operation) {
        case 0: {
          int value = random.nextInt(10);
          list.insertFirst(value);
          model.add(0, value);
          break;
        }
        case 1: {
          int value = random.nextInt(10);
          list.insertLast(value);
          model.add(value);
          break;
        }
        case 2: {
          int index = random.nextInt(model.size() + 1);
          int value = random.nextInt(10);
          list.insertAt(index, value);
          model.add(index, value);
          break;
        }
        case 3: {
          boolean expected = !model.isEmpty();
          if (expected) {
            model.remove(0);
          }
          assertEquals(expected, list.deleteFirst(), "deleteFirst at step " + step);
          break;
        }
        case 4: {
          boolean expected = !model.isEmpty();
          if (expected) {
            model.remove(model.size() - 1);
          }
          assertEquals(expected, list.deleteLast(), "deleteLast at step " + step);
          break;
        }
        case 5: {
          if (model.isEmpty()) {
            break;
          }
          int index = random.nextInt(model.size());
          model.remove(index);
          check(list.deleteAt(index), "deleteAt(" + index + ") at step " + step);
          break;
        }
        case 6: {
          int value = random.nextInt(10);
          boolean expected = model.remove(Integer.valueOf(value));
          assertEquals(expected, list.deleteKey(value), "deleteKey at step " + step);
          break;
        }
        default: {
          int value = random.nextInt(10);
          assertEquals(model.indexOf(value), list.find(value), "find at step " + step);
          break;
        }
      }

      assertEquals(model.toString(), list.toString(), "model mismatch at step " + step);
      checkInvariants(list, model.size(), "step " + step);
    }
    checkInvariants(list, model.size(), "end of randomized model test");
  }

  /** Positional stress test with large values that would trip {@code ==} lookups. */
  private static void randomizedModelLargeValues() {
    Random random = new Random(98765);
    LinkedList<Integer> list = new LinkedList<>();
    List<Integer> model = new ArrayList<>();

    for (int step = 0; step < 1500; step++) {
      int operation = random.nextInt(6);
      int value = 200 + random.nextInt(1000);
      switch (operation) {
        case 0: {
          list.insertFirst(value);
          model.add(0, value);
          break;
        }
        case 1: {
          list.insertLast(value);
          model.add(value);
          break;
        }
        case 2: {
          int index = random.nextInt(model.size() + 1);
          list.insertAt(index, value);
          model.add(index, value);
          break;
        }
        case 3: {
          boolean expected = !model.isEmpty();
          if (expected) {
            model.remove(0);
          }
          assertEquals(expected, list.deleteFirst(), "deleteFirst at step " + step);
          break;
        }
        case 4: {
          boolean expected = !model.isEmpty();
          if (expected) {
            model.remove(model.size() - 1);
          }
          assertEquals(expected, list.deleteLast(), "deleteLast at step " + step);
          break;
        }
        default: {
          if (model.isEmpty()) {
            break;
          }
          int index = random.nextInt(model.size());
          model.remove(index);
          check(list.deleteAt(index), "deleteAt(" + index + ") at step " + step);
          break;
        }
      }

      assertEquals(model.toString(), list.toString(), "large-value model mismatch at step " + step);
      if (step % 50 == 0) {
        checkInvariants(list, model.size(), "large-value step " + step);
      }
    }
    checkInvariants(list, model.size(), "end of large-value model test");
  }

  private static void bulkInsertsAndDeletes() {
    LinkedList<Integer> list = new LinkedList<>();
    for (int i = 0; i < 1000; i++) {
      list.insertLast(i);
    }
    checkInvariants(list, 1000, "after 1000 insertLast");
    assertEquals("0, 1, 2", list.headLink.data + ", " + list.headLink.next.data + ", "
        + list.headLink.next.next.data, "first three elements are 0, 1, 2");
    assertEquals(0, list.find(0), "find the first element");
    check(list.tailLink.data == 999, "tail holds 999");
    check(list.tailLink.next == null, "tail.next is null after bulk inserts");

    for (int i = 0; i < 500; i++) {
      check(list.deleteFirst(), "bulk deleteFirst #" + i);
    }
    checkInvariants(list, 500, "after 500 deleteFirst");
    check(list.headLink.data == 500, "new head is 500");

    for (int i = 0; i < 500; i++) {
      check(list.deleteLast(), "bulk deleteLast #" + i);
    }
    checkInvariants(list, 0, "after all deletes");
    check(list.isEmpty(), "list is empty after bulk deletes");
  }

  private static void displayOutput() {
    LinkedList<Integer> list = listOf(1, 2, 3);
    String output = capture(list::display);
    check(output.contains("linked list: [1, 2, 3]"), "display prints the list; got: " + output);
    check(output.contains("headLink"), "display prints headLink");
    check(output.contains("tailLink"), "display prints tailLink");

    LinkedList<Integer> empty = new LinkedList<>();
    String emptyOutput = capture(empty::display);
    check(emptyOutput.contains("linked list: []"), "display of an empty list; got: " + emptyOutput);
  }

  /**
   * Length-two lists are the smallest case where every deletion branch
   * (deleteFirst / deleteLast / middle / index == size) is exercised.
   */
  private static void twoElementBoundaries() {
    LinkedList<Integer> deleteLastSide = listOf(1, 2);
    check(!deleteLastSide.deleteAt(2), "deleteAt(size) on a two element list must fail");
    check(deleteLastSide.deleteAt(1), "deleteAt(last index) on a two element list");
    assertEquals("[1]", deleteLastSide.toString(), "last element removed from two element list");
    checkInvariants(deleteLastSide, 1, "two element list after deleteAt(last)");
    check(deleteLastSide.deleteFirst(), "deleteFirst the remaining element");

    LinkedList<Integer> deleteFrontSide = listOf(1, 2);
    check(deleteFrontSide.deleteAt(0), "deleteAt(0) on a two element list");
    assertEquals("[2]", deleteFrontSide.toString(), "front removed from two element list");
    checkInvariants(deleteFrontSide, 1, "two element list after deleteAt(0)");

    LinkedList<Integer> middleInsert = listOf(1, 2);
    middleInsert.insertAt(1, 99);
    assertEquals("[1, 99, 2]", middleInsert.toString(),
        "insertAt(1) is a true middle insert in a two element list");
    checkInvariants(middleInsert, 3, "two element list after middle insert");

    LinkedList<Integer> appendInsert = listOf(1, 2);
    appendInsert.insertAt(2, 3);
    assertEquals("[1, 2, 3]", appendInsert.toString(),
        "insertAt(2) appends on a two element list");
    checkInvariants(appendInsert, 3, "two element list after append");

    LinkedList<Integer> keys = listOf(1, 2);
    check(keys.deleteKey(1), "deleteKey the head of a two element list");
    checkInvariants(keys, 1, "two element list after deleteKey(head)");
    check(keys.deleteLast(), "deleteLast the remaining element");
    check(keys.isEmpty(), "two element list emptied via different delete branches");
  }

  /** A list must be fully reusable after being drained to empty. */
  private static void reuseAfterEmpty() {
    LinkedList<Integer> list = listOf(1, 2, 3);
    check(list.deleteFirst(), "drain first");
    check(list.deleteFirst(), "drain second");
    check(list.deleteFirst(), "drain third");
    checkInvariants(list, 0, "drained list");

    list.insertLast(9);
    assertEquals("[9]", list.toString(), "reinsert after emptying");
    check(list.headLink == list.tailLink, "rebuilt list head == tail");
    checkInvariants(list, 1, "after first reinsert");

    list.insertFirst(8);
    assertEquals("[8, 9]", list.toString(), "reinsert at both ends");
    checkInvariants(list, 2, "after second reinsert");

    list.insertLast(10);
    assertEquals("[8, 9, 10]", list.toString(), "reinsert at the tail");
    checkInvariants(list, 3, "after third reinsert");
  }

  /** Pathological index arguments must not corrupt the list or loop forever. */
  private static void extremeIndices() {
    LinkedList<Integer> list = listOf(1, 2, 3);

    list.insertAt(Integer.MAX_VALUE, 9);
    checkInvariants(list, size(list), "after insertAt(Integer.MAX_VALUE)");
    check(list.find(1) == 0 && list.find(2) >= 0 && list.find(3) >= 0,
        "no data lost on a huge insert index");

    list.insertAt(Integer.MIN_VALUE, 9);
    checkInvariants(list, size(list), "after insertAt(Integer.MIN_VALUE)");
    check(list.find(1) >= 0 && list.find(2) >= 0 && list.find(3) >= 0,
        "no data lost on a minimal insert index");

    check(!list.deleteAt(Integer.MAX_VALUE), "deleteAt(Integer.MAX_VALUE) must fail");
    check(!list.deleteAt(Integer.MIN_VALUE), "deleteAt(Integer.MIN_VALUE) must fail");
    checkInvariants(list, size(list), "after extreme deleteAt calls");
  }

  /** Integer extremes verify compareTo based lookup has no special cases. */
  private static void extremeValues() {
    LinkedList<Integer> list = new LinkedList<>();
    list.insertLast(Integer.MIN_VALUE);
    list.insertLast(0);
    list.insertLast(Integer.MAX_VALUE);
    assertEquals(0, list.find(Integer.MIN_VALUE), "find Integer.MIN_VALUE");
    assertEquals(2, list.find(Integer.MAX_VALUE), "find Integer.MAX_VALUE");
    assertEquals(-1, list.find(Integer.MIN_VALUE + 1), "find an absent value near MIN_VALUE");

    check(list.deleteKey(Integer.MAX_VALUE), "deleteKey Integer.MAX_VALUE");
    assertEquals("[-2147483648, 0]", list.toString(), "MAX_VALUE removed");
    check(list.deleteKey(Integer.MIN_VALUE), "deleteKey Integer.MIN_VALUE");
    assertEquals("[0]", list.toString(), "MIN_VALUE removed");
    check(list.deleteKey(0), "deleteKey the last value");
    check(list.isEmpty(), "extreme value list emptied");
  }

  /** String specific equality, duplicates, case sensitivity and the empty string. */
  private static void stringValues() {
    LinkedList<String> list = new LinkedList<>();
    list.insertLast(new String("apple"));
    list.insertLast(new String("banana"));
    list.insertLast(new String("apple"));
    list.insertLast("");

    assertEquals(0, list.find("apple"), "find the first equal String");
    assertEquals(0, list.find(new String("apple")), "find an equal-but-distinct String");
    assertEquals(1, list.find("banana"), "find another String");
    assertEquals(3, list.find(""), "find the empty String");
    assertEquals(-1, list.find("Apple"), "find is case sensitive");
    assertEquals(-1, list.find("appl"), "a partial String must not match");

    check(list.deleteKey(new String("apple")), "deleteKey an equal-but-distinct String");
    assertEquals("[banana, apple, ]", list.toString(), "only the first apple was removed");
    check(list.deleteKey("apple"), "deleteKey the remaining apple");
    assertEquals("[banana, ]", list.toString(), "apple removed, empty String remains");
    check(!list.deleteKey("cherry"), "deleteKey an absent String");
    check(list.deleteKey("banana") && list.deleteKey(""), "deleteKey the remaining Strings");
    check(list.isEmpty(), "String list emptied");
    checkInvariants(list, 0, "String list after all deletes");
  }

  /** Minimal Comparable type used to prove the generic code uses compareTo. */
  private static final class Widget implements Comparable<Widget> {
    private final int id;
    private final String label;

    private Widget(int id, String label) {
      this.id = id;
      this.label = label;
    }

    @Override
    public int compareTo(Widget other) {
      return Integer.compare(id, other.id);
    }

    @Override
    public String toString() {
      return label;
    }
  }

  /** Generic class must work for any T extends Comparable<T>, not just Integer. */
  private static void customComparableType() {
    LinkedList<Widget> list = new LinkedList<>();
    list.insertLast(new Widget(1, "one"));
    list.insertLast(new Widget(2, "two"));
    list.insertLast(new Widget(3, "three"));

    assertEquals(1, list.find(new Widget(2, "different label")),
        "find must use compareTo for a custom Comparable type");
    assertEquals(-1, list.find(new Widget(4, "four")), "find an absent custom value");
    check(list.deleteKey(new Widget(1, "whatever")),
        "deleteKey must use compareTo for a custom Comparable type");
    assertEquals("[two, three]", list.toString(), "custom type toString after delete");
    checkInvariants(list, 2, "after custom type delete");
  }

  /** insertFirst-only construction, then drain from the tail (worst case for singly). */
  private static void insertFirstHeavyBulk() {
    LinkedList<Integer> list = new LinkedList<>();
    for (int i = 0; i < 1000; i++) {
      list.insertFirst(i);
    }
    checkInvariants(list, 1000, "after 1000 insertFirst");
    check(list.headLink.data == 999, "the newest insertFirst value is the head");
    check(list.tailLink.data == 0, "the oldest insertFirst value is the tail");

    for (int i = 0; i < 1000; i++) {
      check(list.deleteLast(), "deleteLast #" + i);
    }
    checkInvariants(list, 0, "after deleting every element from the tail");
  }

  /** Alternate deletions from both ends and diff against a model each step. */
  private static void alternatingEndDeletes() {
    LinkedList<Integer> list = new LinkedList<>();
    List<Integer> model = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      list.insertLast(i);
      model.add(i);
    }

    for (int i = 0; i < 100; i++) {
      if (i % 2 == 0) {
        check(list.deleteFirst(), "alternating deleteFirst #" + i);
        model.remove(0);
      } else {
        check(list.deleteLast(), "alternating deleteLast #" + i);
        model.remove(model.size() - 1);
      }
      assertEquals(model.toString(), list.toString(), "alternating delete mismatch at step " + i);
      checkInvariants(list, model.size(), "alternating step " + i);
    }
    check(list.isEmpty(), "alternating deletes empty the list");
  }

  // ------------------------------------------------------------------
  // Entry point
  // ------------------------------------------------------------------
  public static void main(String[] args) {
    System.out.println("Running LinkedListTest\n");

    test("empty list", LinkedListTest::emptyList);
    test("insertFirst ordering", LinkedListTest::insertFirstOrder);
    test("insertLast ordering", LinkedListTest::insertLastOrder);
    test("interleaved insertFirst/insertLast", LinkedListTest::mixedEnds);
    test("insertAt positions", LinkedListTest::insertAtPositions);
    test("insertAt on empty list", LinkedListTest::insertAtOnEmpty);
    test("insertAt out of range", LinkedListTest::insertAtOutOfRange);
    test("deleteFirst", LinkedListTest::deleteFirstBehaviour);
    test("deleteLast", LinkedListTest::deleteLastBehaviour);
    test("deleteAt", LinkedListTest::deleteAtIndex);
    test("deleteKey", LinkedListTest::deleteKeyBehaviour);
    test("deleteKey with duplicates", LinkedListTest::deleteKeyDuplicates);
    test("find indices", LinkedListTest::findIndices);
    test("value equality in find/deleteKey", LinkedListTest::valueEquality);
    test("toString formatting", LinkedListTest::toStringFormat);
    test("randomized model (small values)", LinkedListTest::randomizedModelSmallValues);
    test("randomized model (large values)", LinkedListTest::randomizedModelLargeValues);
    test("bulk inserts/deletes", LinkedListTest::bulkInsertsAndDeletes);
    test("two element boundaries", LinkedListTest::twoElementBoundaries);
    test("reuse after empty", LinkedListTest::reuseAfterEmpty);
    test("extreme indices", LinkedListTest::extremeIndices);
    test("extreme values", LinkedListTest::extremeValues);
    test("String values", LinkedListTest::stringValues);
    test("custom Comparable type", LinkedListTest::customComparableType);
    test("insertFirst heavy bulk", LinkedListTest::insertFirstHeavyBulk);
    test("alternating end deletes", LinkedListTest::alternatingEndDeletes);
    test("display output", LinkedListTest::displayOutput);

    System.out.println();
    System.out.printf("LinkedListTest summary: %d checks, %d failure(s)%n", checks, failures);
    if (failures > 0) {
      System.out.println("RESULT: FAIL");
      System.exit(1);
    }
    System.out.println("RESULT: PASS");
  }
}
