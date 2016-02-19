package com.dank.util;

import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DCONST_1;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.FCONST_1;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.LCONST_1;
import static org.objectweb.asm.Opcodes.LDC;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class ASMUtil {

    public static Number extractNum(AbstractInsnNode ain) {
        final int op = ain.opcode();
        if(op == LDC) return (Number) ((LdcInsnNode)ain).cst;
        if(op >= 2 && op <= 8) return op - 3; //ICONSTS
        switch (op) {
            case LCONST_0: return 0L;
            case LCONST_1: return 1L;
            case FCONST_0: return 0F;
            case FCONST_1: return 1F;
            case DCONST_0: return 0D;
            case DCONST_1: return 1D;
        }
        if(ain instanceof IntInsnNode) {
            return ((IntInsnNode)ain).operand;
        }
        return null;
    }

    public static String fieldKey(String owner, String name, String desc) {
        return owner + "#" + name + "@" + desc;
    }

    public static String fieldKey(FieldInsnNode fin) {
        return fieldKey(fin.owner,fin.name,fin.desc);
    }
    
    public static String getMethodDescriptor(Class<?> ret, Class<?>... args) {
        Type ret0 = Type.getType(ret);
        Type[] args0 = new Type[args.length];
        for(int i = 0; i < args.length; i++) {
            args0[i] = Type.getType(args[i]);
        }
        return Type.getMethodDescriptor(ret0,args0);
    }

}
