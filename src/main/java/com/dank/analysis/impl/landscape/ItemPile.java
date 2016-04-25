package com.dank.analysis.impl.landscape;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.dank.analysis.Analyser;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

//All fields and methods identified as of r113
public class ItemPile extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(Hook.ENTITY) == 3 ? new ClassSpec(Hook.ITEM_PILE, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
    		if(fn.desc.equals("L"+Hook.ENTITY.getInternalName()+";")){
    			for(MethodData md : fd.referencedFrom){
    				if(new Wildcard("(IIIIL"+Hook.ENTITY.getInternalName()+";IL"+Hook.ENTITY.getInternalName()+";L"+Hook.ENTITY.getInternalName()+";)V").matches(md.METHOD_DESC)){
    					List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md.bytecodeMethod,
								Mask.ILOAD,
								Mask.IMUL.distance(2),
								Mask.PUTFIELD
								);
						if (patterns != null) {
							for(ArrayList<AbstractInsnNode> pattern : patterns){
								VarInsnNode var = (VarInsnNode) pattern.get(0);
								FieldInsnNode fin = (FieldInsnNode) pattern.get(2);
								if(var.var==2)
									Hook.ITEM_PILE.put(new RSField(fin, "strictX"));
								if(var.var==3)
									Hook.ITEM_PILE.put(new RSField(fin, "strictY"));
								if(var.var==4)
									Hook.ITEM_PILE.put(new RSField(fin, "counterHeight"));
								if(var.var==6)
									Hook.ITEM_PILE.put(new RSField(fin, "uid"));
								if(var.var==10)
									Hook.ITEM_PILE.put(new RSField(fin, "height"));
							}
						}
						patterns = Assembly.findAll(md.bytecodeMethod,
								Mask.ALOAD,
								Mask.PUTFIELD
								);
						if (patterns != null) {
							for(ArrayList<AbstractInsnNode> pattern : patterns){
								VarInsnNode var = (VarInsnNode) pattern.get(0);
								FieldInsnNode fin = (FieldInsnNode) pattern.get(1);
								if(var.var==5)
									Hook.ITEM_PILE.put(new RSField(fin, "bottom"));
								if(var.var==7)
									Hook.ITEM_PILE.put(new RSField(fin, "middle"));
								if(var.var==8)
									Hook.ITEM_PILE.put(new RSField(fin, "top"));
							}
						}
    					break;
    				}
    			}
    		}
    	}
    }
}
