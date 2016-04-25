package com.dank.analysis.impl.landscape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.dank.analysis.Analyser;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMember;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;

//All methods identified as of r113
public class Projectile extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(double.class) == 8 && cn.fieldCount(int.class) > 10 && cn.fieldCount(boolean.class) == 1
                ? new ClassSpec(Hook.PROJECTILE, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
		RSMember member = Hook.ENTITY.get("getAnimatedModel");
		RSMethod rotatedModel=null;
		if(member!=null)
			rotatedModel = (RSMethod)member;
        hookStupidShit(cn.getMethodByName("<init>"));
        for (MethodNode mn : cn.methods) {
        	if(mn.isStatic())
        		continue;
    		if(new Wildcard("(?)L"+Hook.MODEL.getInternalName()+";").matches(mn.desc) && rotatedModel!=null && mn.name.equals(rotatedModel.name)) {
    			rotatedModel=new RSMethod(mn, "getAnimatedModel");
    			Hook.PROJECTILE.put(rotatedModel);
    		}
        	if(new Wildcard("(I?)V").matches(mn.desc)){
	            Hook.PROJECTILE.put(new RSMethod(mn, "updateAnimation"));
        	}
        	if(new Wildcard("(IIII?)V").matches(mn.desc)){
	            Hook.PROJECTILE.put(new RSMethod(mn, "updatePosition"));
        		List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(mn,
        				Mask.ALOAD,
						Mask.ILOAD,
						Mask.I2D,
						Mask.GETFIELD.describe("D").distance(2),
						Mask.DSUB,
						Mask.DLOAD,
						Mask.DDIV,
						Mask.PUTFIELD.describe("D")
						);
				if (patterns != null) {
					HashMap<Integer, FieldInsnNode> fins = new HashMap<Integer, FieldInsnNode>();
					for(ArrayList<AbstractInsnNode> pattern : patterns){
						VarInsnNode var = (VarInsnNode) pattern.get(1);
						FieldInsnNode fin = (FieldInsnNode) pattern.get(3);
						fins.put(var.var, fin);
					}
					for(int key : fins.keySet()){
						FieldInsnNode fin = fins.get(key);
						if(key==1)
				            Hook.PROJECTILE.put(new RSField(fin, "currStrictX"));
						if(key==2)
				            Hook.PROJECTILE.put(new RSField(fin, "currStrictY"));
						if(key==3)
				            Hook.PROJECTILE.put(new RSField(fin, "currZ"));
					}
				}
        	}
        }
        for(FieldNode fn : cn.fields){
        	if(fn.isStatic())
        		continue;
        	if(fn.desc.equals("L"+Hook.ANIMATION_SEQUENCE.getInternalName()+";")){
	            Hook.PROJECTILE.put(new RSField(fn, "animationSequence"));
        	}
        }
    }

    private void hookStupidShit(final MethodNode wyd0) {
        final Map<Integer, String> sammi = new HashMap<Integer, String>() {
            {
                super.put(1, "id");
                super.put(3, "strictX");
                super.put(4, "strictY");
                super.put(5, "startHeight");
                super.put(7, "loopCycle");
                super.put(8, "slope");
                super.put(9, "startDistance");
                super.put(10, "targetIndex");
                super.put(11, "endHeight");
            }
        };
        for (final Entry<Integer, String> petra : sammi.entrySet()) {
            final FieldInsnNode marija = wyd(wyd0, Opcodes.ILOAD, petra.getKey(), Hook.PROJECTILE);
            if (marija == null) continue;
            Hook.PROJECTILE.put(new RSField(marija, petra.getValue()));
        }
    }

    private FieldInsnNode wyd(final MethodNode wyd0, final int wyd1, final int wyd2, final Hook wyd3) {
        for (final AbstractInsnNode wyd4 : wyd0.instructions.toArray()) {
            if (wyd4 instanceof VarInsnNode) {
                final VarInsnNode wyd5 = (VarInsnNode) wyd4;
                if (wyd5.var == wyd2 && wyd5.opcode() == wyd1) {
                    AbstractInsnNode wyd6 = wyd5;
                    for (int i = 0; i < 7; i++) {
                        if (wyd6 == null) break;
                        if (wyd6.opcode() == Opcodes.PUTFIELD && ((FieldInsnNode) wyd6).owner.equals(wyd3.getInternalName())) {
                            return (FieldInsnNode) wyd6;
                        }
                        wyd6 = wyd6.next();
                    }
                }
            }
        }
        return null;
    }
}
