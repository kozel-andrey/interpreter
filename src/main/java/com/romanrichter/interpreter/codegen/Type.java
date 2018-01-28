package com.romanrichter.interpreter.codegen;

public class Type {

  private String code;

  public Type(String code) {
    this.code = code;
  }

  String codeString() {
    return code;
  }
}
