package com.romanrichter.interpreter.codegen;

public class BinaryCondition extends Condition {
  private Condition lhs;
  
  public Condition getLhs() {
    return lhs;
  }
  
  private Bbinop operator;
  
  public Bbinop getOperator() {
    return operator;
  }
  
  private Condition rhs;
  
  public Condition getRhs() {
    return rhs;
  }
  
  public BinaryCondition(Condition lhs, Bbinop operator, Condition rhs) {
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
      case And:
        return 3;
      case Or:
        return 4;
    }
    throw new RuntimeException("Unreachable");
  }

}
