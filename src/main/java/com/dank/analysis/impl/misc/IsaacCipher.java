package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

import java.util.Hashtable;

import org.objectweb.asm.tree.*;

//All fields and methods identified as of r111
public class IsaacCipher extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	if(cn.access!=49)
    		return null;
    	if(!cn.ownerless())
    		return null;
    	int nonstatic=0, intarray=0, ints=0;
    	for(FieldNode fn : cn.fields){
    		if(!fn.isStatic()){
    			nonstatic++;
    			if(fn.desc.equals("[I"))
    				intarray++;
    			if(fn.desc.equals("I"))
    				ints++;
    		}
    	}
    	if(nonstatic==6 && intarray==2 && ints==4)
    		return new ClassSpec(Hook.ISAAC_CIPHER, cn);
    	return null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(MethodNode mn : cn.methods){
    		if(mn.isStatic())
    			continue;
    		MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
    		if(new Wildcard("(?)I").matches(mn.desc)){
        		Hook.ISAAC_CIPHER.put(new RSMethod(mn, "next"));
    		}
    		if(new Wildcard("(?)V").matches(mn.desc)){
    			if(md.methodReferences.size()>0){
            		for(MethodData md2 : md.methodReferences){
                		if(new Wildcard("(?)V").matches(md2.METHOD_DESC)){
                			Hook.ISAAC_CIPHER.put(new RSMethod(mn, "initializeKeySet"));
                    		Hook.ISAAC_CIPHER.put(new RSMethod(md2.bytecodeMethod, "decrypt"));
                			break;
                		}
            		}
    			}
    		}
    	}
    	RSMethod method = (RSMethod)Hook.ISAAC_CIPHER.get("next");
    	if(method!=null){
    		MethodData md = DynaFlowAnalyzer.getMethod(method.owner, method.name, method.desc);
    		boolean count=false;
    		for(FieldData fd : md.fieldReferences){
    			if(fd.bytecodeField.desc.equals("[I")){
                    Hook.ISAAC_CIPHER.put(new RSField(fd.bytecodeField, "results"));
    			}
    			if(!count && fd.bytecodeField.desc.equals("I")){
                    Hook.ISAAC_CIPHER.put(new RSField(fd.bytecodeField, "count"));
                    count=true;
    			}
    		}
    		for(FieldNode fn : cn.fields){
    			if(fn.isStatic())
    				continue;
    			if(fn.desc.equals("[I")){
    				if(!fn.name.equals(Hook.ISAAC_CIPHER.get("results").name))
                        Hook.ISAAC_CIPHER.put(new RSField(fn, "memory"));
    			}
    		}
    	}
    	method = (RSMethod)Hook.ISAAC_CIPHER.get("decrypt");
    	if(method!=null){
    		MethodData md = DynaFlowAnalyzer.getMethod(method.owner, method.name, method.desc);
    		Hashtable<String, Integer> fieldRefCount = new Hashtable<String, Integer>();//Field name, count
    		for(FieldData fd : md.fieldReferences){
    			if(!fd.CLASS_NAME.equals(cn.name))
    				continue;
    			int count=fieldRefCount.containsKey(fd.FIELD_NAME)?fieldRefCount.get(fd.FIELD_NAME):0;
    			count++;
    			fieldRefCount.put(fd.FIELD_NAME, count);
    		}
    		for(String key : fieldRefCount.keySet()){
    			FieldData fd = DynaFlowAnalyzer.getField(cn.name, key);
    			int count = fieldRefCount.get(key);
    			if(count==2){
                    Hook.ISAAC_CIPHER.put(new RSField(fd.bytecodeField, "counter"));
    			}
    			if(count==4){
                    Hook.ISAAC_CIPHER.put(new RSField(fd.bytecodeField, "lastResult"));
    			}
    			if(count==15){
                    Hook.ISAAC_CIPHER.put(new RSField(fd.bytecodeField, "accumulator"));
    			}
    		}
    	}
    }
}
