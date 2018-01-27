package parser;

import java.util.ArrayList;

import codegen.*;
import codegen.Number;

public class Parser {
    private static final int PADDING = 3;
    private boolean notClass;

    private static String[] lex(String input) {
        ArrayList<String> tokens = new ArrayList<>();
        int index = 0;
        while (index < input.length()) {
            char next = input.charAt(index);
            if (next == ' ' || next == '\n' || next == '\r' || next == '\t') {
                index++;
                continue;
            }
            switch (next) {
                case '{':
                case '}':
                case '(':
                case ')':
                case '[':
                case ']':
                case '+':
                case '-':
                case '/':
                case '*':
                case '%':
                case ';':
                    index++;
                    tokens.add("" + next);
                    continue;

            }
            StringBuilder tokenBuilder = new StringBuilder();
            compBinAssLoop:
            while (index < input.length() && tokenBuilder.length() < 2) {
                char nextOp = input.charAt(index);
                switch (nextOp) {
                    case '=':
                    case '!':
                    case '<':
                    case '>':
                    case '&':
                    case '|':
                        tokenBuilder.append(nextOp);
                        break;
                    default:
                        break compBinAssLoop;
                }
                index++;
            }
            if (tokenBuilder.length() == 0) {
                while (index < input.length()) {
                    char nextLetterNumber = input.charAt(index);
                    if (nextLetterNumber >= 'a' && nextLetterNumber <= 'z'
                            || nextLetterNumber >= 'A' && nextLetterNumber <= 'Z'
                            || nextLetterNumber >= '0' && nextLetterNumber <= '9')
                        tokenBuilder.append(nextLetterNumber);
                    else
                        break;
                    index++;
                }
            }
            if (tokenBuilder.length() > 0)
                tokens.add(tokenBuilder.toString());
            else {
                index++;
                tokens.add("" + next);
            }
        }
        // Padding
        for (int i = 0; i < PADDING; i++)
            tokens.add("");
        return tokens.toArray(new String[0]);
    }

    public static boolean isKeyword(String token) {
        switch (token) {
            case "true":
            case "false":
            case "int":
            case "while":
            case "if":
            case "else":
            case "read":
            case "write":
                return true;
            default:
                return false;
        }
    }

    private String[] tokens;

    private int from;

    public Parser(String code) {
        this.tokens = lex(code);
    }

    public Number parseNumber() {
        for (int i = 0; i < tokens[from].length(); i++) {
            char next = tokens[from].charAt(i);
            if (!(next >= '0' && next <= '9'))
                return null;
        }
        return new Number(Integer.parseInt(tokens[from++]));
    }

    public String parseName() {
        if (isKeyword(tokens[from]))
            return null;
        if (tokens[from].length() == 0)
            return null;
        char first = tokens[from].charAt(0);
        if (!(first >= 'a' && first <= 'z') && !(first >= 'A' && first <= 'Z'))
            return null;
        for (int i = 1; i < tokens[from].length(); i++) {
            char next = tokens[from].charAt(i);
            if (!(next >= 'a' && next <= 'z') && !(next >= 'A' && next <= 'Z')
                    && !(next >= '0' && next <= '9'))
                return null;
        }
        return tokens[from++];
    }

    public Type parseType() {
        if (tokens[from++].equals("int")) {
            if ((tokens[from] + tokens[from + 1]).equals("[]")) {
                from += 2;
                return new Type("int[]");
            }
            return new Type("int");
        } else {
            if (isClassDefined(tokens[from - 1])) {
                return new Type(tokens[from - 1]);
            }
        }
        return null;
    }

    private boolean isClassDefined(String name) {
        if (tokens.length == 0) return false;

        for (int i = 0; i < tokens.length; i++) {
            if (name.equals(tokens[i])) {

                if (i == 0) return false;
                if (tokens[i - 1].equals("class")) return true;

            }
        }

        return false;
    }

    public Declaration parseDecl() {
        Type type = parseType();
        if (type == null)
            return null;

        ArrayList<String> variables = new ArrayList<String>();
        while (true) {
            String name = parseName();
            if (name == null)
                return null;
            variables.add(name);
            if (!tokens[from].equals(","))
                break;
            from++;
        }
        if (tokens[from].equals(";")) {
            from += 1;
            return new Declaration(type, variables.toArray(new String[]{}));
        } else
            return null;
    }

    public Unop parseUnop() {
        if (tokens[from].equals("-")) {
            from = from + 1;
            return Unop.Minus;
        }
        return null;
    }

    public Binop parseBinop() {
        switch (tokens[from]) {
            case "+":
                from++;
                return Binop.Plus;
            case "-":
                from++;
                return Binop.Minus;
            case "*":
                from++;
                return Binop.MultiplicationOperator;
            case "/":
                from++;
                return Binop.DivisionOperator;
            case "%":
                from++;
                return Binop.Modulo;
            default:
                return null;
        }
    }

    public Comp parseComp() {
        switch (tokens[from]) {
            case "==":
                from++;
                return Comp.Equals;
            case "!=":
                from++;
                return Comp.NotEquals;
            case "<=":
                from++;
                return Comp.LessEqual;
            case "<":
                from++;
                return Comp.Less;
            case ">=":
                from++;
                return Comp.GreaterEqual;
            case ">":
                from++;
                return Comp.Greater;
            default:
                return null;
        }
    }

    public Expression parseExpressionNoBinop() {
        // <number>
        int before = from;
        Expression number = parseNumber();
        if (number != null)
            return number;
        from = before;
        // new int[...]
        if (tokens[from].equals("new") && tokens[from + 1].equals("int") && tokens[from + 2].equals("[")) {
            from += 3;
            Expression length = parseExpression();
            if (length == null)
                return null;
            if (tokens[from++].equals("]"))
                return new ArrayInitializer(length);
            else
                return null;
        }
        // <name>( ... )
        from = before;

        if(tokens[from].equals("new") && isClassDefined(tokens[from + 1])) {
            from++;
        }

        String functionName = parseName();
        if (functionName != null && tokens[from].equals("(")) {
            from++;
            ArrayList<Expression> arguments = new ArrayList<Expression>();
            if (!tokens[from].equals(")"))
                argumentLoop:while (true) {
                    Expression nextArgument = parseExpression();
                    if (nextArgument == null)
                        return null;
                    arguments.add(nextArgument);
                    switch (tokens[from]) {
                        case ",":
                            from++;
                            continue;
                        case ")":
                            break argumentLoop;
                        default:
                            return null;
                    }
                }
            if (!tokens[from++].equals(")"))
                return null;
            if (functionName.equals("length") && arguments.size() == 1)
                return new ArrayLength(arguments.get(0));
            if (isClassDefined(tokens[before + 1]))
                return new ObjectInitializer(functionName, arguments.toArray(new Expression[]{}));
            else
                return new Call(functionName, arguments.toArray(new Expression[]{}));
        }
        // <name>
        from = before;
        String name = parseName();
        if (name != null) {
            if (tokens[from].equals(".")) {
                from++;
                return new MethodCall(new Variable(name), parseExpression());
            } else {
                return new Variable(name);
            }
        }
        from = before;
        // (<expr>)
        from = before;
        if (tokens[from].equals("(")) {
            from += 1;
            Expression sub = parseExpression();
            if (sub == null)
                return null;
            if (tokens[from].equals(")")) {
                from = from + 1;
                return sub;
            } else
                return null;
        }
        // <unop> <expr>
        from = before;
        Unop unop = parseUnop();
        if (unop != null) {
            Expression sub = parseExpression();
            if (sub != null)
                return new Unary(unop, sub);
            else
                return null;
        }
        return null;
    }

    private Expression tryParseArraySquareBracketExpression() {
        int before = from;
        if (tokens[from].equals("[")) {
            from++;
            Expression index = parseExpression();
            if (index == null) {
                from = before;
                return null;
            }
            if (!tokens[from++].equals("]")) {
                from = before;
                return null;
            }
            return index;
        }
        if (isClassDefined(tokens[from])) {

        }
        return null;
    }

    public Expression parseExpression() {
        Expression current = parseExpressionNoBinop();
        if (current == null)
            return null;

        {
            Expression index = tryParseArraySquareBracketExpression();
            if (index != null)
                current = new ArrayAccess(current, index);
        }

        while (true) {
            int before = from;
            Binop binop = parseBinop();
            if (binop == null) {
                from = before;
                break;
            }
            Expression next = parseExpressionNoBinop();
            if (next == null)
                return null;
            Expression index = tryParseArraySquareBracketExpression();
            if (index != null)
                next = new ArrayAccess(next, index);
            current = new Binary(current, binop, next);
        }

        return current;
    }

    public Bbinop parseBbinop() {
        switch (tokens[from]) {
            case "&&":
                from++;
                return Bbinop.And;
            case "||":
                from++;
                return Bbinop.Or;
            default:
                return null;
        }
    }

    public Bunop parseBunop() {
        if (tokens[from].equals("!")) {
            from++;
            return Bunop.Not;
        }
        return null;
    }

    public Condition parseConditionNoBinary() {
        int before = from;
        // true
        if (tokens[from].equals("true")) {
            from++;
            return new True();
        }
        // false
        if (tokens[from].equals("false")) {
            from++;
            return new False();
        }
        // <expr> <comp> <expr>
        Expression lhs = parseExpression();
        if (lhs != null) {
            Comp comp = parseComp();
            if (comp == null)
                return null;
            Expression rhs = parseExpression();
            if (rhs == null)
                return null;
            return new Comparison(lhs, comp, rhs);
        }
        // (<cond>)
        from = before;
        if (tokens[from].equals("(")) {
            from += 1;
            Condition sub = parseCondition();
            if (sub == null)
                return null;
            if (tokens[from].equals(")")) {
                from++;
                return sub;
            } else {
                return null;
            }
        }
        // <bunop> ( <cond> )
        from = before;
        Bunop bunop = parseBunop();
        if (bunop != null) {
            if (tokens[from].equals("(")) {
                from += 1;
                Condition sub = parseCondition();
                if (sub == null)
                    return null;
                if (tokens[from].equals(")")) {
                    from++;
                    return new UnaryCondition(bunop, sub);
                } else
                    return null;
            }
        }
        return null;
    }

    public Condition parseCondition() {
        Condition current = parseConditionNoBinary();
        if (current == null)
            return null;

        while (true) {
            int before = from;
            Bbinop bbinop = parseBbinop();
            if (bbinop == null) {
                from = before;
                break;
            }
            Condition rhs = parseConditionNoBinary();
            if (rhs == null)
                return null;
            current = new BinaryCondition(current, bbinop, rhs);
        }
        return current;
    }

    public Statement parseAssigment() {
        String variableName = parseName();
        if (variableName == null)
            return null;
        Expression index = tryParseArraySquareBracketExpression();
        if (!tokens[from++].equals("="))
            return null;
        if (index == null && from + 2 < tokens.length
                && (tokens[from] + tokens[from + 1] + tokens[from + 2]).equals("read()")) {
            from = from + 3;
            return new Read(variableName);
        }
        Expression exp = parseExpression();
        if (exp == null)
            return null;
        if (index == null)
            return new Assignment(variableName, exp);
        else
            return new ArrayAssignment(variableName, index, exp);
    }

    public Condition parseWhileIteCond() {
        if (tokens[from].equals("("))
            from++;
        else {
            return null;
        }
        Condition cond = parseCondition();
        if (cond == null)
            return null;
        if (!tokens[from].equals(")"))
            return null;
        from = from + 1;
        return cond;
    }

    public Statement parseIte() {
        if (!tokens[from].equals("if"))
            return null;
        from++;
        Condition cond = parseWhileIteCond();
        if (cond == null)
            return null;
        Statement thenBranch = parseStatement();
        if (thenBranch == null)
            return null;
        if (from < tokens.length && tokens[from].equals("else")) {
            from++;
            Statement elseBranch = parseStatement();
            if (elseBranch == null)
                return null;
            return new IfThenElse(cond, thenBranch, elseBranch);
        } else
            return new IfThen(cond, thenBranch);
    }

    public Statement parseStatement() {
        switch (tokens[from]) {
            case ";": {
                from = from + 1;
                return new Composite(new Statement[]{});
            }
            case "{": {
                ArrayList<Statement> statements = parseStatements();
                if (tokens[from++].equals("}")) {
                    return new Composite(statements.toArray(new Statement[]{}));
                } else
                    return null;
            }
            case "while": {
                from++;
                Condition cond = parseWhileIteCond();
                if (cond == null)
                    return null;
                Statement body = parseStatement();
                if (body == null)
                    return null;
                return new While(cond, body);
            }
            case "if": {
                Statement ite = parseIte();
                if (ite == null)
                    return null;
                return ite;
            }
            case "write": {
                if (!tokens[from + 1].equals("("))
                    return null;
                from += 2;
                Expression exp = parseExpression();
                if (exp == null || !tokens[from].equals(")"))
                    return null;
                if (!tokens[from + 1].equals(";"))
                    return null;
                from = from + 2;
                return new Write(exp);
            }
            case "return": {
                from++;
                Expression exp = parseExpression();
                if (exp == null)
                    return null;
                if (!tokens[from++].equals(";"))
                    return null;
                return new Return(exp);
            }
            default: {
                Statement ass = parseAssigment();
                if (ass != null) {
                    if (tokens[from].equals(";")) {
                        from += 1;
                        return ass;
                    } else
                        return null;
                }
                return null;
            }
        }
    }

    public SingleDeclaration[] parseFunctionParameterList() {
        if (!tokens[from++].equals("("))
            return null;
        ArrayList<SingleDeclaration> parameters = new ArrayList<>();
        if (!tokens[from].equals(")")) {
            do {
                Type type = parseType();
                if (type == null)
                    return null;
                String parameter = parseName();
                if (parameter == null)
                    return null;
                parameters.add(new SingleDeclaration(type, parameter));
            } while (tokens[from++].equals(","));
            from--;
        }
        if (!tokens[from++].equals(")"))
            return null;
        return parameters.toArray(new SingleDeclaration[]{});
    }

    public Function parseFunction() {
        Type returnType = parseType();
        if (returnType == null)
            return null;
        String functionName = parseName();
        if (functionName == null)
            return null;
        SingleDeclaration[] parameters = parseFunctionParameterList();
        if (parameters == null)
            return null;
        if (!tokens[from++].equals("{"))
            return null;

        ArrayList<Declaration> declarations = parseDeclarations();
        ArrayList<Statement> statements = parseStatements();

        if (!tokens[from++].equals("}"))
            return null;
        return new Function(returnType, functionName, parameters,
                declarations.toArray(new Declaration[]{}), statements.toArray(new Statement[]{}));
    }

    private ArrayList<Statement> parseStatements() {
        ArrayList<Statement> statements = new ArrayList<>();
        while (true) {
            int before = from;
            Statement stmt = parseStatement();
            if (stmt == null) {
                from = before;
                break;
            }
            statements.add(stmt);
        }
        return statements;
    }

    private ArrayList<Declaration> parseDeclarations() {
        ArrayList<Declaration> declarations = new ArrayList<>();
        while (true) {
            int before = from;
            Declaration decl = parseDecl();
            if (decl == null) {
                from = before;
                break;
            }
            declarations.add(decl);
        }
        return declarations;
    }

    private CustomClass parseCustomClass() {
        if (isNotClass()) return null;

        String className = parseName();

        if (className == null)
            return null;

        if (!tokens[from++].equals("{"))
            return null;

        ArrayList<Declaration> declarations = parseDeclarations();
        ArrayList<Function> functions = new ArrayList<>();
        int before;
        Constructor constructor = null;
        while (true) {
            before = from;
            Function function = parseFunction();
            if (function == null) {
                from = before;
            } else {
                functions.add(function);
                before = from;
            }
            Constructor parsedConstructor = parseConstuctor(className);
            if (parsedConstructor == null) {
                from = before;
            } else {
                if (constructor != null) {
                    throw new IllegalStateException("Two constructors are not allowed!");
                }
                constructor = parsedConstructor;
            }
            if (constructor == null && function == null) {
                return null;
            }
            if (tokens[from].equals("}")) {
                from++;
                return new CustomClass(className, declarations.toArray(new Declaration[declarations.size()]),
                        constructor, functions.toArray(new Function[functions.size()]));
            }
        }

    }

    private Constructor parseConstuctor(String className) {
        if (!isConstructor(className)) {
            return null;
        }

        SingleDeclaration[] parameters = parseFunctionParameterList();
        if (parameters == null)
            return null;
        if (!tokens[from++].equals("{"))
            return null;

        ArrayList<Declaration> declarations = parseDeclarations();
        ArrayList<Statement> statements = parseStatements();

        if (!tokens[from++].equals("}"))
            return null;

        return new Constructor(className, parameters, declarations.toArray(new Declaration[]{}),
                statements.toArray(new Statement[]{}));
    }

    private boolean isConstructor(String className) {
        return tokens[from++].equals(className);
    }

    private Program parseProgram() {

        int before;

        ArrayList<Function> functions = new ArrayList<>();
        ArrayList<CustomClass> classes = new ArrayList<>();
        while (true) {
            before = from;
            Function function = parseFunction();
            if (function == null) {
                from = before;
            } else {
                functions.add(function);
                before = from;
            }

            CustomClass customClass = parseCustomClass();
            if (customClass == null) {
                from = before;
            } else {
                classes.add(customClass);
            }
            if (customClass == null && function == null) {
                break;
            }
        }

        return new Program(functions.toArray(new Function[]{}), classes.toArray(new CustomClass[]{}));
    }

    public Program parse() {
        from = 0;
        Program program = parseProgram();
        // Consume padding
        while (from < this.tokens.length && this.tokens[from].length() == 0)
            from++;
        if (from == this.tokens.length)
            return program;
        return null;
    }

    public boolean isNotClass() {
        return !tokens[from++].equals("class");
    }
}