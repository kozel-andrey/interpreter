package com.romanrichter.interpreter.codegen;

public enum Bbinop {
  And, Or;

  String codeString() {
    switch (this) {
      case And:
        return "&&";
      case Or:
        return "||";
    }
    throw new RuntimeException("Unreachable");
  }
}
