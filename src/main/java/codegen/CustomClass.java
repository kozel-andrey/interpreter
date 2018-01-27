package codegen;

public class CustomClass implements Visitable {

    private String name;
    private Declaration[] declarations;
    private Constructor constructor;
    private Function[] functions;

    public CustomClass(String className, Declaration[] declarations, Constructor constructor, Function[] functions) {
        name = className;
        this.declarations = declarations;
        this.constructor = constructor;
        this.functions = functions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Constructor getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor constructor) {
        this.constructor = constructor;
    }

    public Declaration[] getDeclarations() {
        return declarations;
    }

    public void setDeclarations(Declaration[] declarations) {
        this.declarations = declarations;
    }

    public Function[] getFunctions() {
        return functions;
    }

    public void setFunctions(Function[] functions) {
        this.functions = functions;
    }

    @Override
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
