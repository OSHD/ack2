package com.dank.analysis.visitor.stuff;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.List;

/**
 * RNFT = Raw Numeric Field Type
 *
 * Once a RNFT is loaded onto the stack, what it does with it is the question.
 * We must acknoledge every operation
 *
 * Properties:
 * Encoded: ( The value provided to these operations must be in a encoded state, if not ruled out )
 *
 *      - PUTFILED, PUTSTATIC                 : The value set must be encoded
 *          field = field
 *      - IMUL, IADD, ISUB, FMUL, IADD, ISUB  : The value provided must be encoded
 *          field + (...), field - (...), field * (...)
 *
 *
 * Natural Properties: Operations performed with a RNFT that prove its natural
 *
 * - Type conversions of RNFT: Impossible with a encoded type
 *      - IINC, L2I, I2B, I2C, I2S, I2L, I2D, L2D
 *
 * - Logical: Impossible logic with a encoded type
 *      - IREM, ISHL, ISHR, IUSHR, IAND, IOR, IXOR
 *      - LREM, LSHL, LSHR, LUSHR, LAND, LOR, LXOR
 *
 * - Arithmetic: Division is not possible with encoded types
 *      - IDIV, LDIV
 *
 * - Control flow with RNFT: Impossible to encode the underlying 0 compare
 *      - IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE
 *
 * - Specifications:
 *      - NEWARRAY, ANEWARRAY  : Array size
 *      - LALOAD, DALOAD, FALOAD, IALOAD, BALOAD, CALOAD, SALOAD, AALOAD: Array index
 *      - LASTORE, DASTORE, FASTORE, IASTORE, BASTORE, CASTORE, SASTORE, AASTORE:
 *          - If the index if a RNFT then that field is natural
 *          - If the element store is a RNFT then that field is natural: Since elements in arrays are not encoded [Observed]
 *
 * - Observed:
 *      - ISTORE, LSTORE   : Locals are natural
 *      - IRETURN, LRETURN : Returned values are natural
 *      - IF_ICMPEQ , ... , LCMP : The compared value is not encoded ; if(field == value)
 *      - INVOKE, ... ,: If any argument is a RNFT then its natural: since arguments are natural (since locls are not encoded)
 * AnyState:
 *  INEG, LNEG
 *
 *
 * @author Brainfree
 */
public class CodecInterpreter2 extends BasicInterpreter {


    @Override
    public BasicValue unaryOperation(final AbstractInsnNode insn,
                                     final BasicValue value) throws AnalyzerException {
        switch (insn.opcode()) {

            case GETFIELD: {
                FieldInsnNode fin0 = (FieldInsnNode) insn;
                Type type = Type.getType(fin0.desc);
                switch (type.getSort()) {
                    case Type.INT:
                    case Type.LONG:
                        return new NumericField(type, fin0);
                }
                break;
            }

            /////////////////////////////////////////////////////////

                //Converting types
            case IINC:
            case I2L:
            case I2F:
            case I2D:
            case L2I:
            case L2F:
            case L2D:
            case I2B:
            case I2C:
            case I2S:
                // 0 value comparison
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
                // returns
            case IRETURN:
            case LRETURN:
                // array construction argument
            case NEWARRAY:
            case ANEWARRAY: {
                check(value);
            }

        }

        return super.unaryOperation(insn, value);

    }

    @Override
    public BasicValue newOperation(final AbstractInsnNode insn)
            throws AnalyzerException {
        switch (insn.opcode()) {

            case LDC: {
                LdcInsnNode ldc = ((LdcInsnNode) insn);
                Object cst = ldc.cst;
                if (cst instanceof Integer) {
                    return new NumericLdc(Type.INT_TYPE, ldc);
                } else if (cst instanceof Long) {
                    return new NumericLdc(Type.LONG_TYPE, ldc);
                }
                break;
            }

            case GETSTATIC: {
                FieldInsnNode fin = (FieldInsnNode) insn;
                Type type = Type.getType(fin.desc);
                switch (type.getSort()) {
                    case Type.INT:
                    case Type.LONG:
                        return new NumericField(type,fin);
                }
                break;
            }

            /////////////////////////////////////////////////////////

        }

        return super.newOperation(insn);

    }

    @Override
    public BasicValue binaryOperation(final AbstractInsnNode insn,
                                      final BasicValue value1, final BasicValue value2) throws AnalyzerException {

        switch (insn.opcode()) {
            case IMUL:
            case LMUL: {
                if (value1 instanceof NumericField && value2 instanceof NumericLdc) { // FIELD x LDC
                    chkTypes(insn,value1,value2);
                    return new DecodedField(value1.getType(), (NumericField) value1, (NumericLdc) value2);
                } else if(value1 instanceof NumericLdc && value2 instanceof NumericField) { // LDC x FIELD
                    chkTypes(insn,value1,value2);
                    return new DecodedField(value1.getType(), (NumericField) value2, (NumericLdc) value1);
                }
                break;
            }

            /////////////////////////////////////////////////////////

                //array index value must be decoded
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
                // division (both stack values must be decoded)
            case IDIV:
            case LDIV:
                // logical (both stack values must be decoded)
            case IREM:
            case LREM:
            case FREM:
            case DREM:
            case ISHL:
            case LSHL:
            case ISHR:
            case LSHR:
            case IUSHR:
            case LUSHR:
            case IAND:
            case LAND:
            case IOR:
            case LOR:
            case IXOR:
            case LXOR:
                // comparisons (both stack values must be decoded)
            case LCMP:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE: {
                //We'll check both even though -in some cases- one stack
                // value is not numeric...
                check(value1);
                check(value2);
            }

        }

        return super.binaryOperation(insn, value1, value2);

    }


    @Override
    public BasicValue ternaryOperation(AbstractInsnNode insn, BasicValue value1,
                                       BasicValue value2, BasicValue value3) throws AnalyzerException {

        switch (insn.opcode()) {
            // The index and the value stored in arrays must be decoded
            case IASTORE:
            case LASTORE:
            case FASTORE:
            case DASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE:
                check(value2);
                check(value3);
        }

        return super.ternaryOperation(insn, value1, value2, value3);

    }

    @Override
    public BasicValue naryOperation(AbstractInsnNode insn,
                                    List<? extends BasicValue> values)
            throws AnalyzerException {

        switch(insn.opcode()) {
                // The dimensions parameters must be decoded
            case MULTIANEWARRAY:
                // All parameters passed to methods must be decoded
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEINTERFACE: {
                for(BasicValue v : values) {
                    check(v);
                }
            }
        }

        return super.naryOperation(insn, values);

    }

    @Override
    public BasicValue copyOperation(AbstractInsnNode insn, BasicValue value)
            throws AnalyzerException {

        switch (insn.opcode()) {
            // Local variables must be decoded
            case ISTORE:
            case LSTORE:
                check(value);
        }

        return super.copyOperation(insn, value);
    }


    private void check(BasicValue v) {
        if(v instanceof DecodedField) {
            DecodedField f = (DecodedField) v;
            guaranteedDecoded(f.field.fin,(Number)f.decoder.ldc.cst);
        }
    }

    protected void guaranteedDecoded(FieldInsnNode field, Number decoder) {
    }

    private static void chkTypes(AbstractInsnNode insn, BasicValue A, BasicValue B) throws AnalyzerException {
        if(A.getType() != B.getType())
            throw new AnalyzerException(insn, A.getType() + " x " + B.getType());
    }


//////////////////////////////////////////////////////////////////////////////////////////////

    private class NumericField extends BasicValue {

        FieldInsnNode fin;

        public NumericField(Type type, FieldInsnNode fin) {
            super(type);
            this.fin = fin;
        }

        @Override
        public String toString() {
            return fin.toString();
        }

    }

    private class NumericLdc extends BasicValue {

        final LdcInsnNode ldc;

        public NumericLdc(Type type, LdcInsnNode ldc) {
            super(type);
            this.ldc = ldc;
        }

        @Override
        public String toString() {
            return ldc.cst.toString();
        }

    }

    private class DecodedField extends BasicValue {

        NumericField field;
        NumericLdc decoder;

        public DecodedField(Type t, NumericField field, NumericLdc decoder) {
            super(t);
            this.field   = field;
            this.decoder = decoder;
        }

        @Override
        public String toString() {
            return field + " x " + decoder;
        }

    }

}
