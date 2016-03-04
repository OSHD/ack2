package com.dank.analysis.interpret;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import com.dank.util.ASMUtil;


public abstract class NumericConstantBranch extends BasicInterpreter {

    public abstract void visitBranch(Number value, FieldInsnNode compare, int compare_type, LabelNode target);


    private class NumericProducer extends BasicValue {
        AbstractInsnNode value;
        public NumericProducer(Type type, AbstractInsnNode num) {
            super(type);
            this.value = num;
        }
    }

    private class NumericField extends NumericProducer {
        public NumericField(Type type, FieldInsnNode fin) {
            super(type,fin);
        }
    }

    // A numeric field * number
    private class CodecValue extends NumericProducer {
        ConstantNumber num;
        NumericField field;
        public CodecValue(Type type, ConstantNumber num, NumericField field) {
            super(type, null);
            this.num = num;
            this.field = field;
        }
    }

    private class ConstantNumber extends BasicValue {
        Number num;
        public ConstantNumber(Type type, AbstractInsnNode num) {
            super(type);
            this.num = ASMUtil.extractNum(num);
        }
    }

    @Override
    public BasicValue newOperation(final AbstractInsnNode insn) throws AnalyzerException {
        switch (insn.opcode()) {
            case ACONST_NULL:
                return newValue(Type.getObjectType("null"));
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                return new ConstantNumber(Type.INT_TYPE,insn);
            case LCONST_0:
            case LCONST_1:
                return new ConstantNumber(Type.LONG_TYPE,insn);
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
                return new ConstantNumber(Type.FLOAT_TYPE,insn);
            case DCONST_0:
            case DCONST_1:
                return new ConstantNumber(Type.DOUBLE_TYPE,insn);
            case BIPUSH:
            case SIPUSH:
                return new ConstantNumber(Type.INT_TYPE,insn);
            case GETSTATIC:
                FieldInsnNode fin = (FieldInsnNode) insn;
                Type t = Type.getType((fin).desc);
                switch (t.getSort()) {
                    case Type.INT:    return new NumericField(Type.INT_TYPE,fin);
                    case Type.LONG:   return new NumericField(Type.LONG_TYPE,fin);
                    case Type.FLOAT:  return new NumericField(Type.FLOAT_TYPE,fin);
                    case Type.DOUBLE: return new NumericField(Type.DOUBLE_TYPE,fin);
                    case Type.BYTE:   return new NumericField(Type.BYTE_TYPE,fin);
                    case Type.SHORT:  return new NumericField(Type.SHORT_TYPE,fin);
                }
                return newValue(t);
            case LDC:
                Object cst = ((LdcInsnNode) insn).cst;
                if (cst instanceof Integer) {
                    return new ConstantNumber(Type.INT_TYPE,insn);
                } else if (cst instanceof Float) {
                    return new ConstantNumber(Type.FLOAT_TYPE,insn);
                } else if (cst instanceof Long) {
                    return new ConstantNumber(Type.LONG_TYPE,insn);
                } else if (cst instanceof Double) {
                    return new ConstantNumber(Type.DOUBLE_TYPE,insn);
                }
        }
        return super.newOperation(insn);
    }

    @Override
    public BasicValue binaryOperation(final AbstractInsnNode insn, final BasicValue value1, final BasicValue value2) throws AnalyzerException {
        switch (insn.opcode()) {
            case IMUL: {
                if(value1 instanceof NumericField && value2 instanceof ConstantNumber) {
                    return new CodecValue(Type.INT_TYPE,(ConstantNumber)value2,(NumericField)value1);
                } else if(value1 instanceof ConstantNumber && value2 instanceof NumericField) {
                    return new CodecValue(Type.INT_TYPE,(ConstantNumber)value1,(NumericField)value2);
                }
                break;
            }
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
                if(value1 instanceof ConstantNumber && value2 instanceof CodecValue) {
                    JumpInsnNode jin = (JumpInsnNode) insn;
                    ConstantNumber num = (ConstantNumber) value1;
                    CodecValue cmp = (CodecValue) value2;
                    visitBranch(num.num,(FieldInsnNode)cmp.field.value,jin.opcode(),jin.label);
                } else if(value2 instanceof ConstantNumber && value1 instanceof CodecValue) {
                    JumpInsnNode jin = (JumpInsnNode) insn;
                    ConstantNumber num = (ConstantNumber) value2;
                    CodecValue cmp = (CodecValue) value1;
                    visitBranch(num.num,(FieldInsnNode)cmp.field.value,jin.opcode(),jin.label);
                }
        }
        return super.binaryOperation(insn, value1, value2);
    }

    @Override
    public BasicValue unaryOperation(final AbstractInsnNode insn, final BasicValue value) throws AnalyzerException {
        switch (insn.opcode()) {
            case GETFIELD:
                FieldInsnNode fin = (FieldInsnNode) insn;
                Type t = Type.getType((fin).desc);
                switch (t.getSort()) {
                    case Type.INT:    return new NumericField(Type.INT_TYPE,fin);
                    case Type.LONG:   return new NumericField(Type.LONG_TYPE,fin);
                    case Type.FLOAT:  return new NumericField(Type.FLOAT_TYPE,fin);
                    case Type.DOUBLE: return new NumericField(Type.DOUBLE_TYPE,fin);
                    case Type.BYTE:   return new NumericField(Type.BYTE_TYPE,fin);
                    case Type.SHORT:  return new NumericField(Type.SHORT_TYPE,fin);
                }
                return newValue(t);
        }
        return super.unaryOperation(insn,value);
    }


}
