class LinkedList<T extends Comparable<T>> {
  // for a linked list, we only need to keep track of head pointer and also the
  // tail pointer to be able to insert quickly
  Link<T> headLink;
  Link<T> tailLink;

  // for orientation, we say that:
  //
  // Head -> ... -> Tail
  //
  // And when we say "insert first", we say insert in front of the head
  //
  // Similarly for "insert last" we insert it at the tail
  public LinkedList() {
    // 1.a. this takes constant time O(1)
    headLink = null;
    tailLink = null;
  }

  public void insertFirst(T newItem) {
    // 1.b. this takes O(1) time because we kept track of the head pointer, and
    // we're
    // just doing a constant amount of pointer manipulation
    // we must insert it in front of the current head

    Link<T> newLink = new Link<T>();
    newLink.data = newItem;
    newLink.next = headLink;
    headLink = newLink;
    if (tailLink == null) {
      tailLink = newLink;
    }
    // NOTE: this also handles for the case when the headLink is null.
  }

  public void insertLast(T newItem) {
    // 1.c. this takes O(1) time similar to insertFront
    Link<T> newLink = new Link<T>();
    newLink.data = newItem;
    newLink.next = null;
    if (tailLink != null) {
      tailLink.next = newLink;
    } else {
      headLink = newLink;
    }
    tailLink = newLink;
    // NOTE: the tail.next is always going to be pointing to the null, or is exactly
    // null when it's empty
  }

  public void insertAt(int index, T newItem) {
    // 1.d. this will take O(n) time because it takes time to traverse to index n
    // TODO: make sure that off-by-one error is dealt with because definition of
    // "index" is not clear

    if (index == 0) {
      insertFirst(newItem);
      return;
    }

    Link<T> insertionLink = headLink;
    for (int i = 0; i < index - 1; i++) {
      if (insertionLink == null) {
        // TODO: not sure how we would handle it if we reach the end of the linked list
        return;
      }
      insertionLink = insertionLink.next;
    }

    // once we're at the insertion link, we just perform insertion

    if (insertionLink == null) {
      insertLast(newItem);
      return;
    }

    Link<T> newLink = new Link<T>();
    newLink.data = newItem;
    newLink.next = insertionLink.next;
    insertionLink.next = newLink;
  }

  public boolean deleteFirst() {
    // 1.e. deletion of the first element takes O(1) time because we can direcly
    // just move the head link to the next in the linked list and just trust that
    // the garbage collector will delete the old headLink which has no references to
    // it
    if (headLink == null) {
      // TODO: not sure whether we should return true or false
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
    return true;
  }

  public boolean deleteLast() {
    // 1.f. deletion of the last index on a singly linked list has to take O(n) time
    // because even though we have access to the tail element, we have no way of
    // traversing back, therefore, we have no choice but to traverse from the head
    // pointer to the pointer directly pointing to last element

    if (headLink == null) {
      // TODO: not sure whether we should return true or false
      return false;
    }

    if (headLink.next == null) {
      headLink = null;
      tailLink = null;
      return true;
    }

    Link<T> beforeTailLink = headLink;
    while (beforeTailLink.next.next != null) {
      // we move until we reach the Link before the last link
      beforeTailLink = beforeTailLink.next;
    }

    // now beforeTailLink is actually the link before the tail link

    beforeTailLink.next = null;
    tailLink = beforeTailLink;
    return true;
  }

  public boolean deleteAt(int index) {
    // 1.g. this will take O(n) time because we need to traverse through the link
    // list to find the index of insertion.

    // TODO: make sure that this is the right thing to return also
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

    Link<T> beforeDeletionLink = null;
    Link<T> deletionLink = headLink;

    // now we know that the index is greater than 0
    for (int i = 0; i < index; i++) {
      if (deletionLink == null) {
        // TODO: make sure that this is the right thing to return
        return false;
      }
      beforeDeletionLink = deletionLink;
      deletionLink = deletionLink.next;
    }

    if (deletionLink == null) {
      return false;
    }

    if (deletionLink == tailLink) {
      return deleteLast();
    }

    // now that we know that we're deleting in the middle, so we don't need to
    // change the head and tail pointer
    beforeDeletionLink.next = deletionLink.next;
    return true;
  }

  public boolean deleteKey(T key) {
    // 1.h. this takes O(n) time because we need to find the key which takes O(n)
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
    // 1.i. this takes O(n) time because we need to search and traverse through the
    // linked list
    Link<T> currentLink = headLink;
    int index = 0;
    while (currentLink != null) {
      if (currentLink.data == key) {
        return index;
      }
      currentLink = currentLink.next;
      index++;
    }
    return -1;
  }

  public boolean isEmpty() {
    // 1.j. this takes O(1) time because it's it's just a pointer comparison
    return headLink == null;
  }

  public String toString() {
    // 1.k. this takes O(n) time because we need to loop through the whole list to
    // convert it into a string
    String returnString = "[";
    Link<T> currentLink = headLink;
    while (currentLink != null) {
      returnString = returnString.concat(String.format(" %s", currentLink.data.toString()));
      if (currentLink != tailLink) {
        returnString = returnString.concat(",");
      }
      currentLink = currentLink.next;
    }
    returnString = returnString.concat("]");
    return returnString;
  }

  public void display() {
    // 1.l. this takes O(n) time because toString takes O(n) time
    System.out.printf("linked list: %s\r\n", toString());
    System.out.printf("headLink: %s\r\n", headLink);
    System.out.printf("tailLink: %s\r\n", tailLink);
  }
}
