package com.dank.analysis.impl.node;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

//All fields and methods identified as of r111
public class Entity extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return (cn.fieldCount("I") == 1 && cn.getFieldTypeCount() == 1 && cn.access==1057 && cn.superName.equals(Hook.DUAL_NODE.getInternalName()) ? new ClassSpec(Hook.ENTITY, cn) : null);	
    }
    @Override
    public void evaluate(ClassNode cn) {
        for (FieldNode fn : cn.fields) {
        	if(fn.isStatic())
        		continue;
            if (fn.desc.equals("I")) {
                Hook.ENTITY.put(new RSField(fn, "modelHeight"));
            }
        }
        for(MethodNode mn : cn.methods){
        	if(mn.isStatic())
        		continue;
        	MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
        	if(md.referencedFrom.size()>0 && new Wildcard("()L"+Hook.MODEL.getInternalName()+";").matches(mn.desc)){
        		Hook.ENTITY.put(new RSMethod(mn, "getRotatedModel"));
        	}
        	if(md.referencedFrom.size()>0 && new Wildcard("(IIIIIIIII)V").matches(mn.desc)){
        		Hook.ENTITY.put(new RSMethod(mn, "renderAtPoint"));
        		for(MethodData md2 : md.methodReferences){
                	if(md.CLASS_NAME.equals(cn.name)){
                		Hook.ENTITY.put(new RSMethod(md2.bytecodeMethod, "getAnimatedModel"));
                		break;
                	}
        		}
        	}
        }
    }
}
