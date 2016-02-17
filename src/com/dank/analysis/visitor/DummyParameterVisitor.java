package com.dank.analysis.visitor;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.util.Assembly;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Project: DankWise
 * Date: 23-02-2015
 * Time: 15:49
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class DummyParameterVisitor extends NodeVisitor {

    public static final Map<String, Integer> VALUES = new HashMap<>();

    private MethodNode mn;

    private int numberFor(AbstractInsnNode ain) {
        if (ain instanceof IntInsnNode) {
            return ((IntInsnNode) ain).operand;
        } else if (ain instanceof LdcInsnNode) {
            return (int) ((LdcInsnNode) ain).cst;
        } else if (ain instanceof InsnNode) {
            if (ain.opcode() >= ICONST_0 && ain.opcode() <= DCONST_1) {
                String opname = Assembly.OPCODES[ain.opcode()];
                return Integer.parseInt(opname.substring(opname.length() - 1));
            } else if (ain.opcode() == NOP) {
                return 0;
            } else if (ain.opcode() == ICONST_M1) {
                return -1;
            }
        }
        return Integer.MAX_VALUE;
    }

    private int validPredicateFor(JumpInsnNode jin, int predicate, boolean flip) {
        switch (jin.opcode()) {
            case IFNE:
            case IF_ICMPNE: {
                return predicate;
            }
            case IFEQ:
            case IF_ICMPEQ: {
                return predicate + 1;
            }
            case IFGE:
            case IF_ICMPGE: {
                return predicate - 1;
            }
            case IFGT:
            case IF_ICMPGT: {
                return predicate - (flip ? 1 : -1);
            }
            case IFLE:
            case IF_ICMPLE: {
                return predicate + 1;
            }
            case IFLT:
            case IF_ICMPLT: {
                return predicate + (flip ? 1 : -1);
            }
            default: {
                return predicate;
            }
        }
    }

    public void accept(MethodNode mn) {
        Type[] types = Type.getArgumentTypes(mn.desc);
        if (types.length != 0) {
            this.mn = mn;
            for (final AbstractInsnNode ain0 : mn.instructions.toArray()) {
                if (ain0.type() != AbstractInsnNode.JUMP_INSN || ain0.next() == null) continue;
                final JumpInsnNode jn = (JumpInsnNode) ain0;
                AbstractInsnNode ain = ain0.next();
                if (ain != null && (ain.opcode() == RETURN || (ain.opcode() == NEW &&
                        ((TypeInsnNode) ain).desc.equals("java/lang/IllegalStateException")))) {
                    boolean flip = false;
                    AbstractInsnNode arg = jn.previous();
                    if (arg != null) {
                        AbstractInsnNode load = arg.previous();
                        if (load != null) {
                            int predicate = numberFor(arg);
                            if (predicate == Integer.MAX_VALUE) {
                                predicate = numberFor(load);
                                flip = true;
                            }
                            if (predicate != Integer.MAX_VALUE) {
                                predicate = validPredicateFor(jn, predicate, flip);
                                VALUES.put(mn.owner.name + '.' + mn.name + mn.desc, predicate);
                            }
                        }
                    }
                }
            }
        }
    }
}
