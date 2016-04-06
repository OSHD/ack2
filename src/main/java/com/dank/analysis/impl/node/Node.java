package com.dank.analysis.impl.node;

import java.lang.reflect.Modifier;

import com.dank.util.Wildcard;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;

//All Fields/Methods Identified as of r111
public class Node extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount("J") == 1 && cn.fieldCount('L' + cn.name + ';') == 2 && cn.ownerless() ? new ClassSpec(Hook.NODE, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (fn.desc.equals("J")) {
                Hook.NODE.put(new RSField(fn, "key"));
            } else if (fn.desc.equals("L"+cn.name+";")) {
                Hook.NODE.put(Modifier.isPublic(fn.access) ? new RSField(fn, "next") : new RSField(fn, "previous"));
            }
        }
        boolean isParentFound=false;
        for (final MethodNode mn : cn.methods) {
        	if(Modifier.isStatic(mn.access))
        		continue;
            MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
            if(md!=null){
            	if (md.referencedFrom.size()>0 && new Wildcard("()V").matches(mn.desc) && !mn.name.contains("<")) {
            		Hook.NODE.put(new RSMethod(mn, "unlink"));
            	}
            	if(!isParentFound && new Wildcard("()Z").matches(mn.desc) && !mn.name.contains("<")) {
            		Hook.NODE.put(new RSMethod(mn, "isParent"));
            		isParentFound=true;
            	}
            }
        }
    }
}
