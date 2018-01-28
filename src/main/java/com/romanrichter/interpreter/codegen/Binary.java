package com.romanrichter.interpreter.codegen;

public class Binary extends Expression {
  private Expression lhs;
  
  public Expression getLhs() {
    return lhs;
  }
  
  private Binop operator;
  
  public Binop getOperator() {
    return operator;
  }
  
  private Expression rhs;
  
  public Expression getRhs() {
    return rhs;
  }
  
  public Binary(Expression lhs, Binop operator, Expression rhs) {
    super();
    this.lhs = lhs;
    this.operator = operator;
    this.rhs = rhs;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public int firstLevelPriority() {
    switch(operator) {
      case DivisionOperator:
      case Modulo:
      case MultiplicationOperator:
        return 1;
      case Plus:
      case Minus:
        return 2;
    }
    throw new RuntimeException("Unreachable");
  }
}
