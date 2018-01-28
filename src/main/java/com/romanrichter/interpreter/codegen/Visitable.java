package com.romanrichter.interpreter.codegen;

public interface Visitable {
  void accept(Visitor visitor);
}
