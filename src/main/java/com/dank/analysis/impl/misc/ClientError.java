package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

//All methods and fields identified as of r112
public class ClientError extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	if(cn.access!=33)
    		return null;
    	if(cn.ownerless() || !cn.superName.equals("java/lang/RuntimeException"))
    		return null;
        return cn.fieldCount(Throwable.class) == 1 && cn.fieldCount(String.class) == 1
                ? new ClassSpec(Hook.CLIENT_ERROR, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	//No instanced methods as of r112
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		if(fn.desc.equals("Ljava/lang/Throwable;")){
            	Hook.CLIENT_ERROR.put(new RSField(fn, "source"));
    		}
    		if(fn.desc.equals("Ljava/lang/String;")){
            	Hook.CLIENT_ERROR.put(new RSField(fn, "message"));
    		}
    	}
    }
}
