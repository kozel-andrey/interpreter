package codegen;

public class ArrayInitializer extends Expression {
  private Expression length;
  
  public Expression getLength() {
    return length;
  }

  public ArrayInitializer(Expression length) {
    super();
    this.length = length;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public int firstLevelPriority() {
    return 3;
  }

}
