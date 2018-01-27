package codegen;

public class ArrayAccess extends Expression {
  private Expression array;
  
  public Expression getArray() {
    return array;
  }
  
  private Expression index;
  
  public Expression getIndex() {
    return index;
  }
  
  public ArrayAccess(Expression array, Expression index) {
    super();
    this.array = array;
    this.index = index;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public int firstLevelPriority() {
    return 1;
  }

}
