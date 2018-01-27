package codegen;

public enum Type {
  Int, IntArray;

  String codeString() {
    switch (this) {
      case Int:
        return "int";
      case IntArray:
        return "int[]";
    }
    throw new RuntimeException("Unreachable");
  }
}
