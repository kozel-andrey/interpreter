package codegen;

public class Function implements Visitable {
  private Type returnType;

  public Type getReturnType() {
    return returnType;
  }

  private String name;

  public String getName() {
    return name;
  }

  private SingleDeclaration[] parameters;

  public SingleDeclaration[] getParameters() {
    return parameters;
  }

  private Declaration[] declarations;

  public Declaration[] getDeclarations() {
    return declarations;
  }

  private Statement[] statements;

  public Statement[] getStatements() {
    return statements;
  }

  public Function(Type returnType, String name, SingleDeclaration[] parameters,
      Declaration[] declarations, Statement[] statements) {
    this.returnType = returnType;
    this.name = name;
    this.parameters = parameters;
    this.declarations = declarations;
    this.statements = statements;
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
