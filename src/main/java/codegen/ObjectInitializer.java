package codegen;

public class ObjectInitializer extends Expression {

    private Expression expression;
    private String functionName;

    public ObjectInitializer(String functionName, Expression[] expressions) {
        this.functionName = functionName;
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    public String getFunctionName() {
        return functionName;
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
