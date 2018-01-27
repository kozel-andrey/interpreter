package codegen;

public abstract class Expression implements Visitable {
  public abstract int firstLevelPriority();
  
  @Override
  public String toString() {
    FormatVisitor visitor = new FormatVisitor();
    accept(visitor);
    return visitor.getResult();
  }
}
