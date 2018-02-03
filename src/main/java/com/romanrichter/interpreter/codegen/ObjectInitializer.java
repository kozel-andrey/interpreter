package com.romanrichter.interpreter.codegen;

public class ObjectInitializer extends Expression {

    private Expression[] arguments;
    private String className;

    public ObjectInitializer(String className, Expression[] arguments) {
        this.className = className;
        this.arguments = arguments;
    }

    public Expression[] getArguments() {
        return arguments;
    }

    public String getClassName() {
        return className;
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
