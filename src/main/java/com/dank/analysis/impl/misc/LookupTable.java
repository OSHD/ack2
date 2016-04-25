package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;

//All fields and methods identified as of r113
public class LookupTable extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	if(cn.access!=33)
    		return null;
    	if(!cn.ownerless())
    		return null;
        return cn.fieldCount(int[].class) == 1 && cn.fieldCount() == 1 ? new ClassSpec(Hook.LOOKUP_TABLE, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(MethodNode mn : cn.methods){
    		if(mn.isStatic())
    			continue;
    		if(new Wildcard("(?)I").matches(mn.desc)){
				Hook.LOOKUP_TABLE.put(new RSMethod(mn, "lookupIdentifier"));
    		}
    	}
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		if(fn.desc.equals("[I"))
				Hook.LOOKUP_TABLE.put(new RSField(fn, "identityTable"));
    	}
    }
}
