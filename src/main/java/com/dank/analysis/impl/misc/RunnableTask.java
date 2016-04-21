package com.dank.analysis.impl.misc;

import java.awt.EventQueue;
import java.io.RandomAccessFile;
import java.lang.reflect.Modifier;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

public class RunnableTask extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	if(cn.access!=33)
    		return null;
    	if(!cn.ownerless())
    		return null;
        return cn.fieldCount(Thread.class) == 1 && cn.fieldCount(EventQueue.class) == 1 && cn.fieldCount(boolean.class) == 1
                ? new ClassSpec(Hook.RUNNABLE_TASK, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
    		if(fd.bytecodeField.desc.equals("Z"))
            	Hook.RUNNABLE_TASK.put(new RSField(fn, "active"));
    		if(fd.bytecodeField.desc.equals("Ljava/lang/Thread;"))
            	Hook.RUNNABLE_TASK.put(new RSField(fn, "thread"));
    		if(fd.bytecodeField.desc.equals("Ljava/awt/EventQueue;"))
            	Hook.RUNNABLE_TASK.put(new RSField(fn, "eventQueue"));
    	}
    }
}
