package com.dank.analysis.impl.node;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

public class HUD extends Analyser implements Opcodes {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName.equals(Hook.NODE.getInternalName()) && cn.fieldCount(int.class) == 2 && cn.fieldCount(boolean.class) == 1
                ? new ClassSpec(Hook.HUD, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
    		if(fd.bytecodeField.desc.equals("I")){
    			boolean isOwner=false;
	    		for(MethodData md : fd.referencedFrom){
	    			if(new Wildcard("(L"+cn.name+";Z?)V").matches(md.METHOD_DESC)){
	                    isOwner=true;
	                    break;
	    			}
	    		}
	    		if(isOwner)
                    Hook.HUD.put(new RSField(fn, "owner"));
	    		else
                    Hook.HUD.put(new RSField(fn, "type"));
    		}
    		if(fd.bytecodeField.desc.equals("Z")){
                Hook.HUD.put(new RSField(fn, "isMainHud"));
    		}
    	}
    }
}
