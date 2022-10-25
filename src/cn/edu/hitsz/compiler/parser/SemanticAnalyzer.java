package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.symtab.SymbolTableEntry;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {

    private SymbolTable symbolTable;

    private Stack<String> semanticStack = new Stack<>();

    public SemanticAnalyzer() {
        semanticStack.push("$");
    }

    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
//        throw new NotImplementedException();
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
//        System.out.println("reduce " + currentStatus.index() + production.toString());

        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
//        throw new NotImplementedException();
        switch (currentStatus.index()) {
            case 5 -> {
            }
            case 8 -> {
                ArrayList<String> info = new ArrayList<>();
                production.body().forEach(it -> {
                    info.add(semanticStack.peek());
                    semanticStack.pop();
                });
//                System.out.println("set " + info.get(0) + " Int");
                symbolTable.get(info.get(0)).setType(SourceCodeType.Int);
                semanticStack.push("");
            }
            default -> {
                production.body().forEach(it -> {
                    semanticStack.pop();
                });
                semanticStack.push("");
            }
        }
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
//        throw new NotImplementedException();
        switch (currentToken.getKindId()) {
            case "int" -> {
                semanticStack.push("int");
            }
            case "id" -> {
                semanticStack.push(currentToken.getText());
            }
            default -> {
                semanticStack.push("");
            }
        }
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
//        throw new NotImplementedException();
        this.symbolTable = table;
    }
}

