package com.romanrichter.interpreter.codegen;

public class Declaration implements Visitable {
  private Type type;
  
  public Type getType() {
    return type;
  }
  
  private String[] names;
  
  public String[] getNames() {
    return names;
  }
  
  public Declaration(Type type, String[] names) {
    this.type = type;
    this.names = names;
  }
  
  public Declaration(Type type, String a) {
    this.type = type;
    this.names = new String [] { a };
  }

  public Declaration(Type type, String a, String b) {
    this.type = type;
    this.names = new String [] { a, b };
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
