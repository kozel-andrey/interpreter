package codegen;

public class MethodCall extends Expression {

    private Expression variable;
    private Expression call;

    public MethodCall(Expression variable, Expression call) {
        this.variable = variable;
        this.call = call;
    }

    public Expression getVariable() {
        return variable;
    }

    public Expression getCall() {
        return call;
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
