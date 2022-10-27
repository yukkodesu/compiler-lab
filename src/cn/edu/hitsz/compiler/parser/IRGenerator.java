package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.*;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {

    SymbolTable symbolTable;
    ArrayList<Instruction> irInstruction = new ArrayList<>();
    Stack<String> semanticStack = new Stack<>();
    Map<String, IRValue> irValues = new HashMap<>();
    ArrayList<String> info = new ArrayList<>();

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
//        throw new NotImplementedException();
        switch (currentToken.getKindId()) {
            case "id", "IntConst" -> semanticStack.push(currentToken.getText());

            default -> semanticStack.push(currentToken.getKindId());

        }
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
//        throw new NotImplementedException();
        info.clear();
        switch (production.index()) {
            case 4 -> {// S -> D id;
                production.body().forEach(it -> {
                    info.add(semanticStack.peek());
                    semanticStack.pop();
                });
                System.out.printf("Create IRVar %s\n", info.get(0));
                irValues.put(info.get(0), IRVariable.named(info.get(0)));
                semanticStack.push(info.get(0));
            }
            case 6 -> {//S -> id = E;
                production.body().forEach(it -> {
                    info.add(semanticStack.peek());
                    semanticStack.pop();
                });
                System.out.printf("Mov %s %s\n", info.get(2), info.get(0));
                instructionAdd(Instruction.createMov((IRVariable) getIRValue(info.get(2)), getIRValue(info.get(0))));
                semanticStack.push(info.get(2));
            }
            case 8, 9, 11 -> {//E -> E +/- A; //A -> A * B;
                production.body().forEach(it -> {
                    info.add(semanticStack.peek());
                    semanticStack.pop();
                });
                IRVariable var = IRVariable.temp();
                irValues.put(var.getName(), var);
//                System.out.printf("Push %s\n", var.getName());
                semanticStack.push(var.getName());
                switch (info.get(1)) {
                    case "+" -> {
                        instructionAdd(Instruction.createAdd(var, getIRValue(info.get(2)), getIRValue(info.get(0))));
                        System.out.printf("Add %s %s %s\n", var, info.get(2), info.get(0));
                    }
                    case "-" -> {
                        instructionAdd(Instruction.createSub(var, getIRValue(info.get(2)), getIRValue(info.get(0))));
                        System.out.printf("Sub %s %s %s\n", var, info.get(2), info.get(0));
                    }
                    case "*" -> {
                        System.out.printf("Mul %s %s %s\n", var, info.get(2), info.get(0));
                        instructionAdd(Instruction.createMul(var, getIRValue(info.get(2)), getIRValue(info.get(0))));
                    }
                }
            }
            case 14, 15 -> { //B -> id; B -> IntConst;
                String info = semanticStack.peek();
                if (Character.isDigit(info.charAt(0))) {
                    irValues.put(info, IRImmediate.of(Integer.parseInt(info)));
                }
            }
            case 13 -> {//B -> ( E );
                production.body().forEach(it -> {
                    info.add(semanticStack.peek());
                    semanticStack.pop();
                });
                semanticStack.push(info.get(1));
            }
            case 7 -> {//S -> return E;
                production.body().forEach(it -> {
                    info.add(semanticStack.peek());
                    semanticStack.pop();
                });
                System.out.printf("Ret %s\n", getIRValue(info.get(0)));
                instructionAdd(Instruction.createRet(getIRValue(info.get(0))));
            }
        }
    }

    private IRValue getIRValue(String s) {
        return irValues.get(s);
    }

    private void instructionAdd(Instruction ins) {
        irInstruction.add(ins);
    }

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
//        throw new NotImplementedException();
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
//        throw new NotImplementedException();
        this.symbolTable = table;
    }

    public List<Instruction> getIR() {
        // TODO
//        throw new NotImplementedException();
        return irInstruction;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

