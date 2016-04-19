package com.dank.analysis.impl.character;

import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.MemberKey;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 *
 * GET_ARG_BUFFER(0);
 * PUSH DUMMY
 * INVOKE
 * MULTI
 * PUT
 *
 *
 *
 *
 *
 */
public final class AnimInterp2 extends BasicInterpreter {


    private AnimInterp2(){
    }

    @Override
    public BasicValue copyOperation(final AbstractInsnNode insn, final BasicValue value) throws AnalyzerException {
        if(insn.getOpcode()==ALOAD) {
            VarInsnNode vin = (VarInsnNode) insn;
            if(vin.var==1) {
                return new BufferArg(value.getType());
            } else if(vin.var==0) {
                return new SelfArg(value.getType());
            }
        }
        return super.copyOperation(insn, value);
    }

    @Override
    public BasicValue naryOperation(final AbstractInsnNode insn, final List<? extends BasicValue> values)
            throws AnalyzerException {
        if(insn.getOpcode()==INVOKEVIRTUAL) {
            MethodInsnNode min = (MethodInsnNode) insn;
            if(min.equals("dx","s","(B)I")) { //TODO 112
                BasicValue ref = values.get(0);
                if(ref instanceof BufferArg) {
                    return new BufferReturnValue(Type.INT_TYPE);
                }
            }
        }
        return super.naryOperation(insn, values);
    }


    @Override
    public BasicValue newOperation(final AbstractInsnNode insn) throws AnalyzerException {
        if(insn.getOpcode()==LDC) {
            LdcInsnNode ldc = (LdcInsnNode) insn;
            if(ldc.cst instanceof Integer) {
                return new NumericLDC(Type.INT_TYPE);
            }
        }
        return super.newOperation(insn);
    }

    @Override
    public BasicValue binaryOperation(final AbstractInsnNode insn, final BasicValue value1, final BasicValue value2)
            throws AnalyzerException {
        if(insn.getOpcode()==IMUL) {
            if(value1 instanceof BufferReturnValue && value2 instanceof NumericLDC) {
                return new EncodedBufferReturn(Type.INT_TYPE);
            } else if(value1 instanceof NumericLDC && value2 instanceof BufferReturnValue) {
                return new EncodedBufferReturn(Type.INT_TYPE);
            }
        } else if(insn.getOpcode()==PUTFIELD) {
            if (value1 instanceof SelfArg && value2 instanceof EncodedBufferReturn) {
                readBuffer((FieldInsnNode)insn);
            }
        }
        return super.binaryOperation(insn, value1, value2);
    }


    // Buffer read order | Hook name
    private static final Map<Integer,String> hookMap = new HashMap<>();
    static {
        hookMap.put(0,"idleAnimation");
        hookMap.put(2,"walkAnimation");
        hookMap.put(6,"runAnimation");
    }

    int index = 0;
    protected void readBuffer(FieldInsnNode dest) {
        String mnemonic = hookMap.get(index);
        if(mnemonic != null) {
            Hook.CHARACTER.put(new RSField(new MemberKey(Hook.CHARACTER.getInternalName(),dest.name,dest.desc),mnemonic));
        }
        index++;
    }


    private class SelfArg extends BasicValue {
        public SelfArg(Type type) {
            super(type);
        }
    }
    private class NumericLDC extends BasicValue {
        public NumericLDC(Type type) {
            super(type);
        }
    }
    private class BufferReturnValue extends BasicValue {
        public BufferReturnValue(Type type) {
            super(type);
        }
    }
    private class EncodedBufferReturn extends BasicValue {
        public EncodedBufferReturn(Type type) {
            super(type);
        }
    }
    private class BufferArg extends BasicValue {
        public BufferArg(Type t) {
            super(t);
        }
    }



    public static void run(String owner, MethodNode mn) {
        AnimInterp2 interp = new AnimInterp2();
        Analyzer a = new Analyzer<>(interp);
        try {
            a.analyze(owner,mn);
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }
    }


}
