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
import com.dank.hook.RSMethod;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 15:50
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class DynamicObject extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName(Hook.ENTITY.getInternalName()) && cn.fieldCount(int.class) > 7 && cn.fieldCount() < 11
                && cn.fieldCount(boolean.class) == 0 ? new ClassSpec(Hook.DYNAMIC_OBJECT, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
    	RSMethod rotatedModel = (RSMethod)Hook.ENTITY.get("getRotatedModel");//Inherited method from Entity
    	if(rotatedModel!=null){
    		MethodData sub = DynaFlowAnalyzer.getMethod(cn.name, rotatedModel.name, rotatedModel.desc);
    		if(sub!=null)
    			Hook.DYNAMIC_OBJECT.put(new RSMethod(sub.bytecodeMethod, "getRotatedModel"));
    	}
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
        	if(fn.desc.equals("L"+Hook.ANIMATION_SEQUENCE.getInternalName()+";")){
	            Hook.DYNAMIC_OBJECT.put(new RSField(fn, "animationSequence"));
        	}
    	}
        final MethodNode mn = cn.getMethodByName("<init>");
        if (mn != null) {
            final int[] indexes = {1, 2, 3, 4, 5, 6};
            final String[] hooks = {"id", "type", "orientation", "floorLevel", "regionX", "regionY"};
            for (int i = 0; i < indexes.length; i++) {
                final FieldInsnNode fin = load(mn, Opcodes.ILOAD, indexes[i], Hook.DYNAMIC_OBJECT);
                if (fin != null) {
                    Hook.DYNAMIC_OBJECT.put(new RSField(fin, hooks[i]));
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
