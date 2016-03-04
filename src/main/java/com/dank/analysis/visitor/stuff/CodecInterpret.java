package com.dank.analysis.visitor.stuff;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

/**
 * Finds possible encoders and decoders using simple stack analysis
 *
 * @see org.objectweb.asm.tree.analysis.BasicInterpreter
 * @see org.objectweb.asm.tree.analysis.Analyzer
 * @author Brainfre
 */

public class CodecInterpret extends BasicInterpreter {



    // field = (...) * encoder || field = encoder * (...)
    public void visitEncoder(String owner, String name, String desc, AbstractInsnNode encoder) {
        System.out.println(owner + "#" + name + "@" + desc + " Encoder == " + encoder);
    }

    // decoder * field || field * decoder
    public void visitDecoder(String owner, String name, String desc, AbstractInsnNode decoder) {
        System.out.println(owner + "#" + name + "@" + desc + " Decoder == " + decoder);
    }

    @Override
    public BasicValue unaryOperation(final AbstractInsnNode insn,
                                     final BasicValue value) throws AnalyzerException {
        switch (insn.opcode()) {
            case INEG:
            case IINC:
            case L2I:
            case F2I:
            case D2I:
            case I2B:
            case I2C:
            case I2S:
                return BasicValue.INT_VALUE;
            case FNEG:
            case I2F:
            case L2F:
            case D2F:
                return BasicValue.FLOAT_VALUE;
            case LNEG:
            case I2L:
            case F2L:
            case D2L:
                return BasicValue.LONG_VALUE;
            case DNEG:
            case I2D:
            case L2D:
            case F2D:
                return BasicValue.DOUBLE_VALUE;
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case TABLESWITCH:
            case LOOKUPSWITCH:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
                return null;
            case PUTSTATIC:

                FieldInsnNode fin = (FieldInsnNode) insn;
                if(value instanceof EncodedValue) { // field = (...) * C
                    EncodedValue m = (EncodedValue) value;
                    visitEncoder(fin.owner,fin.name,fin.desc,m.getCoder());
                }else if(value instanceof CodecSum) { // field = (...) * C + (...) * C ==> field = (...) * C
                    CodecSum sum = (CodecSum) value;
                    visitEncoder(fin.owner,fin.name,fin.desc,sum.A.getCoder());
                    visitEncoder(fin.owner,fin.name,fin.desc,sum.B.getCoder());
                } else if(value instanceof NumericConstant) { // field = C
                    visitEncoder(fin.owner,fin.name,fin.desc,((NumericConstant)value).get());
                }

                return null;
            case GETFIELD:
                FieldInsnNode fin0 = (FieldInsnNode) insn;
                Type type = Type.getType(fin0.desc);
                switch (type.getSort()) {
                    case Type.INT:
                    case Type.LONG:
                        return new NumericField(type,fin0);
                }
                return newValue(type);
            case NEWARRAY:
                switch (((IntInsnNode) insn).operand) {
                    case T_BOOLEAN:
                        return newValue(Type.getType("[Z"));
                    case T_CHAR:
                        return newValue(Type.getType("[C"));
                    case T_BYTE:
                        return newValue(Type.getType("[B"));
                    case T_SHORT:
                        return newValue(Type.getType("[S"));
                    case T_INT:
                        return newValue(Type.getType("[I"));
                    case T_FLOAT:
                        return newValue(Type.getType("[F"));
                    case T_DOUBLE:
                        return newValue(Type.getType("[D"));
                    case T_LONG:
                        return newValue(Type.getType("[J"));
                    default:
                        throw new AnalyzerException(insn, "Invalid array type");
                }
            case ANEWARRAY:
                String desc = ((TypeInsnNode) insn).desc;
                return newValue(Type.getType("[" + Type.getObjectType(desc)));
            case ARRAYLENGTH:
                return BasicValue.INT_VALUE;
            case ATHROW:
                return null;
            case CHECKCAST:
                desc = ((TypeInsnNode) insn).desc;
                return newValue(Type.getObjectType(desc));
            case INSTANCEOF:
                return BasicValue.INT_VALUE;
            case MONITORENTER:
            case MONITOREXIT:
            case IFNULL:
            case IFNONNULL:
                return null;
            default:
                throw new Error("Internal error.");
        }
    }

    @Override
    public BasicValue newOperation(final AbstractInsnNode insn)
            throws AnalyzerException {
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
                return new Num(Type.INT_TYPE,insn);
            case LCONST_0:
            case LCONST_1:
                return new Num(Type.LONG_TYPE,insn);
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
                return BasicValue.FLOAT_VALUE;
            case DCONST_0:
            case DCONST_1:
                return BasicValue.DOUBLE_VALUE;
            case BIPUSH:
            case SIPUSH:
                return new Num(Type.INT_TYPE,insn);
            case LDC:
                LdcInsnNode ldc = ((LdcInsnNode) insn);
                Object cst = ldc.cst;
                if (cst instanceof Integer) {
                    return new Ldc(Type.INT_TYPE,ldc);
                } else if (cst instanceof Float) {
                    return BasicValue.FLOAT_VALUE;
                } else if (cst instanceof Long) {
                    return new Ldc(Type.LONG_TYPE,ldc);
                } else if (cst instanceof Double) {
                    return BasicValue.DOUBLE_VALUE;
                } else if (cst instanceof String) {
                    return newValue(Type.getObjectType("java/lang/String"));
                } else if (cst instanceof Type) {
                    int sort = ((Type) cst).getSort();
                    if (sort == Type.OBJECT || sort == Type.ARRAY) {
                        return newValue(Type.getObjectType("java/lang/Class"));
                    } else if (sort == Type.METHOD) {
                        return newValue(Type
                                .getObjectType("java/lang/invoke/MethodType"));
                    } else {
                        throw new IllegalArgumentException("Illegal LDC constant "
                                + cst);
                    }
                } else if (cst instanceof Handle) {
                    return newValue(Type
                            .getObjectType("java/lang/invoke/MethodHandle"));
                } else {
                    throw new IllegalArgumentException("Illegal LDC constant "
                            + cst);
                }
            case JSR:
                return BasicValue.RETURNADDRESS_VALUE;
            case GETSTATIC:
                FieldInsnNode fin = (FieldInsnNode) insn;
                Type type = Type.getType(fin.desc);
                switch (type.getSort()) {
                    case Type.INT:
                    case Type.LONG:
                        return new NumericField(type,fin);
                }
                return newValue(type);
            case NEW:
                return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
            default:
                throw new Error("Internal error.");
        }
    }


    @Override
    public BasicValue copyOperation(final AbstractInsnNode insn,
                                    final BasicValue value) throws AnalyzerException {
        return value;
    }


    @Override
    public BasicValue binaryOperation(final AbstractInsnNode insn,
                                      final BasicValue value1, final BasicValue value2)
            throws AnalyzerException {
        switch (insn.opcode()) {
            case IMUL:
                visitDecode(value1, value2);
                boolean a = value1 instanceof NumericConstant;
                boolean b = value2 instanceof NumericConstant;
                if(a && !b) return new EncodedValue(Type.INT_TYPE,(NumericConstant)value1,value2);
                if(!a && b) return new EncodedValue(Type.INT_TYPE,(NumericConstant)value2,value1);
            case IADD:
            case ISUB:
                BasicValue v = visitInc(Type.INT_TYPE,value1, value2);
                if(v!=null) return v;
            case IALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
            case IDIV:
            case IREM:
            case ISHL:
            case ISHR:
            case IUSHR:
            case IAND:
            case IOR:
            case IXOR:
                return BasicValue.INT_VALUE;
            case FMUL:
            case FADD:
            case FSUB:
            case FALOAD:
            case FDIV:
            case FREM:
                return BasicValue.FLOAT_VALUE;
            case LMUL: // value1 * value2
                visitDecode(value1, value2);
                a = value1 instanceof NumericConstant;
                b = value2 instanceof NumericConstant;
                if(a && !b) return new EncodedValue(Type.LONG_TYPE,(NumericConstant)value1,value2);
                if(!a && b) return new EncodedValue(Type.LONG_TYPE,(NumericConstant)value2,value1);
            case LADD:
            case LSUB:
                v = visitInc(Type.LONG_TYPE,value1, value2);
                if(v!=null) return v;
            case LALOAD:
            case LDIV:
            case LREM:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
                return BasicValue.LONG_VALUE;
            case DMUL:
            case DADD:
            case DSUB:
            case DALOAD:
            case DDIV:
            case DREM:
                return BasicValue.DOUBLE_VALUE;
            case AALOAD:
                return BasicValue.REFERENCE_VALUE;
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
                return BasicValue.INT_VALUE;
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
                return null;
            case PUTFIELD:

                FieldInsnNode fin = (FieldInsnNode) insn;
                if(value2 instanceof EncodedValue) { // field = (...) * C
                    EncodedValue m = (EncodedValue) value2;
                    visitEncoder(fin.owner,fin.name,fin.desc,m.getCoder());
                } else if(value2 instanceof CodecSum) {  // field = (..A..) * C + (..B..) * C ==> field = (..A.. + ..B..) * C
                    CodecSum sum = (CodecSum) value2;
                    visitEncoder(fin.owner, fin.name, fin.desc, sum.A.getCoder());
                    visitEncoder(fin.owner,fin.name,fin.desc,sum.B.getCoder());
                } else if(value2 instanceof NumericConstant) { // field = C
                    visitEncoder(fin.owner,fin.name,fin.desc,((NumericConstant)value2).get());
                }

                return null;
            default:
                throw new Error("Internal error.");
        }
    }

    private BasicValue visitInc(Type type, BasicValue value1, BasicValue value2) { //Verified later

        if(value1 instanceof NumericField && value2 instanceof NumericConstant) {

        }
        if(value1 instanceof NumericField && value2 instanceof EncodedValue) {  // (...) * C + field
            FieldInsnNode fin = ((NumericField) value1).fin;
            visitEncoder(fin.owner,fin.name,fin.desc,((EncodedValue)value2).getCoder());
            return new Innc(type, ((EncodedValue)value2).getCoder() , fin,false);
        } else if(value2 instanceof NumericField && value1 instanceof EncodedValue) { // field + C * (...)
            FieldInsnNode fin = ((NumericField) value2).fin;
            return new Innc(type, ((EncodedValue)value1).getCoder() , fin,false);
        } else if(value1 instanceof EncodedValue && value2 instanceof EncodedValue) {
            return new CodecSum(type,(EncodedValue)value1,(EncodedValue)value2);
        }

        return null;

    }

    private void visitDecode(BasicValue value1, BasicValue value2) {
        if(value1 instanceof NumericField && value2 instanceof Ldc) { // field * ldc
            FieldInsnNode fin = ((NumericField)value1).fin;
            visitDecoder(fin.owner, fin.name, fin.desc, ((Ldc) value2).ldc);
        } else if(value1 instanceof Ldc && value2 instanceof NumericField) { // ldc * field
            FieldInsnNode fin = ((NumericField)value2).fin;
            visitDecoder(fin.owner, fin.name, fin.desc, ((Ldc) value1).ldc);
        }
    }


    private class CodecSum extends BasicValue { // A + B ==> (...A...) * C + (...B...) * C
        final EncodedValue A;
        final EncodedValue B;
        CodecSum(Type t, EncodedValue A, EncodedValue B) {
            super(t);
            this.A = A;
            this.B = B;
        }
    }

    // Any field fetch in which is a int or long
    private class NumericField extends BasicValue {

        FieldInsnNode fin;

        public NumericField(Type type, FieldInsnNode fin) {
            super(type);
            this.fin = fin;
        }

        public String toString() {
            return fin.toString();
        }

    }

    // field + (...) * C
    private class Innc extends NumericField { // When a field value is added to a encoded field

        FieldInsnNode field;
        AbstractInsnNode encdoer;
        boolean by_constant;

        public Innc(Type type, AbstractInsnNode encoder, FieldInsnNode src,boolean inc) {
            super(type,src);
            this.field = src;
            this.encdoer = encoder;
            this.by_constant = inc;
        }

        boolean verify(FieldInsnNode target) {
            if(!field.owner.equals(target.owner)
            || !field.name.equals(target.name)
            || !field.desc.equals(target.desc))
                return false;
            return true;
        }

    }

    interface NumericConstant {
        AbstractInsnNode get();
    }

    // CONSTS, BIPUSH, SIPUSH
    private class Num extends BasicValue implements NumericConstant {

        final AbstractInsnNode val;

        public Num(Type type, AbstractInsnNode val) {
            super(type);
            this.val = val;

        }

        @Override
        public AbstractInsnNode get() {
            return val;
        }
    }

    // A numeric ldc
    private class Ldc extends BasicValue implements NumericConstant {
        
        final LdcInsnNode ldc;

        public Ldc(Type type,LdcInsnNode ldc) {
            super(type);
            this.ldc = ldc;
        }

        @Override
        public String toString() {
            return ldc.cst.toString();
        }

        @Override
        public AbstractInsnNode get() {
            return ldc;
        }
    }



    // A * B
    private class EncodedValue extends BasicValue {

        BasicValue target;
        NumericConstant coder;

        public EncodedValue(Type t, NumericConstant coder, BasicValue target) {
            super(t);
            this.coder = coder;
            this.target = target;
        }

        public AbstractInsnNode getCoder() {
            return coder.get();
        }

        @Override
        public String toString() {
            return coder + " * " + target;
        }

    }

}
