package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    private BufferedReader codeReader = null;

    private List<Token> tokens;

    private int start = 0;
    private int current = 0;
    private int line = 0;
    private String sourceCurrentLine;
    private char c;


    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
        try {
            codeReader = new BufferedReader(new FileReader(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        throw new NotImplementedException();
    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
        tokens = new ArrayList<Token>();
        System.out.println("Parsing ...");
        try {
            while ((sourceCurrentLine = codeReader.readLine()) != null) {
                line++;
                current = 0;
//                System.out.println(sourceCurrentLine);
                while (current < sourceCurrentLine.length()) {
                    start = current;
                    c = sourceCurrentLine.charAt(current++);
                    switch (c) {
                        case '=':
                        case ',':
                        case '+':
                        case '-':
                        case '*':
                        case '/':
                        case '(':
                        case ')':
                            addToken(Token.simple(String.valueOf(c)));
                            break;
                        case ';':
                            addToken(Token.simple("Semicolon"));
                            break;
                        case ' ':
                        case '\r':
                        case '\t':
                        case '\n':
                            // Ignore whitespace.
                            break;
                        default:
                            if (Character.isDigit(c)) {
                                number();
                            } else if (Character.isAlphabetic(c)) {
//                                System.out.println("id or keyword");
                                identifier();
                            } else {
                                System.out.println("line " + line + " Unexpected character.");
                            }
                            break;
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        tokens.add(Token.eof());
//        throw new NotImplementedException();

    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
//        throw new NotImplementedException();
        return tokens;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
                path,
                StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }

    private void addToken(Token t) {
        tokens.add(t);
    }

    private boolean isAtEnd() {
        return current >= sourceCurrentLine.length();
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return sourceCurrentLine.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= sourceCurrentLine.length()) return '\0';
        return sourceCurrentLine.charAt(current + 1);
    }

    private void number() {
        while (Character.isDigit(peek())) current++;
        addToken(Token.normal("IntConst", sourceCurrentLine.substring(start, current)));
    }

    private void identifier() {
        while (Character.isAlphabetic(peek()) || Character.isDigit(peek()) || peek() == '_') current++;
        String text = sourceCurrentLine.substring(start, current);
//        System.out.println(text);
        if (TokenKind.isAllowed(text)) {
            addToken(Token.normal(TokenKind.fromString(text), ""));
        } else {
            addToken(Token.normal("id", text));
            symbolTable.add(text);
        }
    }
}
