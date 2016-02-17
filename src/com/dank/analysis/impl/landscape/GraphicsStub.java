package com.dank.analysis.impl.landscape;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 15:48
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class GraphicsStub extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName(Hook.ENTITY.getInternalName()) && cn.fieldCount(int.class) > 7 && cn.fieldCount() < 11
                && cn.fieldCount(boolean.class) > 0 ? new ClassSpec(Hook.GRAPHICS_STUB, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (fn.desc.equals("Z") && !fn.isStatic()) {
                Hook.GRAPHICS_STUB.put(new RSField(fn, "finished"));
            }
        }
        final MethodNode mn = cn.getMethodByName("<init>");
        if (mn != null) {
            final int[] indexes = {1, 2, 3, 4, 5, 6};
            final String[] hooks = {"id", "floorLevel", "regionX", "regionY", "height", "startCycle"};
            for (int i = 0; i < indexes.length; i++) {
                final FieldInsnNode fin = load(mn, Opcodes.ILOAD, indexes[i], Hook.GRAPHICS_STUB);
                if (fin != null) {
                    Hook.GRAPHICS_STUB.put(new RSField(fin, hooks[i]));
                }
            }
        }
    }

    private FieldInsnNode load(final MethodNode mn, final int opcode, final int index, final Hook owner) {
        for (final AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof VarInsnNode) {
                final VarInsnNode vin = (VarInsnNode) ain;
                if (vin.var == index && vin.opcode() == opcode) {
                    AbstractInsnNode dog = vin;
                    for (int i = 0; i < 7; i++) {
                        if (dog == null) break;
                        if (dog.opcode() == Opcodes.PUTFIELD && ((FieldInsnNode) dog).owner.equals(owner.getInternalName())) {
                            return (FieldInsnNode) dog;
                        }
                        dog = dog.next();
                    }
                }
            }
        }
        return null;
    }
}
