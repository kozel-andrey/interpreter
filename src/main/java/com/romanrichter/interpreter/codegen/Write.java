package com.romanrichter.interpreter.codegen;

public class Write extends Statement {
  private Expression expression;
  
  public Expression getExpression() {
    return expression;
  }
  
  public Write(Expression expression) {
    this.expression = expression;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
