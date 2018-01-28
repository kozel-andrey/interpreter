package com.romanrichter.interpreter.codegen;

public class ObjectInitializer extends Expression {

    private Expression[] expressions;
    private String functionName;

    public ObjectInitializer(String functionName, Expression[] expressions) {
        this.functionName = functionName;
        this.expressions = expressions;
    }

    public Expression[] getExpressions() {
        return expressions;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public int firstLevelPriority() {
        return 0;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
