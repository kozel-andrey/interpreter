package com.romanrichter.interpreter.codegen;

public enum Unop {
  Minus;
  
  String codeString() {
    switch(this) {
      case Minus: return "-";
    }
    throw new RuntimeException("Unreachable");
  }
}
