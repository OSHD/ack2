package com.dank.analysis.impl.character;


import com.dank.analysis.interpret.FieldInterpreter;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;

/**
 * Created by Greg on 12/22/2015.
 */
public class AnimationInterpreter extends FieldInterpreter {

    private String decoder;
    private int iterValue = 0;

    public AnimationInterpreter(String decoder) {
        super(decoder);
        this.decoder = decoder;
    }

    String[] names = new String[]{"runAnimation", "animFrameId"};

    @Override
    public BasicValue unaryOperation(final AbstractInsnNode insn, final BasicValue value) throws AnalyzerException {
        if (insn.opcode() == Opcodes.GETFIELD) {

            // if... its... walkAnimation the fields it getting... TRACK it
            FieldInsnNode fin = (FieldInsnNode) insn;
//            System.out.println(fin.owner + "." + fin.name);

            if (fin.owner.equals(decoder)) {
                return new FieldRef((FieldInsnNode) insn);
            }
        }
        return super.unaryOperation(insn, value);
    }

    @Override
    public BasicValue binaryOperation(final AbstractInsnNode insn, final BasicValue value1, final BasicValue value2)
            throws AnalyzerException {

        if (insn.opcode() == Opcodes.PUTFIELD) { // REF VALUE4
            if (value2 instanceof DeocdedField) { // This operation is using a interesting value...

                FieldRef value = ((DeocdedField) value2).theField;

                FieldInsnNode the_getter = value.theField;
                FieldInsnNode put_field = (FieldInsnNode) insn; // Where its being put to

//                System.out.println("putting " + the_getter.owner + "." + the_getter.name + " into " + put_field.owner + "." + put_field.name + " - " +iterValue );

                if (iterValue == 1) {  // Character.?? = NPCDEF.?? * LDC
//                    System.out.println(the_getter.owner + "." + the_getter.name + " -> " + names[iterValue - 2]);
                    the_getter.owner = the_getter.owner.replaceAll("[a-z]+", Hook.CHARACTER.getInternalName());
                    Hook.CHARACTER.put(new RSField(names[0], the_getter));

//                    System.out.println(put_field.owner + "." + put_field.name + " -> " + names[iterValue - 2]);
                    put_field.owner = put_field.owner.replaceAll("[a-z]+", Hook.CHARACTER.getInternalName());
                    Hook.CHARACTER.put(new RSField(names[1], put_field));

                }
                iterValue++;
            }
        } else if (insn.opcode() == Opcodes.IMUL) {
            // 'Decoding' a numeric field
            if (value1 instanceof NumericConstant && value2 instanceof FieldRef) { // LDC * REF.FIELD
                return new DeocdedField((FieldRef) value2);
            } else if (value2 instanceof NumericConstant && value1 instanceof FieldRef) { // REF.FIELD * LDC
                return new DeocdedField((FieldRef) value1);
            }

        }
        return super.binaryOperation(insn, value1, value2);
    }

    @Override
    public BasicValue newOperation(final AbstractInsnNode insn) throws AnalyzerException {
        if (insn.opcode() == LDC) {
            Object cst = ((LdcInsnNode) insn).cst;
            if (cst instanceof Integer) {
                return new NumericConstant(Type.INT_TYPE);
            } else if (cst instanceof Long) {
                return new NumericConstant(Type.LONG_TYPE);
            }
        }
        return super.newOperation(insn);
    }


    class NumericConstant extends BasicValue {

        public NumericConstant(Type type) {
            super(type);
        }
    }

    class FieldRef extends BasicValue {

        FieldInsnNode theField;

        public FieldRef(FieldInsnNode fin) {
            super(Type.getType(fin.desc));
            this.theField = fin;
        }
    }

    class DeocdedField extends BasicValue {

        FieldRef theField;

        public DeocdedField(FieldRef fieldValue) {
            super(fieldValue.getType());
            this.theField = fieldValue;
        }
    }
}
