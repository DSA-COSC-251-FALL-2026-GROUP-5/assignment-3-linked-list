import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rigorous unit tests for the doubly linked {@link DoublyLinkedList} class.
 *
 * <p>Run through the project's existing harness:
 *
 * <pre>
 *   ./build
 *   ./run_test DoublyLinkedListTest
 * </pre>
 *
 * <p>On top of the behaviour verified for the singly linked list, every
 * mutation here is checked for full bidirectional pointer integrity: for every
 * node {@code n.next.previous == n} and {@code n.previous.next == n}, plus
 * {@code head.previous == null} and {@code tail.next == null}. Forward and
 * backward traversals must agree in size and content.
 */
public class DoublyLinkedListTest {

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

  private static DoublyLinkedList<Integer> listOf(int... values) {
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
    for (int value : values) {
      list.insertLast(value);
    }
    return list;
  }

  private static int size(DoublyLinkedList<?> list) {
    int count = 0;
    for (DoublyLink<?> node = list.headLink; node != null; node = node.next) {
      if (++count > 100000) {
        throw new AssertionError("cycle detected while measuring size");
      }
    }
    return count;
  }

  private static String expectedBackward(List<Integer> model) {
    StringBuilder builder = new StringBuilder("[");
    for (int i = model.size() - 1; i >= 0; i--) {
      if (i != model.size() - 1) {
        builder.append(", ");
      }
      builder.append(model.get(i));
    }
    return builder.append("]").toString();
  }

  /**
   * Full bidirectional invariant check: null-ness of head/tail, head.previous,
   * tail.next, forward reachability, backward reachability, and consistency of
   * every next/previous pair in both directions. Detects cycles.
   */
  private static void checkInvariants(DoublyLinkedList<?> list, int expectedSize, String context) {
    if (expectedSize == 0) {
      check(list.headLink == null, context + ": head must be null when list is empty");
      check(list.tailLink == null, context + ": tail must be null when list is empty");
      return;
    }

    check(list.headLink != null, context + ": head must not be null for a non-empty list");
    check(list.tailLink != null, context + ": tail must not be null for a non-empty list");
    check(list.headLink.previous == null, context + ": head.previous must be null");
    check(list.tailLink.next == null, context + ": tail.next must be null");

    List<DoublyLink<?>> forward = new ArrayList<>();
    DoublyLink<?> node = list.headLink;
    while (node != null) {
      forward.add(node);
      if (forward.size() > 100000) {
        check(false, context + ": cycle detected in next pointers");
        return;
      }
      node = node.next;
    }

    check(forward.size() == expectedSize,
        context + ": forward size should be " + expectedSize + " but was " + forward.size());
    check(forward.get(forward.size() - 1) == list.tailLink,
        context + ": forward traversal must end at tail");
    for (int i = 0; i < forward.size(); i++) {
      DoublyLink<?> current = forward.get(i);
      if (i > 0) {
        check(current.previous == forward.get(i - 1),
            context + ": next/previous link broken at index " + i);
      }
      if (i < forward.size() - 1) {
        check(current.next == forward.get(i + 1),
            context + ": forward next link broken at index " + i);
      }
    }

    List<DoublyLink<?>> backward = new ArrayList<>();
    node = list.tailLink;
    while (node != null) {
      backward.add(node);
      if (backward.size() > 100000) {
        check(false, context + ": cycle detected in previous pointers");
        return;
      }
      node = node.previous;
    }

    check(backward.size() == expectedSize,
        context + ": backward size should be " + expectedSize + " but was " + backward.size());
    check(backward.get(backward.size() - 1) == list.headLink,
        context + ": backward traversal must end at head");
    for (int i = 0; i < backward.size(); i++) {
      DoublyLink<?> current = backward.get(i);
      if (i > 0) {
        check(current.next == backward.get(i - 1),
            context + ": backward next link broken at index " + i);
      }
      if (i < backward.size() - 1) {
        check(current.previous == backward.get(i + 1),
            context + ": backward previous link broken at index " + i);
      }
    }
  }

  // ------------------------------------------------------------------
  // Individual test cases
  // ------------------------------------------------------------------

  private static void emptyList() {
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
    check(list.isEmpty(), "a new list must be empty");
    check(list.headLink == null, "new list head must be null");
    check(list.tailLink == null, "new list tail must be null");
    assertEquals("[]", list.toString(), "empty list toString");
    assertEquals("[]", list.toStringBackward(), "empty list toStringBackward");
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
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
    list.insertFirst(1);
    assertEquals("[1]", list.toString(), "after a single insertFirst");
    assertEquals("[1]", list.toStringBackward(), "backward after a single insertFirst");
    checkInvariants(list, 1, "single element");
    check(list.headLink == list.tailLink, "one element: head and tail are the same node");

    list.insertFirst(2);
    list.insertFirst(3);
    assertEquals("[3, 2, 1]", list.toString(), "insertFirst prepends (reverse order)");
    assertEquals("[1, 2, 3]", list.toStringBackward(), "backward traversal is the reverse");
    checkInvariants(list, 3, "three elements inserted at the front");
    assertEquals(0, list.find(3), "find newest front element");
    assertEquals(2, list.find(1), "find oldest element");
    check(list.headLink.data == 3, "head holds 3");
    check(list.tailLink.data == 1, "tail holds 1");
  }

  private static void insertLastOrder() {
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
    list.insertLast(1);
    list.insertLast(2);
    list.insertLast(3);
    assertEquals("[1, 2, 3]", list.toString(), "insertLast appends in order");
    assertEquals("[3, 2, 1]", list.toStringBackward(), "backward traversal is the reverse");
    checkInvariants(list, 3, "three elements inserted at the back");
    check(list.tailLink.next == null, "tail.next is null");
    check(list.headLink.previous == null, "head.previous is null");
  }

  private static void mixedEnds() {
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
    list.insertLast(2);
    list.insertFirst(1);
    list.insertLast(3);
    list.insertFirst(0);
    assertEquals("[0, 1, 2, 3]", list.toString(), "interleaved insertFirst/insertLast");
    assertEquals("[3, 2, 1, 0]", list.toStringBackward(), "backward traversal matches");
    checkInvariants(list, 4, "interleaved ends");

    // Explicit pairwise pointer check.
    check(list.headLink.next.previous == list.headLink, "second node points back to head");
    check(list.tailLink.previous.next == list.tailLink, "second-to-last points forward to tail");
  }

  private static void insertAtPositions() {
    DoublyLinkedList<Integer> list = listOf(1, 2, 3);
    list.insertAt(0, 0);
    assertEquals("[0, 1, 2, 3]", list.toString(), "insertAt(0) inserts at the front");
    checkInvariants(list, 4, "after insertAt(0)");

    list.insertAt(4, 4);
    assertEquals("[0, 1, 2, 3, 4]", list.toString(), "insertAt(size) appends");
    checkInvariants(list, 5, "after insertAt(size)");

    list.insertAt(2, 99);
    assertEquals("[0, 1, 99, 2, 3, 4]", list.toString(), "insertAt in the middle");
    assertEquals("[4, 3, 2, 99, 1, 0]", list.toStringBackward(), "backward after middle insert");
    checkInvariants(list, 6, "after middle insert");
    // The node after the inserted one must point back to it.
    check(list.headLink.next.next.previous == list.headLink.next, "middle insert wires previous");

    list.insertAt(1, 88);
    assertEquals("[0, 88, 1, 99, 2, 3, 4]", list.toString(), "insertAt the second position");
    checkInvariants(list, 7, "after second-position insert");

    list.insertAt(7, 77);
    assertEquals("[0, 88, 1, 99, 2, 3, 4, 77]", list.toString(), "insertAt(size) appends again");
    checkInvariants(list, 8, "after final append");
  }

  private static void insertAtOnEmpty() {
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
    list.insertAt(0, 5);
    assertEquals("[5]", list.toString(), "insertAt(0) on an empty list");
    checkInvariants(list, 1, "after insertAt(0) on empty");

    list.insertAt(1, 6);
    assertEquals("[5, 6]", list.toString(), "insertAt(size) on a one element list appends");
    checkInvariants(list, 2, "after insertAt(size) on one element");
  }

  private static void insertAtOutOfRange() {
    DoublyLinkedList<Integer> list = listOf(1, 2, 3);
    list.insertAt(100, 9);
    checkInvariants(list, size(list), "after far out-of-range insert");
    assertEquals(0, list.find(1), "existing element 1 survives out-of-range insert");
    assertEquals(1, list.find(2), "existing element 2 survives out-of-range insert");
    assertEquals(2, list.find(3), "existing element 3 survives out-of-range insert");
    check(!list.isEmpty(), "list must not be emptied by an out-of-range insert");

    DoublyLinkedList<Integer> negative = listOf(1, 2, 3);
    negative.insertAt(-5, 42);
    checkInvariants(negative, size(negative), "after negative-index insert");
    check(negative.find(1) >= 0 && negative.find(2) >= 0 && negative.find(3) >= 0,
        "no original data lost on a negative-index insert");
  }

  private static void deleteFirstBehaviour() {
    DoublyLinkedList<Integer> one = listOf(5);
    check(one.deleteFirst(), "deleteFirst on a single element returns true");
    check(one.isEmpty(), "list is empty after deleting the only element");
    check(one.headLink == null && one.tailLink == null, "head and tail cleared");
    checkInvariants(one, 0, "cleared single element list");
    check(!one.deleteFirst(), "deleteFirst again on empty returns false");

    DoublyLinkedList<Integer> list = listOf(1, 2, 3);
    check(list.deleteFirst(), "deleteFirst on the head");
    assertEquals("[2, 3]", list.toString(), "head removed");
    assertEquals("[3, 2]", list.toStringBackward(), "backward after deleteFirst");
    checkInvariants(list, 2, "after deleteFirst");
    check(list.headLink.previous == null, "new head.previous must be null");
    check(list.headLink.data == 2 && list.tailLink.data == 3, "head/tail correct after deleteFirst");

    check(list.deleteFirst(), "deleteFirst again");
    assertEquals("[3]", list.toString(), "two removed");
    check(list.headLink == list.tailLink, "single node: head == tail");

    check(list.deleteFirst(), "deleteFirst last element");
    assertEquals("[]", list.toString(), "list now empty");
    assertEquals("[]", list.toStringBackward(), "backward on empty list");
    check(list.headLink == null && list.tailLink == null, "head/tail null after last deleteFirst");
  }

  private static void deleteLastBehaviour() {
    DoublyLinkedList<Integer> one = listOf(5);
    check(one.deleteLast(), "deleteLast on a single element returns true");
    check(one.isEmpty() && one.headLink == null && one.tailLink == null,
        "list cleared after deleting the only element");
    check(!one.deleteLast(), "deleteLast again on empty returns false");

    DoublyLinkedList<Integer> list = listOf(1, 2, 3);
    check(list.deleteLast(), "deleteLast on the tail");
    assertEquals("[1, 2]", list.toString(), "tail removed");
    assertEquals("[2, 1]", list.toStringBackward(), "backward after deleteLast");
    checkInvariants(list, 2, "after deleteLast");
    check(list.tailLink.next == null, "new tail.next must be null");
    check(list.tailLink.data == 2 && list.headLink.data == 1, "tail/head correct after deleteLast");

    check(list.deleteLast(), "deleteLast again");
    assertEquals("[1]", list.toString(), "one element left");
    check(list.headLink == list.tailLink, "single node: head == tail");

    check(list.deleteLast(), "deleteLast last element");
    assertEquals("[]", list.toString(), "list now empty");
    check(list.headLink == null && list.tailLink == null, "head/tail null after last deleteLast");
  }

  private static void deleteAtIndex() {
    DoublyLinkedList<Integer> list = listOf(1, 2, 3, 4, 5);
    check(!list.deleteAt(5), "deleteAt(size) must fail");
    check(!list.deleteAt(100), "deleteAt(far out of range) must fail");
    check(!list.deleteAt(-1), "deleteAt(-1) must fail");
    assertEquals("[1, 2, 3, 4, 5]", list.toString(), "list unchanged after failed deleteAt");
    checkInvariants(list, 5, "after failed deleteAt calls");

    check(list.deleteAt(2), "deleteAt in the middle");
    assertEquals("[1, 2, 4, 5]", list.toString(), "middle element removed");
    assertEquals("[5, 4, 2, 1]", list.toStringBackward(), "backward after middle deleteAt");
    checkInvariants(list, 4, "after middle deleteAt");

    check(list.deleteAt(0), "deleteAt the front");
    assertEquals("[2, 4, 5]", list.toString(), "front element removed");
    checkInvariants(list, 3, "after front deleteAt");
    check(list.headLink.previous == null, "head.previous null after front delete");

    check(list.deleteAt(2), "deleteAt the last index");
    assertEquals("[2, 4]", list.toString(), "last element removed");
    checkInvariants(list, 2, "after last deleteAt");
    check(list.tailLink.next == null, "tail.next null after last delete");

    check(list.deleteAt(1), "deleteAt the current last index");
    assertEquals("[2]", list.toString(), "one element left");
    checkInvariants(list, 1, "one element left");

    check(list.deleteAt(0), "deleteAt(0) the only element");
    assertEquals("[]", list.toString(), "list emptied");
    check(!list.deleteAt(0), "deleteAt on an emptied list must fail");
  }

  private static void deleteKeyBehaviour() {
    DoublyLinkedList<Integer> list = listOf(1, 2, 3, 4, 5);
    check(list.deleteKey(1), "deleteKey removes the head");
    assertEquals("[2, 3, 4, 5]", list.toString(), "head removed by key");
    checkInvariants(list, 4, "after deleteKey(head)");
    check(list.headLink.previous == null, "head.previous null after deleting head");

    check(list.deleteKey(5), "deleteKey removes the tail");
    assertEquals("[2, 3, 4]", list.toString(), "tail removed by key");
    checkInvariants(list, 3, "after deleteKey(tail)");
    check(list.tailLink.next == null, "tail.next null after deleting tail");

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
    DoublyLinkedList<Integer> list = listOf(1, 2, 1, 3, 1);
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
    DoublyLinkedList<Integer> list = listOf(10, 20, 30, 40);
    assertEquals(0, list.find(10), "find first element");
    assertEquals(1, list.find(20), "find second element");
    assertEquals(2, list.find(30), "find third element");
    assertEquals(3, list.find(40), "find last element");
    assertEquals(-1, list.find(50), "find absent element");

    DoublyLinkedList<Integer> duplicates = listOf(7, 7, 7);
    assertEquals(0, duplicates.find(7), "find returns the index of the first duplicate");
  }

  /**
   * Exposes the {@code data == key} comparison used by find/deleteKey. Reference
   * identity is not value equality.
   */
  private static void valueEquality() {
    DoublyLinkedList<Integer> big = new DoublyLinkedList<>();
    big.insertLast(200);
    assertEquals(0, big.find(200),
        "find must use value equality for Integer values outside the boxing cache (>127)");
    check(big.deleteKey(200), "deleteKey must match Integer 200 by value");
    check(big.isEmpty(), "list empty after deleteKey(200)");
    checkInvariants(big, 0, "after deleting a large value");

    DoublyLinkedList<String> strings = new DoublyLinkedList<>();
    strings.insertLast(new String("hello"));
    strings.insertLast(new String("world"));
    assertEquals(0, strings.find("hello"),
        "find must use value equality for equal-but-distinct Strings");
    check(strings.deleteKey("hello"), "deleteKey must match equal-but-distinct Strings");
    assertEquals("[world]", strings.toString(), "only the matching String was removed");
  }

  private static void toStringFormat() {
    assertEquals("[]", new DoublyLinkedList<Integer>().toString(), "empty list format");
    assertEquals("[7]", listOf(7).toString(), "single element format");
    assertEquals("[1, 2, 3]", listOf(1, 2, 3).toString(), "multi element format");
    assertEquals("[]", new DoublyLinkedList<Integer>().toStringBackward(), "empty backward format");
    assertEquals("[7]", listOf(7).toStringBackward(), "single element backward format");
    assertEquals("[3, 2, 1]", listOf(1, 2, 3).toStringBackward(), "multi element backward format");
  }

  /** Model based randomized test using small (boxed-cache safe) values. */
  private static void randomizedModelSmallValues() {
    Random random = new Random(24680);
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
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

      assertEquals(model.toString(), list.toString(), "forward model mismatch at step " + step);
      assertEquals(expectedBackward(model), list.toStringBackward(),
          "backward model mismatch at step " + step);
      checkInvariants(list, model.size(), "step " + step);
    }
    checkInvariants(list, model.size(), "end of randomized model test");
  }

  /** Positional stress test with large values that would trip {@code ==} lookups. */
  private static void randomizedModelLargeValues() {
    Random random = new Random(13579);
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
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
      assertEquals(expectedBackward(model), list.toStringBackward(),
          "large-value backward mismatch at step " + step);
      if (step % 50 == 0) {
        checkInvariants(list, model.size(), "large-value step " + step);
      }
    }
    checkInvariants(list, model.size(), "end of large-value model test");
  }

  private static void bulkInsertsAndDeletes() {
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
    for (int i = 0; i < 1000; i++) {
      list.insertLast(i);
    }
    checkInvariants(list, 1000, "after 1000 insertLast");
    assertEquals(0, list.find(0), "find the first element");
    check(list.tailLink.data == 999, "tail holds 999");
    check(list.tailLink.next == null, "tail.next is null after bulk inserts");
    check(list.toStringBackward().startsWith("[999, 998, 997"), "backward starts at the tail");

    for (int i = 0; i < 500; i++) {
      check(list.deleteFirst(), "bulk deleteFirst #" + i);
    }
    checkInvariants(list, 500, "after 500 deleteFirst");
    check(list.headLink.data == 500, "new head is 500");
    check(list.headLink.previous == null, "head.previous null after bulk deleteFirst");

    for (int i = 0; i < 500; i++) {
      check(list.deleteLast(), "bulk deleteLast #" + i);
    }
    checkInvariants(list, 0, "after all deletes");
    check(list.isEmpty(), "list is empty after bulk deletes");
  }

  private static void mixedOperationsPreservePointers() {
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
    for (int i = 0; i < 50; i++) {
      list.insertFirst(i);
      checkInvariants(list, i + 1, "after insertFirst " + i);
    }
    for (int i = 100; i < 150; i++) {
      list.insertLast(i);
      checkInvariants(list, size(list), "after insertLast " + i);
    }
    for (int i = 0; i < 50; i += 2) {
      check(list.deleteAt(1), "interleaved deleteAt(1) #" + i);
      checkInvariants(list, size(list), "after interleaved deleteAt #" + i);
    }
    for (int i = 0; i < 25; i++) {
      check(list.deleteLast(), "interleaved deleteLast #" + i);
      checkInvariants(list, size(list), "after interleaved deleteLast #" + i);
    }
    for (int i = 0; i < 25; i++) {
      check(list.deleteFirst(), "interleaved deleteFirst #" + i);
      checkInvariants(list, size(list), "after interleaved deleteFirst #" + i);
    }
    checkInvariants(list, size(list), "end of mixed operations");
  }

  private static void displayOutput() {
    DoublyLinkedList<Integer> list = listOf(1, 2, 3);
    String forward = capture(list::displayForward);
    check(forward.contains("linked list: [1, 2, 3]"),
        "displayForward prints the list; got: " + forward);
    check(forward.contains("headLink"), "displayForward prints headLink");
    check(forward.contains("tailLink"), "displayForward prints tailLink");

    String backward = capture(list::displayBackward);
    check(backward.contains("linked list: [3, 2, 1]"),
        "displayBackward prints the reversed list; got: " + backward);

    DoublyLinkedList<Integer> empty = new DoublyLinkedList<>();
    String emptyForward = capture(empty::displayForward);
    String emptyBackward = capture(empty::displayBackward);
    check(emptyForward.contains("linked list: []"), "displayForward of an empty list");
    check(emptyBackward.contains("linked list: []"), "displayBackward of an empty list");
  }

  /**
   * Length-two lists are the smallest case where every deletion branch
   * (deleteFirst / deleteLast / middle / index == size) is exercised.
   */
  private static void twoElementBoundaries() {
    DoublyLinkedList<Integer> deleteLastSide = listOf(1, 2);
    check(!deleteLastSide.deleteAt(2), "deleteAt(size) on a two element list must fail");
    check(deleteLastSide.deleteAt(1), "deleteAt(last index) on a two element list");
    assertEquals("[1]", deleteLastSide.toString(), "last element removed from two element list");
    assertEquals("[1]", deleteLastSide.toStringBackward(), "backward after deleteAt(last)");
    checkInvariants(deleteLastSide, 1, "two element list after deleteAt(last)");
    check(deleteLastSide.deleteFirst(), "deleteFirst the remaining element");

    DoublyLinkedList<Integer> deleteFrontSide = listOf(1, 2);
    check(deleteFrontSide.deleteAt(0), "deleteAt(0) on a two element list");
    assertEquals("[2]", deleteFrontSide.toString(), "front removed from two element list");
    checkInvariants(deleteFrontSide, 1, "two element list after deleteAt(0)");

    DoublyLinkedList<Integer> middleInsert = listOf(1, 2);
    middleInsert.insertAt(1, 99);
    assertEquals("[1, 99, 2]", middleInsert.toString(),
        "insertAt(1) is a true middle insert in a two element list");
    assertEquals("[2, 99, 1]", middleInsert.toStringBackward(), "backward after middle insert");
    checkInvariants(middleInsert, 3, "two element list after middle insert");

    DoublyLinkedList<Integer> appendInsert = listOf(1, 2);
    appendInsert.insertAt(2, 3);
    assertEquals("[1, 2, 3]", appendInsert.toString(),
        "insertAt(2) appends on a two element list");
    checkInvariants(appendInsert, 3, "two element list after append");

    DoublyLinkedList<Integer> keys = listOf(1, 2);
    check(keys.deleteKey(1), "deleteKey the head of a two element list");
    checkInvariants(keys, 1, "two element list after deleteKey(head)");
    check(keys.deleteLast(), "deleteLast the remaining element");
    check(keys.isEmpty(), "two element list emptied via different delete branches");
  }

  /** A list must be fully reusable after being drained to empty. */
  private static void reuseAfterEmpty() {
    DoublyLinkedList<Integer> list = listOf(1, 2, 3);
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
    DoublyLinkedList<Integer> list = listOf(1, 2, 3);

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
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
    list.insertLast(Integer.MIN_VALUE);
    list.insertLast(0);
    list.insertLast(Integer.MAX_VALUE);
    assertEquals(0, list.find(Integer.MIN_VALUE), "find Integer.MIN_VALUE");
    assertEquals(2, list.find(Integer.MAX_VALUE), "find Integer.MAX_VALUE");
    assertEquals(-1, list.find(Integer.MIN_VALUE + 1), "find an absent value near MIN_VALUE");

    check(list.deleteKey(Integer.MAX_VALUE), "deleteKey Integer.MAX_VALUE");
    assertEquals("[-2147483648, 0]", list.toString(), "MAX_VALUE removed");
    assertEquals("[0, -2147483648]", list.toStringBackward(), "backward after MAX_VALUE removed");
    check(list.deleteKey(Integer.MIN_VALUE), "deleteKey Integer.MIN_VALUE");
    assertEquals("[0]", list.toString(), "MIN_VALUE removed");
    check(list.deleteKey(0), "deleteKey the last value");
    check(list.isEmpty(), "extreme value list emptied");
  }

  /** String specific equality, duplicates, case sensitivity and the empty string. */
  private static void stringValues() {
    DoublyLinkedList<String> list = new DoublyLinkedList<>();
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
    assertEquals("[, apple, banana]", list.toStringBackward(), "backward after first apple removed");
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
    DoublyLinkedList<Widget> list = new DoublyLinkedList<>();
    list.insertLast(new Widget(1, "one"));
    list.insertLast(new Widget(2, "two"));
    list.insertLast(new Widget(3, "three"));

    assertEquals(1, list.find(new Widget(2, "different label")),
        "find must use compareTo for a custom Comparable type");
    assertEquals(-1, list.find(new Widget(4, "four")), "find an absent custom value");
    check(list.deleteKey(new Widget(1, "whatever")),
        "deleteKey must use compareTo for a custom Comparable type");
    assertEquals("[two, three]", list.toString(), "custom type toString after delete");
    assertEquals("[three, two]", list.toStringBackward(), "custom type backward after delete");
    checkInvariants(list, 2, "after custom type delete");
  }

  /** insertFirst-only construction, then drain from the tail. */
  private static void insertFirstHeavyBulk() {
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
    for (int i = 0; i < 1000; i++) {
      list.insertFirst(i);
    }
    checkInvariants(list, 1000, "after 1000 insertFirst");
    check(list.headLink.data == 999, "the newest insertFirst value is the head");
    check(list.tailLink.data == 0, "the oldest insertFirst value is the tail");
    check(list.toStringBackward().startsWith("[0, 1, 2, 3"), "backward starts at the tail");

    for (int i = 0; i < 1000; i++) {
      check(list.deleteLast(), "deleteLast #" + i);
    }
    checkInvariants(list, 0, "after deleting every element from the tail");
  }

  /** Alternate deletions from both ends and diff against a model each step. */
  private static void alternatingEndDeletes() {
    DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
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
      assertEquals(expectedBackward(model), list.toStringBackward(),
          "alternating backward mismatch at step " + i);
      checkInvariants(list, model.size(), "alternating step " + i);
    }
    check(list.isEmpty(), "alternating deletes empty the list");
  }

  // ------------------------------------------------------------------
  // Entry point
  // ------------------------------------------------------------------
  public static void main(String[] args) {
    System.out.println("Running DoublyLinkedListTest\n");

    test("empty list", DoublyLinkedListTest::emptyList);
    test("insertFirst ordering", DoublyLinkedListTest::insertFirstOrder);
    test("insertLast ordering", DoublyLinkedListTest::insertLastOrder);
    test("interleaved insertFirst/insertLast", DoublyLinkedListTest::mixedEnds);
    test("insertAt positions", DoublyLinkedListTest::insertAtPositions);
    test("insertAt on empty list", DoublyLinkedListTest::insertAtOnEmpty);
    test("insertAt out of range", DoublyLinkedListTest::insertAtOutOfRange);
    test("deleteFirst", DoublyLinkedListTest::deleteFirstBehaviour);
    test("deleteLast", DoublyLinkedListTest::deleteLastBehaviour);
    test("deleteAt", DoublyLinkedListTest::deleteAtIndex);
    test("deleteKey", DoublyLinkedListTest::deleteKeyBehaviour);
    test("deleteKey with duplicates", DoublyLinkedListTest::deleteKeyDuplicates);
    test("find indices", DoublyLinkedListTest::findIndices);
    test("value equality in find/deleteKey", DoublyLinkedListTest::valueEquality);
    test("toString/toStringBackward formatting", DoublyLinkedListTest::toStringFormat);
    test("randomized model (small values)", DoublyLinkedListTest::randomizedModelSmallValues);
    test("randomized model (large values)", DoublyLinkedListTest::randomizedModelLargeValues);
    test("bulk inserts/deletes", DoublyLinkedListTest::bulkInsertsAndDeletes);
    test("mixed operations preserve pointers", DoublyLinkedListTest::mixedOperationsPreservePointers);
    test("two element boundaries", DoublyLinkedListTest::twoElementBoundaries);
    test("reuse after empty", DoublyLinkedListTest::reuseAfterEmpty);
    test("extreme indices", DoublyLinkedListTest::extremeIndices);
    test("extreme values", DoublyLinkedListTest::extremeValues);
    test("String values", DoublyLinkedListTest::stringValues);
    test("custom Comparable type", DoublyLinkedListTest::customComparableType);
    test("insertFirst heavy bulk", DoublyLinkedListTest::insertFirstHeavyBulk);
    test("alternating end deletes", DoublyLinkedListTest::alternatingEndDeletes);
    test("display output", DoublyLinkedListTest::displayOutput);

    System.out.println();
    System.out.printf("DoublyLinkedListTest summary: %d checks, %d failure(s)%n", checks, failures);
    if (failures > 0) {
      System.out.println("RESULT: FAIL");
      System.exit(1);
    }
    System.out.println("RESULT: PASS");
  }
}
