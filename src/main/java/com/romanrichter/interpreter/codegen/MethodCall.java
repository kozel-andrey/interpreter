package com.romanrichter.interpreter.codegen;

public class MethodCall extends Expression {

    private Variable variable;
    private Expression call;

    public MethodCall(Variable variable, Expression call) {
        this.variable = variable;
        this.call = call;
    }

    public Variable getVariable() {
        return variable;
    }

    public Expression getCall() {
        return call;
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
