package com.dank.analysis.impl.character;

import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMember;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.List;



/*

   Alright....



   test number one? ^^


 */
public class Inter extends BasicInterpreter {

    public static MethodNode runAnimationMethod;

    private boolean isGetUShort(MethodInsnNode min) {
        RSMember m = Hook.BUFFER.get("readUShort");
        return m.equals(min);
    }

    private boolean isCharacterTypeClass(String name) {
        //gg cpu
        return Hook.PLAYER.getInternalName().equals(name);
    }

    private void setUShort(int data_index, FieldInsnNode target) {
//        System.out.println("WIN?: " + data_index + " ==> " + target.owner + "." + target.name + target.desc );
        if(data_index == 0 && target.owner.equals(Hook.PLAYER.getInternalName())) {

        }
        if(data_index == 1 && target.owner.equals(Hook.PLAYER.getInternalName())) {

        }
        if(data_index == 2 && target.owner.equals(Hook.PLAYER.getInternalName())) {

        }
        if(data_index == 3 && target.owner.equals(Hook.PLAYER.getInternalName())) {

        }
        if(data_index == 4 && target.owner.equals(Hook.PLAYER.getInternalName())) {

        }
        if(data_index == 5 && target.owner.equals(Hook.PLAYER.getInternalName())) {

        }
        if(data_index == 6 && target.owner.equals(Hook.PLAYER.getInternalName())) {
            target.owner = Hook.CHARACTER.getInternalName();
            Hook.CHARACTER.put(new RSField(target, "runAnimation"));
//            runAnimationMethod = target.method();
        }
        if(data_index == 7 && target.owner.equals(Hook.PLAYER.getInternalName())) {

        }
    }


    private final Buffer buff = new Buffer();

    @Override
    public BasicValue copyOperation(AbstractInsnNode insn, BasicValue value)
                    throws AnalyzerException {
        if(insn.opcode() == ALOAD) {
            VarInsnNode vin = (VarInsnNode) insn;
            if(vin.var == 1) return buff; //The first argument
        }
        return super.copyOperation(insn, value);
    }

// so the first USHort read from the buffer == c.ap... the second is c.aa...
    // do you have class C java? no
    @Override
    public BasicValue newOperation(AbstractInsnNode var1)
            throws AnalyzerException {
        if(var1.opcode() == LDC) {
            Object var2 = ((LdcInsnNode)var1).cst;
            if(var2 instanceof Integer) {
                return new IntLdc();
            }
        }
        return super.newOperation(var1);
    }

    @Override
    public BasicValue binaryOperation(AbstractInsnNode var1, BasicValue var2, BasicValue var3)
            throws AnalyzerException {

        if(var1.opcode() == IMUL) {
            if(var2 instanceof IntLdc && var3 instanceof UShort) { // ldc * (buffer.getUShort())
                return new EncodedUShort((UShort)var3);
            } else if(var2 instanceof UShort && var3 instanceof IntLdc) { // (buffer.getUShort()) * ldc
                return new EncodedUShort((UShort)var2);
            }
        } else if(var1.opcode() == PUTFIELD) {
            // var2 is the reference in the case
            // var3 is the value on from the stack in which will be put into the field
            if(var3 instanceof EncodedUShort) { // ref.field = EncodedUShort = (ldc * (buff.getUShort()))

                FieldInsnNode fin = (FieldInsnNode) var1;
                if(isCharacterTypeClass(fin.owner)) {

                    EncodedUShort eus = (EncodedUShort) var3;
                    setUShort(eus.val.idx,fin);

                }

            }

        }

        return super.binaryOperation(var1, var2, var3);
    }


    @Override
    public BasicValue naryOperation(AbstractInsnNode var1,
                                    List<? extends BasicValue> var2)
            throws AnalyzerException {
        if(var1.opcode() == INVOKEVIRTUAL) {
            BasicValue ref = var2.get(0);
            MethodInsnNode min = (MethodInsnNode) var1;
            if(ref instanceof Buffer && isGetUShort(min)) {
                Buffer buff = (Buffer) ref;
                int idx = buff.readShort();
                return new UShort(idx);
            }
        }

        return super.naryOperation(var1,var2);
    }






    static final class Buffer extends BasicValue {

        int index;

        public Buffer() {
            super(Type.getObjectType("java/lang/Object"));
        }

        public int readShort() {
            return index++;
        }

    }

    static final class UShort extends BasicValue {

        final int idx;

        public UShort(int idx) {
            super(Type.INT_TYPE);
            this.idx = idx;
        }

    }//so do you know which fields are below?  um... sec.. it keeps the order of the read... so no exactly right now thats the easy pa

    static final class IntLdc extends BasicValue {
        public IntLdc() {
            super(Type.INT_TYPE);
        }
    }

    static final class EncodedUShort extends BasicValue {

        final UShort val;

        public EncodedUShort(UShort val) {
            super(Type.INT_TYPE);
            this.val = val;
        }

    }


}
