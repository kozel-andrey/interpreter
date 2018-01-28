package com.romanrichter.interpreter.codegen;

public class Constructor implements Visitable {

    private SingleDeclaration[] parameters;
    private Declaration[] declarations;
    private Statement[] statements;
    private String name;

    public Constructor(String name, SingleDeclaration[] parameters, Declaration[] declarations, Statement[] statements) {
        this.parameters = parameters;
        this.declarations = declarations;
        this.statements = statements;
        this.name = name;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public SingleDeclaration[] getParameters() {
        return parameters;
    }

    public Declaration[] getDeclarations() {
        return declarations;
    }

    public Statement[] getStatements() {
        return statements;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        FormatVisitor visitor = new FormatVisitor();
        accept(visitor);
        return visitor.getResult();
    }

}
