class DoublyLink<T extends Comparable<T>> {
  T data;
  DoublyLink<T> next;
  DoublyLink<T> previous;

  public String toString() {
    String returnString = String.format("Link[data=%s, previous=%s, next=%s]",
        data,
        previous == null ? "null" : "not null",
        next == null ? "null" : "not null");
    return returnString;
  }
}
