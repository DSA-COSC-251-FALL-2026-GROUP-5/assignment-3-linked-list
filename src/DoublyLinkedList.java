class DoublyLinkedList<T extends Comparable<T>> {
  // for a linked list, we only need to keep track of head pointer and also the
  // tail pointer to be able to insert quickly
  DoublyLink<T> headLink;
  DoublyLink<T> tailLink;

  public DoublyLinkedList() {
    // 2.a. this takes constant time O(1)
    headLink = null;
    tailLink = null;
  }

  public void insertFirst(T newItem) {
    // 2.b. this takes O(1) time because we kept track of the head pointer, and
    // we're
    // just doing a constant amount of pointer manipulation
    // we must insert it in front of the current head

    DoublyLink<T> newLink = new DoublyLink<T>();
    newLink.data = newItem;
    newLink.previous = null;

    if (headLink == null) {
      newLink.next = null;
      tailLink = newLink;
      headLink = newLink;
      return;
    }

    // now that we have ensured that headLink isn't null
    newLink.next = headLink;
    headLink.previous = newLink;
    headLink = newLink;
  }

  public void insertLast(T newItem) {
    // 2.c. this takes O(1) time similar to insertFront
    DoublyLink<T> newLink = new DoublyLink<T>();
    newLink.data = newItem;
    newLink.next = null;

    if (tailLink == null) {
      newLink.previous = null;
      tailLink = newLink;
      headLink = newLink;
      return;
    }

    newLink.previous = tailLink;
    tailLink.next = newLink;
    tailLink = newLink;
  }

  public void insertAt(int index, T newItem) {
    // 2.d. this will take O(n) time because it takes time to traverse to index n
    // "index" is not clear

    if (index == 0) {
      insertFirst(newItem);
      return;
    }

    DoublyLink<T> insertionLink = headLink;
    for (int i = 0; i < index - 1; i++) {
      if (insertionLink == null) {
        return;
      }
      insertionLink = insertionLink.next;
    }

    // once we're at the insertion link, we just perform insertion

    if (insertionLink == tailLink) {
      insertLast(newItem);
      return;
    }

    // the conditions above ensures that there there is at least one node in the
    // previous and also one node next after the insertion link
    DoublyLink<T> newLink = new DoublyLink<T>();
    newLink.data = newItem;
    newLink.next = insertionLink.next;
    insertionLink.next.previous = newLink;
    newLink.previous = insertionLink;
    insertionLink.next = newLink;
  }

  public boolean deleteFirst() {
    // 2.e. deletion of the first element takes O(1) time because we can direcly
    // just move the head link to the next in the linked list and just trust that
    // the garbage collector will delete the old headLink which has no references to
    // it
    if (headLink == null) {
      return false;
    }

    if (headLink.next == null) {
      // in the special case that there is only one item left, we must make the tail
      // pointer point to null to make sure that nothing else is pointing to the link,
      // such that the garbage collector can handle the rest
      headLink = null;
      tailLink = null;
      return true;
    }
    headLink = headLink.next;
    headLink.previous = null;
    return true;
  }

  public boolean deleteLast() {
    // 2.f. deletion of the last index on a doubly linked list takes O(1) time
    // because we can traverse backwards from the tail to the head.
    if (tailLink == null) {
      return false;
    }

    if (tailLink.previous == null) {
      // in the special case that there is only one item left, we must make the tail
      // pointer point to null to make sure that nothing else is pointing to the link,
      // such that the garbage collector can handle the rest
      headLink = null;
      tailLink = null;
      return true;
    }

    tailLink = tailLink.previous;
    tailLink.next = null;
    return true;
  }

  public boolean deleteAt(int index) {
    // 2.g. this will take O(n) time because we need to traverse through the link
    // list to find the index of insertion.
    if (headLink == null) {
      return false;
    }

    if (index < 0) {
      // can't delete an index smaller than 0
      return false;
    }

    // to avoid the edge case for when we delete in the front
    if (index == 0) {
      return deleteFirst();
    }

    DoublyLink<T> deletionLink = headLink;

    // now we know that the index is greater than 0
    for (int i = 0; i < index; i++) {
      if (deletionLink == null) {
        return false;
      }
      deletionLink = deletionLink.next;
    }

    if (deletionLink == null) {
      return false;
    }

    if (deletionLink == tailLink) {
      return deleteLast();
    }

    // we know that it's in between now
    deletionLink.previous.next = deletionLink.next;
    deletionLink.next.previous = deletionLink.previous;
    return true;
  }

  public boolean deleteKey(T key) {
    // 2.h. this takes O(n) time because we need to find the key which takes O(n)
    // time, and then we need to delete At that index which also takes O(n) time.
    // this could be done faster if find returned the Link as an argument so that we
    // don't have to traverse the list a second time, however,
    // this is simpler and the time complexity doesn't change.
    int index = find(key);
    if (index < 0) {
      return false;
    }
    return deleteAt(index);
  }

  public int find(T key) {
    // 2.i. this takes O(n) time because we need to search and traverse through the
    // linked list
    DoublyLink<T> currentLink = headLink;
    int index = 0;
    while (currentLink != null) {
      if (currentLink.data.compareTo(key) == 0) {
        return index;
      }
      currentLink = currentLink.next;
      index++;
    }
    return -1;
  }

  public boolean isEmpty() {
    // 2.j. this takes O(1) time because it's it's just a pointer comparison
    return headLink == null;
  }

  public String toString() {
    // 2.k. this takes O(n) time because we need to loop through the whole list to
    // convert it into a string
    String returnString = "[";
    DoublyLink<T> currentLink = headLink;
    int i = 0;
    while (currentLink != null) {
      if (i != 0) {
        returnString = returnString.concat(" ");
      }
      returnString = returnString.concat(currentLink.data.toString());
      if (currentLink != tailLink) {
        returnString = returnString.concat(",");
      }
      currentLink = currentLink.next;
      i++;
    }
    returnString = returnString.concat("]");
    return returnString;
  }

  public String toStringBackward() {
    String returnString = "[";
    DoublyLink<T> currentLink = tailLink;
    int i = 0;
    while (currentLink != null) {
      if (i != 0) {
        returnString = returnString.concat(" ");
      }
      returnString = returnString.concat(currentLink.data.toString());
      if (currentLink != headLink) {
        returnString = returnString.concat(",");
      }
      currentLink = currentLink.previous;
      i++;
    }
    returnString = returnString.concat("]");
    return returnString;
  }

  public void displayForward() {
    // 2.l. this takes O(n) time because toString takes O(n) time
    System.out.printf("linked list: %s\r\n", toString());
    System.out.printf("headLink: %s\r\n", headLink);
    System.out.printf("tailLink: %s\r\n", tailLink);
  }

  public void displayBackward() {
    // 2.m this also takes O(n) time because toStringBackward takes O(n) time
    System.out.printf("linked list: %s\r\n", toStringBackward());
    System.out.printf("headLink: %s\r\n", headLink);
    System.out.printf("tailLink: %s\r\n", tailLink);
  }
}
