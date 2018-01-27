package codegen;

public class SingleDeclaration implements Visitable {
  private Type type;
  
  public Type getType() {
    return type;
  }
  
  private String name;
  
  public String getName() {
    return name;
  }
  
  public SingleDeclaration(Type type, String a) {
    this.type = type;
    this.name = a;
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
