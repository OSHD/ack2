package com.dank.analysis.impl.node;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

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
    }
}
