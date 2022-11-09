package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {

    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */

    List<Instruction> irInstruction;
    List<String> asm = new ArrayList<>();
    Map<IRValue, Integer> regMap = new HashMap<>();
    Map<Integer, IRValue> irMap = new HashMap<>();
    Boolean[] isAlloc = new Boolean[7];
    Boolean[] isTemp = new Boolean[7];
    int lastRecycle = 0;


    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
//        throw new NotImplementedException();
        this.irInstruction = originInstructions;
    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */

    private void allocReg(IRValue ir) {
        int firstAvailable = IntStream.range(0, 7)
                .filter(it -> !isAlloc[it])
                .findFirst()
                .orElse(-1);
        if (firstAvailable != -1) {
            if (ir.isIRVariable()) {
                IRVariable irVariable = (IRVariable) ir;
                isTemp[firstAvailable] = irVariable.isTemp();
            } else {
                isTemp[firstAvailable] = true;
            }
            isAlloc[firstAvailable] = true;
            irMap.put(firstAvailable, ir);
            regMap.put(ir, firstAvailable);
        } else {
            int firstTemp = IntStream.range(lastRecycle, 7)
                    .filter(it -> isTemp[it])
                    .findFirst()
                    .orElse(0);
            lastRecycle = (firstTemp + 1) % 7;
            irMap.put(firstTemp, ir);
            regMap.put(ir, firstTemp);
        }
    }

    public void run() {
        // TODO: 执行寄存器分配与代码生成
//        throw new NotImplementedException();
        System.out.println("---- ASM Gen ----");
        for (int i = 0; i < 7; i++) {
            isAlloc[i] = false;
            isTemp[i] = false;
        }

        int index = 0;
        for (Instruction instruction : irInstruction) {
            switch (instruction.getKind()) {
                case MOV -> {
                    IRValue result = instruction.getResult();
                    IRValue from = instruction.getFrom();
                    if (!regMap.containsKey(result)) {
                        allocReg(result);
                    }
                    Integer regResult = regMap.get(result);
                    if (from.isImmediate()) {
                        IRImmediate immFrom = (IRImmediate) from;
                        asm.add("li t%d %d".formatted(regResult, immFrom.getValue()));
                    } else {
                        Integer regFrom = regMap.get(from);
                        asm.add("mv t%d t%d".formatted(regResult, regFrom));
                    }
                }
                case ADD -> {
                    IRValue result = instruction.getResult();
                    IRValue lhs = instruction.getLHS();
                    IRValue rhs = instruction.getRHS();
                    if (!regMap.containsKey(result)) {
                        allocReg(result);
                    }
                    if (!lhs.isImmediate() && !rhs.isImmediate()) {
                        Integer regResult = regMap.get(result);
                        Integer regLHS = regMap.get(lhs);
                        Integer regRHS = regMap.get(rhs);
                        asm.add(
                                "add t%d t%d t%d"
                                        .formatted(regResult, regLHS, regRHS)
                        );
                    } else {
                        boolean isLhsImm = lhs.isImmediate();
                        IRImmediate imm = (IRImmediate) (isLhsImm ? lhs : rhs);
                        Integer regResult = regMap.get(result);
                        Integer regVar = regMap.get(isLhsImm ? rhs : lhs);
                        asm.add(
                                "addi t%d t%d %d"
                                        .formatted(regResult, regVar, imm.getValue())
                        );
                    }
                }
                case SUB -> {
                    IRValue result = instruction.getResult();
                    IRValue lhs = instruction.getLHS();
                    IRValue rhs = instruction.getRHS();
                    if (!regMap.containsKey(result)) {
                        allocReg(result);
                    }
                    if (!lhs.isImmediate() && !rhs.isImmediate()) {
                        Integer regResult = regMap.get(result);
                        Integer regLHS = regMap.get(lhs);
                        Integer regRHS = regMap.get(rhs);
                        asm.add(
                                "sub t%d t%d t%d"
                                        .formatted(regResult, regLHS, regRHS)
                        );
                    } else {
                        if (lhs.isImmediate()) {
                            IRImmediate imm = (IRImmediate) lhs;
                            if (!regMap.containsKey(lhs)) {
                                allocReg(lhs);
                            }
                            Integer regResult = regMap.get(result);
                            Integer regRHS = regMap.get(rhs);
                            Integer regLHS = regMap.get(lhs);
                            asm.add(
                                    "li t%d %d"
                                            .formatted(regLHS, imm.getValue())
                            );
                            asm.add(
                                    "sub t%d t%d t%d"
                                            .formatted(regResult, regLHS, regRHS)
                            );
                        } else {
                            IRImmediate imm = (IRImmediate) rhs;
                            Integer regResult = regMap.get(result);
                            Integer regLHS = regMap.get(lhs);
                            asm.add(
                                    "addi t%d t%d t%d"
                                            .formatted(regResult, regLHS, -imm.getValue())
                            );
                        }
                    }
                }
                case MUL -> {
                    IRValue result = instruction.getResult();
                    IRValue lhs = instruction.getLHS();
                    IRValue rhs = instruction.getRHS();
                    if (!regMap.containsKey(result)) {
                        allocReg(result);
                    }
                    if (!lhs.isImmediate() && !rhs.isImmediate()) {
                        Integer regResult = regMap.get(result);
                        Integer regLHS = regMap.get(lhs);
                        Integer regRHS = regMap.get(rhs);
                        asm.add(
                                "mul t%d t%d t%d"
                                        .formatted(regResult, regLHS, regRHS)
                        );
                    } else {
                        boolean isLhsImm = lhs.isImmediate();
                        IRImmediate imm = (IRImmediate) (isLhsImm ? lhs : rhs);
                        if (!regMap.containsKey(imm)) {
                            allocReg(imm);
                        }
                        Integer regResult = regMap.get(result);
                        Integer regRHS = regMap.get(rhs);
                        Integer regLHS = regMap.get(lhs);
                        asm.add(
                                "li t%d %d"
                                        .formatted(isLhsImm ? regLHS : regRHS, imm.getValue())
                        );
                        asm.add(
                                "mul t%d t%d t%d"
                                        .formatted(regResult, isLhsImm ? regRHS : regLHS, isLhsImm ? regLHS : regRHS)
                        );
                    }
                }
                case RET -> {
                    IRValue ret = instruction.getReturnValue();
                    asm.add("mv a0 t%d".formatted(regMap.get(ret)));
                }
            }
        }
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
//        throw new NotImplementedException();
        asm.forEach(System.out::println);
        FileUtils.writeLines(path, asm);
    }
}

