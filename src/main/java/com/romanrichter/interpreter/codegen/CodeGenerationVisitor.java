package com.romanrichter.interpreter.codegen;

import static com.romanrichter.interpreter.codegen.interpreter.SteckOperation.*;

import java.lang.*;
import java.util.ArrayList;
import java.util.HashMap;

class PatchLocation {
  public final String functionName;
  public final int argumentCount;
  public final int ldiLocation;

  public PatchLocation(String functionName, int argumentCount, int ldiLocation) {
    this.functionName = functionName;
    this.argumentCount = argumentCount;
    this.ldiLocation = ldiLocation;
  }
}


class FunctionDesc {
  public final int functionIndex;
  public final int argumentCount;

  public FunctionDesc(int functionIndex, int argumentCount) {
    this.functionIndex = functionIndex;
    this.argumentCount = argumentCount;
  }
}


public class CodeGenerationVisitor extends Visitor {
  /*
   * Liste für die generierten Instruktionen. Eine ArrayList verhält sich wie ein Array, welches
   * wachsen kann.
   */
  private ArrayList<Integer> instructions = new ArrayList<Integer>();
  /*
   * HashMap für lokale Variablen. Man beachte, dass es lokale Variablen nur als Funktionsparameter
   * und am Anfang von Funktionen gibt; daher gibt es nur genau einen Scope.
   */
  private HashMap<String, Integer> locals = new HashMap<String, Integer>();
  /*
   * Frame-Zellen der aktuellen Funktion. Wird für den Rücksprung benötigt.
   */
  private int frameCells = 0;
  /*
   * Funktionen, die bereits assembliert wurden. Wir merken uns alle Funktionen, sodass wir am Ende
   * die Call-Sites patchen können.
   */
  private HashMap<String, FunctionDesc> functions = new HashMap<>();
  /*
   * Call-Sites; Funktionsaufrufe müssen ganz am Ende generiert werden, nämlich dann, wenn die
   * Funktionen schon bekannte Adressen haben.
   */
  private ArrayList<PatchLocation> patchLocations = new ArrayList<>();

  public int[] getProgram() {
    // Umwandlung von Integer[] nach int[]
    Integer[] program = instructions.toArray(new Integer[0]);
    int[] intProgram = new int[program.length];
    for (int i = 0; i < intProgram.length; i++)
      intProgram[i] = program[i];
    return intProgram;
  }

  private int add(int instruction) {
    int index = instructions.size();
    instructions.add(instruction);
    return index;
  }

  private int addDummy() {
    return add(0);
  }

  /*
   * Expression
   */

  @Override
  public void visit(Number number) {
    int value = number.getValue();
    add(LDI.encode(value & 0xffff));
    // Für den Fall, dass der Wert mehr als 16 Bit braucht, benötigen
    // wir mehrere Operationen. Wir legen die oberen 16 Bit auf den Stack,
    // schieben diese an die richtige Stelle, und vereinen zum Schluss die
    // oberen und unteren Bits.
    if (value > 0xffff || value < 0) {
      add(LDI.encode(value >>> 16));
      add(SHL.encode(16));
      add(OR.encode());

    }
  }

  @Override
  public void visit(Variable variable) {
    if (!locals.containsKey(variable.getName()))
      throw new RuntimeException("Unknown variable '" + variable.getName() + "'");
    int variableLocation = locals.get(variable.getName());
    add(LDS.encode(variableLocation));
  }

  @Override
  public void visit(Unary unary) {
    switch (unary.getOperator()) {
      case Minus:
        unary.getOperand().accept(this);
        add(LDI.encode(0));
        add(SUB.encode());
        break;
    }
  }

  @Override
  public void visit(Binary binary) {
    binary.getRhs().accept(this);
    binary.getLhs().accept(this);
    switch (binary.getOperator()) {
      case Minus:
        add(SUB.encode());
        break;
      case Plus:
        add(ADD.encode());
        break;
      case MultiplicationOperator:
        add(MUL.encode());
        break;
      case DivisionOperator:
        add(DIV.encode());
        break;
      case Modulo:
        add(MOD.encode());
        break;
    }
  }

  @Override
  public void visit(Call call) {
    for (Expression e : call.getArguments())
      e.accept(this);
    int patchLocation = addDummy();
    // Wir können die Instruktion zum Laden der Adresse hier noch nicht generieren; die
    // Funktion wird evtl. erst später assembliert.
    patchLocations
        .add(new PatchLocation(call.getFunctionName(), call.getArguments().length, patchLocation));
    // Padding for the tail call optimization
    for (int i = 0; i < call.getArguments().length; i++)
      add(NOP.encode());
    add(CALL.encode(call.getArguments().length));
  }

  @Override
  public void visit(ArrayAccess arrayAccess) {
    arrayAccess.getIndex().accept(this);
    // Wir addieren 1 auf den Index, da an Offset 0 die Länge des Arrays gespeichert ist.
    add(LDI.encode(1));
    add(ADD.encode());
    arrayAccess.getArray().accept(this);
    add(LDH.encode());
  }

  @Override
  public void visit(ArrayInitializer arrayInitializer) {
    arrayInitializer.getLength().accept(this);
    // Kopie der Länge in t0 speichern
    add(STS.encode(locals.get("$t0")));
    add(LDS.encode(locals.get("$t0")));
    // Länge für Heap-Allokation um 1 erhöhen (Platz für Länge)
    add(LDI.encode(1));
    add(ADD.encode());
    // Heap-Allokation durchführen
    add(ALLOCH.encode());
    // Kopie der Heap-Referenz in t1 speichern
    add(STS.encode(locals.get("$t1")));
    
    // Array Initialisieren (heißt das length-Feld an Offset 0 setzen)
    // Länge laden
    add(LDS.encode(locals.get("$t0")));
    // Offset 0 laden
    add(LDI.encode(0));
    // Heap-Referenz laden
    add(LDS.encode(locals.get("$t1")));
    // Länge an Offset 0 von Array schreiben
    add(STH.encode());

    // Heap-Referenz laden (als Ergebnis auf dem Stack)
    add(LDS.encode(locals.get("$t1")));
  }
  
  @Override
  public void visit(ArrayLength arrayLength) {
    // Die Länge des Arrays ist im Heap an Offset 0 gespeichert
    add(LDI.encode(0));
    arrayLength.getArray().accept(this);
    add(LDH.encode());
  }

  /*
   * Statement
   */

  @Override
  public void visit(Read read) {
    add(IN.encode());
    int location = locals.get(read.getName());
    add(STS.encode(location));
  }

  @Override
  public void visit(Write write) {
    write.getExpression().accept(this);
    add(OUT.encode());
  }

  @Override
  public void visit(Assignment assignment) {
    assignment.getExpression().accept(this);
    Integer location = locals.get(assignment.getName());
    if (location == null)
      throw new RuntimeException("Unknown variable " + assignment.getName());
    add(STS.encode(location));
  }

  @Override
  public void visit(Composite composite) {
    for (Statement s : composite.getStatements())
      s.accept(this);
  }

  @Override
  public void visit(IfThenElse ifThenElse) {
    ifThenElse.getCond().accept(this);
    int jumpToElseAddress = addDummy();
    ifThenElse.getElseBranch().accept(this);
    new True().accept(this);
    int jumpToEndAddress = addDummy();
    // Wir setzen den Sprung zum Else-Zweig
    instructions.set(jumpToElseAddress, JUMP.encode(instructions.size()));
    ifThenElse.getThenBranch().accept(this);
    // Wir setzen den Sprung zum Ende am Ende des Else-Zweiges
    instructions.set(jumpToEndAddress, JUMP.encode(instructions.size()));
  }

  @Override
  public void visit(IfThen ifThen) {
    ifThen.getCond().accept(this);
    add(NOT.encode());
    int jumpToEndAddress = addDummy();
    ifThen.getThenBranch().accept(this);
    instructions.set(jumpToEndAddress, JUMP.encode(instructions.size()));
  }

  @Override
  public void visit(While while_) {
    int whileBeginAddress = instructions.size();
    while_.getCond().accept(this);
    add(NOT.encode());
    int jumpToEndAddress = addDummy();
    while_.getBody().accept(this);
    new True().accept(this);
    add(JUMP.encode(whileBeginAddress));
    int whileEndAddress = instructions.size();
    // Wir setzen den Sprung ans Ende der Schleife
    instructions.set(jumpToEndAddress, JUMP.encode(whileEndAddress));
  }

  @Override
  public void visit(Return return_) {
    return_.getExpression().accept(this);
    add(RETURN.encode(frameCells));
  }

  @Override
  public void visit(EmptyStatement emptyStatement) {}

  @Override
  public void visit(ArrayAssignment arrayAssignment) {
    arrayAssignment.getRhs().accept(this);
    arrayAssignment.getIndex().accept(this);
    // Wir addieren 1 auf den Index, da an Offset 0 die Länge des Arrays gespeichert ist.
    add(LDI.encode(1));
    add(ADD.encode());
    int variableLocation = locals.get(arrayAssignment.getName());
    add(LDS.encode(variableLocation));
    add(STH.encode());
  }

  /*
   * Condition
   */

  @Override
  public void visit(True true_) {
    add(LDI.encode(0));
    add(NOT.encode());
  }

  @Override
  public void visit(False false_) {
    add(LDI.encode(0));
  }

  @Override
  public void visit(Comparison comparison) {
    comparison.getRhs().accept(this);
    comparison.getLhs().accept(this);
    switch (comparison.getOperator()) {
      case Equals:
        add(EQ.encode());
        break;
      case NotEquals:
        add(EQ.encode());
        add(NOT.encode());
        break;
      case Greater:
        add(LE.encode());
        add(NOT.encode());
        break;
      case GreaterEqual:
        add(LT.encode());
        add(NOT.encode());
        break;
      case Less:
        add(LT.encode());
        break;
      case LessEqual:
        add(LE.encode());
        break;
    }
  }

  @Override
  public void visit(UnaryCondition unaryCondition) {
    unaryCondition.getOperand().accept(this);
    switch (unaryCondition.getOperator()) {
      case Not:
        add(NOT.encode());
        break;
    }
  }

  @Override
  public void visit(BinaryCondition binaryCondition) {
    binaryCondition.getRhs().accept(this);
    binaryCondition.getLhs().accept(this);
    switch (binaryCondition.getOperator()) {
      case And:
        add(AND.encode());
        break;
      case Or:
        add(OR.encode());
        break;
    }
  }

  /*
   * Rest
   */

  private void visitDeclarations(Type type, String[] names) {
    int offset = locals.size() + 1;
    for (int i = 0; i < names.length; i++) {
      if (locals.containsKey(names[i]))
        throw new RuntimeException("Variable '" + names[i] + "' is already defined");
      locals.put(names[i], offset + i);
    }
    // Beachte: Mehrere ALLOCs am Funktionsanfang
    // hintereinander sind ok
    add(ALLOC.encode(names.length));
  }

  @Override
  public void visit(SingleDeclaration singleDeclaration) {
    visitDeclarations(singleDeclaration.getType(), new String[] {singleDeclaration.getName()});
  }

  @Override
  public void visit(Declaration declaration) {
    visitDeclarations(declaration.getType(), declaration.getNames());
  }

  @Override
  public void visit(Function function) {
    // Wir müssen Platz für Generator-private Variablen schaffen (vgl. Code
    // zur Initialisierung von Arrays)
    locals.put("$t0", locals.size() + 1);
    locals.put("$t1", locals.size() + 1);
    add(ALLOC.encode(2));
    int declarations = 2;
    for (Declaration d : function.getDeclarations()) {
      declarations += d.getNames().length;
      d.accept(this);
    }
    SingleDeclaration[] parameters = function.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      if (locals.containsKey(parameters[i].getName()))
        throw new RuntimeException("Variable '" + parameters[i].getName() + "' is already defined");
      locals.put(parameters[i].getName(), -parameters.length + i + 1);
    }
    frameCells = parameters.length + declarations;
    for (Statement s : function.getStatements())
      s.accept(this);
  }

  @Override
  public void visit(CustomClass type) {

  }

  @Override
  public void visit(Constructor constructor) {

  }

  @Override
  public void visit(ObjectInitializer objectInitializer) {

  }

  @Override
  public void visit(MethodCall call) {

  }

  @Override
  public void visit(Program program) {
    // Das Programm beginnt mit einem Sprung zur Hauptfunktion
    int ldiMainAddress = addDummy();
    add(CALL.encode(0));
    add(HALT.encode());
    boolean hasMain = false;
    for (Function f : program.getFunctions()) {
      int functionStartIndex = instructions.size();
      if (f.getName().equals("main")) {
        if (f.getParameters().length > 0)
          throw new RuntimeException("Main function must not have parameters");
        instructions.set(ldiMainAddress, LDI.encode(functionStartIndex));
        hasMain = true;
      }
      functions.put(f.getName(), new FunctionDesc(functionStartIndex, f.getParameters().length));
      locals.clear();
      f.accept(this);
    }
    for (CustomClass customClass : program.getClasses()) {
      int functionStartIndex = instructions.size();
    }
    if (!hasMain)
      throw new RuntimeException("Main function is missing.");
    // Nachdem alle Funktionen assembliert wurden, müssen wir die Instruktionen patchen, die
    // Funktionsadressen laden.
    for (PatchLocation pl : patchLocations) {
      FunctionDesc fDesc = functions.get(pl.functionName);
      if (fDesc == null)
        throw new RuntimeException("Unknown function " + pl.functionName);
      if (fDesc.argumentCount != pl.argumentCount)
        throw new RuntimeException("Invalid number of function arguments");
      instructions.set(pl.ldiLocation, LDI.encode(fDesc.functionIndex));
    }
  }
}
