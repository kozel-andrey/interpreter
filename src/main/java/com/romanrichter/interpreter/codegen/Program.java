package com.romanrichter.interpreter.codegen;


import java.lang.*;

public class Program implements Visitable {
  private Function[] functions;
  private CustomClass[] classes;
  
  public Function[] getFunctions() {
    return functions;
  }
  
  
  public Program(Function[] functions, CustomClass[] classes) {
    super();
    this.functions = functions;
    this.classes = classes;
  }

  public CustomClass[] getClasses() {
    return classes;
  }

  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
  
  @Override
  public String toString() {
    FormatVisitor visitor = new FormatVisitor();
    accept(visitor);
    return visitor.getResult();
  }
}
