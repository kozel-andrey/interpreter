package codegen;

public class ArrayLength extends Expression {
  private Expression array;
  
  public Expression getArray() {
    return array;
  }
  
  public ArrayLength(Expression array) {
    super();
    this.array = array;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public int firstLevelPriority() {
    return 0;
  }

}
