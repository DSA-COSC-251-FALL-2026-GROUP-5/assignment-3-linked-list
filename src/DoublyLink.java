class DoublyLink<T extends Comparable<T>> {
  T data;
  DoublyLink<T> next;
  DoublyLink<T> previous;

  public String toString() {
    String returnString = String.format("Link[data=%s, next=%s, previous=%s]",
        data,
        next == null ? "null" : "not null",
        previous == null ? "null" : "not null");
    return returnString;
  }
}
