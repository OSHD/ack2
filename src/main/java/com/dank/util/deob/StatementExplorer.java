package com.dank.util.deob;

import static jdk.internal.org.objectweb.asm.Opcodes.ATHROW;
import static jdk.internal.org.objectweb.asm.Opcodes.CHECKCAST;
import static jdk.internal.org.objectweb.asm.Opcodes.DUP;
import static jdk.internal.org.objectweb.asm.Opcodes.DUP2;
import static jdk.internal.org.objectweb.asm.Opcodes.DUP2_X1;
import static jdk.internal.org.objectweb.asm.Opcodes.DUP2_X2;
import static jdk.internal.org.objectweb.asm.Opcodes.DUP_X1;
import static jdk.internal.org.objectweb.asm.Opcodes.DUP_X2;
import static jdk.internal.org.objectweb.asm.Opcodes.GETFIELD;
import static jdk.internal.org.objectweb.asm.Opcodes.GETSTATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.GOTO;
import static jdk.internal.org.objectweb.asm.Opcodes.IFNONNULL;
import static jdk.internal.org.objectweb.asm.Opcodes.IFNULL;
import static jdk.internal.org.objectweb.asm.Opcodes.IINC;
import static jdk.internal.org.objectweb.asm.Opcodes.INSTANCEOF;
import static jdk.internal.org.objectweb.asm.Opcodes.INVOKESTATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.JSR;
import static jdk.internal.org.objectweb.asm.Opcodes.LOOKUPSWITCH;
import static jdk.internal.org.objectweb.asm.Opcodes.MONITORENTER;
import static jdk.internal.org.objectweb.asm.Opcodes.MONITOREXIT;
import static jdk.internal.org.objectweb.asm.Opcodes.NEW;
import static jdk.internal.org.objectweb.asm.Opcodes.POP;
import static jdk.internal.org.objectweb.asm.Opcodes.POP2;
import static jdk.internal.org.objectweb.asm.Opcodes.PUTFIELD;
import static jdk.internal.org.objectweb.asm.Opcodes.PUTSTATIC;
import static jdk.internal.org.objectweb.asm.Opcodes.RET;
import static jdk.internal.org.objectweb.asm.Opcodes.RETURN;
import static jdk.internal.org.objectweb.asm.Opcodes.SWAP;
import static jdk.internal.org.objectweb.asm.Opcodes.TABLESWITCH;

import java.util.Arrays;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

import com.dank.asm.RIS;

/**
 * @author Brainfree
 */
public class StatementExplorer {

    private static final int[] DELTAS;

    static {

        DELTAS = new int[256];
        Arrays.fill(DELTAS, Integer.MIN_VALUE); //Default sums

        Arrays.fill( DELTAS,  0, 19,  0+1 ); // Int Instruction Nodes + LDC, [-> constant] PUSH 1
        Arrays.fill( DELTAS, 21, 26,  0+1 ); // Var Instruction Nodes, [-> value] PUSH 1
        Arrays.fill( DELTAS, 46, 54, -2+1 ); // Array Load [arrayref,index -> value] POP2 -> PUSH1
        Arrays.fill( DELTAS, 54, 59, -1+0 );
        Arrays.fill( DELTAS, 79, 86, -3+0 );
        DELTAS[POP]          = -1+0;
        DELTAS[POP2]         = -2+0;
        DELTAS[DUP]          = -1+2;
        DELTAS[DUP_X1]       = -2+3;
        DELTAS[DUP_X2]       = -3+4;
        DELTAS[DUP2]         = -2+4;
        DELTAS[DUP2_X1]      = -3+5;
        DELTAS[DUP2_X2]      = -4+6;
        DELTAS[SWAP]         = -2+2;
        Arrays.fill( DELTAS, 96,116, -2+1   );
        Arrays.fill( DELTAS, 116, 120, -1+1 );
        Arrays.fill( DELTAS, 120,132, -2+1  );
        DELTAS[IINC]         = 0+0;
        Arrays.fill( DELTAS, 133,148, -1+1  );
        Arrays.fill( DELTAS, 148,153, -2+1  );
        Arrays.fill( DELTAS, 153,159, -1+0  );
        Arrays.fill( DELTAS, 159,167, -2+0  );
        DELTAS[GOTO]         = 0+0;
        DELTAS[JSR]          = 0+1;
        DELTAS[RET]          = 0+0;
        DELTAS[TABLESWITCH]  = -1+0;
        DELTAS[LOOKUPSWITCH] = -1+0;
        Arrays.fill(DELTAS, 172,177,-1+0 ); //Empties the stack
        DELTAS[RETURN]       = 0+0; //Drains the stack
        DELTAS[GETSTATIC]    = 0+1;
        DELTAS[PUTSTATIC]    = -1+0;
        DELTAS[GETFIELD]     = -1+1;
        DELTAS[PUTFIELD]     = -2+0;
        DELTAS[NEW]          = 0+1;
        Arrays.fill( DELTAS, 188,191,-1+1 );
        DELTAS[ATHROW]       = -1+1;
        DELTAS[CHECKCAST]    = -1+1;
        DELTAS[INSTANCEOF]   = -1+1;
        DELTAS[MONITORENTER] = -1+0;
        DELTAS[MONITOREXIT]  = -1+0;
        DELTAS[IFNULL]       = -1+0;
        DELTAS[IFNONNULL]    = -1+0;

    }

    private static int getDelta(int op) {
        final int delta = DELTAS[op];
        if(delta == Integer.MIN_VALUE) throw new Error("Unknown Delta Value: " + RIS.OPCODE_NAME_MAP.get(op) + "<" +op + ">");
        return delta;
    }

    public static interface Observer {
        void visitStarted(AbstractInsnNode initial);
        void visit(AbstractInsnNode cur, int pos, int stackSize);
        void visitEnded(AbstractInsnNode last,int remaining);
    }

    private static int getMethodSize(MethodInsnNode min) {
        Type[] args = Type.getArgumentTypes(min.desc);
        int length = args.length;
        if(!min.desc.endsWith("V")) length--; //Pushes back
        if(min.opcode() != INVOKESTATIC) length++; //Pulls reference
        return length;
    }

    public static void explore(AbstractInsnNode[] stack, int pos, int posSize, Observer observer) {
        observer.visitStarted(stack[pos]);
        int stack_size = posSize;

        for(;pos >= 0;pos--) {

            AbstractInsnNode cur = stack[pos];

            if(cur instanceof LabelNode || cur instanceof JumpInsnNode) break;

            if(cur instanceof MethodInsnNode) {
                stack_size -= getMethodSize((MethodInsnNode) cur);
            } else {
                stack_size -= getDelta(cur.opcode());
            }

            observer.visit(cur,pos,stack_size);

            if(stack_size == 0) break;

        }

        observer.visitEnded(stack[pos],stack_size);

    }

}
