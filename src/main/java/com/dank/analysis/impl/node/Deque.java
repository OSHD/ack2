package com.dank.analysis.impl.node;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;


//All fields identified as of r111
public class Deque extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.ownerless() && cn.getFieldTypeCount() == 1 && cn.fieldCount("L"+Hook.NODE.getInternalName()+";") == 2 ? new ClassSpec(Hook.DEQUE, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		if(fn.desc.equals("L"+Hook.NODE.getInternalName()+";"))
    			Hook.DEQUE.put(fn.access==1 ? new RSField(fn, "head") : new RSField(fn, "tail"));
    	}
    }
}
