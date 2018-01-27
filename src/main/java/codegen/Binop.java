package codegen;

public enum Binop {
  Minus, Plus, MultiplicationOperator, DivisionOperator, Modulo;
  
  String codeString() {
    switch(this) {
      case Minus: return "-";
      case DivisionOperator: return "/";
      case Modulo: return "%";
      case MultiplicationOperator: return "*";
      case Plus: return "+";
    }
    throw new RuntimeException("Unreachable");
  }
}
