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

public class Queue extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	if(cn.access!=49)
    		return null;
    	if(!cn.ownerless())
    		return null;
    	int nonstatic=0, dualnode=0;
    	for(FieldNode fn : cn.fields){
    		if(!fn.isStatic()){
    			nonstatic++;
    			if(fn.desc.equals("L"+Hook.DUAL_NODE.getInternalName()+";"))
    				dualnode++;
    		}
    	}
    	if(nonstatic==1 && dualnode==1)
    		return new ClassSpec(Hook.QUEUE, cn);
    	return null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(MethodNode mn : cn.methods){
    		MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
    		if(md.referencedFrom.size()>0 && new Wildcard("(L"+Hook.DUAL_NODE.getInternalName()+";)V").matches(md.METHOD_DESC) && !md.METHOD_NAME.equals("<init>")){
    			boolean isFirst=false;
    			for(MethodData md2 : md.referencedFrom){
    				if(new Wildcard("(J)L"+Hook.DUAL_NODE.getInternalName()+";").matches(md2.METHOD_DESC)){
    					isFirst=true;
    					break;
    				}
    			}
    			if(isFirst)
    				Hook.QUEUE.put(new RSMethod(mn, "putLast"));
    			else
    				Hook.QUEUE.put(new RSMethod(mn, "putFirst"));
    		}
    		if(md.referencedFrom.size()>0 && new Wildcard("()V").matches(md.METHOD_DESC) && !md.METHOD_NAME.equals("<init>")){
    			Hook.QUEUE.put(new RSMethod(mn, "reset"));
    		}
    		if(md.referencedFrom.size()>0 && new Wildcard("()L"+Hook.DUAL_NODE.getInternalName()+";").matches(md.METHOD_DESC)){
        		if(md.methodReferences.size()>0)
        			Hook.QUEUE.put(new RSMethod(mn, "remove"));
        		else
        			Hook.QUEUE.put(new RSMethod(mn, "getFirst"));
    		}
    	}
        for (final FieldNode fn : cn.fields) {
            if (fn.desc.equals("L"+Hook.DUAL_NODE.getInternalName()+";")) {
                Hook.QUEUE.put(new RSField(fn, "head"));
            }
        }
    }
}
