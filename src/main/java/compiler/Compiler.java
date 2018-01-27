package compiler;
import codegen.CodeGenerationVisitor;
import codegen.Program;
import parser.Parser;

public class Compiler {
  public static int[] compile(String code) {
    Parser p = new Parser(code);
    Program ast = p.parse();
    if (ast == null)
      throw new RuntimeException("Error: Unable to parse program");

    CodeGenerationVisitor cgv = new CodeGenerationVisitor();
    ast.accept(cgv);
//    System.out.println(Interpreter.programToString(cgv.getProgram()));
    return cgv.getProgram();
  }
}
