package com.romanrichter.interpreter.codegen;

public class StatementExpression extends Statement {

    private String caller;
    private Expression expression;

    public StatementExpression(String caller, Expression exp) {
        expression  = exp;
        this.caller = caller;
    }

    @Override
    public void accept(Visitor visitor) {

    }
}
