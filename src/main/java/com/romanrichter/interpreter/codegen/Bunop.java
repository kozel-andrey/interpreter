package com.romanrichter.interpreter.codegen;

public enum Bunop {
  Not;
  
  String codeString() {
    switch(this) {
      case Not: return "!";
    }
    throw new RuntimeException("Unreachable");
  }
}
