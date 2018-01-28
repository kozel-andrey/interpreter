package com.romanrichter.interpreter.codegen;

public class CustomClass implements Visitable {

    private String name;
    private Declaration[] declarations;
    private Constructor constructor;
    private Function[] functions;
    private String parentName;

    public CustomClass(String className, String parentName, Declaration[] declarations, Constructor constructor, Function[] functions) {
        this.name = className;
        this.parentName = parentName;
        this.declarations = declarations;
        this.constructor = constructor;
        this.functions = functions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor constructor) {
        this.constructor = constructor;
    }

    public Declaration[] getDeclarations() {
        return declarations;
    }

    public void setDeclarations(Declaration[] declarations) {
        this.declarations = declarations;
    }

    public Function[] getFunctions() {
        return functions;
    }

    public void setFunctions(Function[] functions) {
        this.functions = functions;
    }

    public String getParentName() {
        return parentName;
    }

    @Override
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
