class Link<T extends Comparable<T>> {
  T data;
  Link<T> next;

  public String toString() {
    String returnString = String.format("Link[data=%s, next=%s]", data, next == null ? "null" : "not null");
    return returnString;
  }
}
