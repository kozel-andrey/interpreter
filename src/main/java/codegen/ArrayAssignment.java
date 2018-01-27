package codegen;

public class ArrayAssignment extends Statement {
  private String name;
  
  public String getName() {
    return name;
  }
  
  private Expression index;
  
  public Expression getIndex() {
    return index;
  }
  
  private Expression rhs;
  
  public Expression getRhs() {
    return rhs;
  }
  
  public ArrayAssignment(String name, Expression index, Expression rhs) {
    super();
    this.name = name;
    this.index = index;
    this.rhs = rhs;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

}
