package com.dank.analysis.impl.character.visitor;

import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.List;

/**
 * Created by Jamie on 11/6/2015.
 */
public class Orientation2 {

    public static void run(MethodNode target) {
        RSMethod add_temp_entity = Hook.LANDSCAPE.getMethod("addTempEntity");
        String player_clazz = Hook.PLAYER.getInternalName();
        String character_clazz = Hook.CHARACTER.getInternalName();
        Analyzer<BasicValue> a = new Analyzer<>(new Interpret(add_temp_entity,player_clazz,character_clazz));
        try {
            a.analyze(target.owner.name,target);
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }

    }

    private static class Interpret extends BasicInterpreter {

        final RSMethod add_temp;
        final String character_clazz;
        final String player_clazz;

        public Interpret(RSMethod add_temp, String player_clazz, String character_clazz) {
            this.add_temp = add_temp;
            this.character_clazz = character_clazz;
            this.player_clazz = player_clazz;
        }

        @Override
        public BasicValue naryOperation(final AbstractInsnNode insn, final List<? extends BasicValue> values)
                throws AnalyzerException {
            if(insn.opcode() == INVOKEVIRTUAL) {
                MethodInsnNode min = (MethodInsnNode) insn;
                if(add_temp.equals(min.owner,min.name,min.desc)) {
                 //   System.out.println("HIT:" + values);
                    try {
                        List<? extends BasicValue> args = values;
                        DecodedValue height     = (DecodedValue) args.get(4);
                        DecodedValue orintation = (DecodedValue) args.get(7);
//                        System.out.println(values);
                        if(height.field.fin.owner.equals(player_clazz)) {
                            Hook.CHARACTER.put(new RSField(character_clazz + "" + orintation.field.fin.name,"I","orientation"));
                            Hook.PLAYER.put(new RSField(height.field.fin,"height"));
                        }
                    } catch (ClassCastException ignored) {}

                }
            }
            return super.naryOperation(insn, values);
        }

        @Override
        public BasicValue binaryOperation(final AbstractInsnNode insn, final BasicValue value1, final BasicValue value2)
                throws AnalyzerException {
            switch (insn.opcode()) {
                case IMUL: {
                    if(value1 instanceof FieldValue && value2 == BasicValue.INT_VALUE) {
                        return new DecodedValue((FieldValue) value1);
                    } else if(value1 == BasicValue.INT_VALUE && value2 instanceof FieldValue) {
                        return new DecodedValue((FieldValue) value2);
                    }
                }
            }
            return super.binaryOperation(insn, value1, value2);
        }

        @Override
        public BasicValue unaryOperation(final AbstractInsnNode insn, final BasicValue value) throws AnalyzerException {
            switch (insn.opcode()) {
                case GETFIELD: {
                    return new FieldValue((FieldInsnNode) insn);
                }
            }
            return super.unaryOperation(insn, value);
        }
    }

    private static class DecodedValue extends BasicValue {
        FieldValue field;
        public DecodedValue(FieldValue field) {
            super(field.getType());
            this.field = field;
        }
        @Override
        public String toString() {
            return "[DECODED]" + field.fin.owner + "" + field.fin.name;
        }
    }

    private static class FieldValue extends BasicValue {
        FieldInsnNode fin;

        public FieldValue(FieldInsnNode fin) {
            super(Type.getType(fin.desc));
            this.fin = fin;
            ;
        }

        @Override
        public String toString() {
            return "[RAW] " + fin.owner + "" + fin.name;
        }

    }
}
