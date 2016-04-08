package com.dank.analysis.impl.focus;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

public class MouseWheelListener extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	if(cn.access!=33)
    		return null;
    	if(!cn.superName.equals(Hook.ABSTRACT_MOUSE_WHEEL_LISTENER.getInternalName()))
    		return null;
        return new ClassSpec(Hook.MOUSE_WHEEL_LISTENER, cn);
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(MethodNode mn : cn.methods){
    		MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
    		MethodData supermd = DynaFlowAnalyzer.getMethod(Hook.ABSTRACT_MOUSE_WHEEL_LISTENER.getInternalName(), mn.name, mn.desc);
    		if(md==null || supermd==null)
    			continue;
    		if(supermd.referencedFrom.size()>0 && new Wildcard("(Ljava/awt/Component;?)V").matches(mn.desc)){
    			List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
						Mask.INVOKEVIRTUAL.describe("(Ljava/awt/event/MouseWheelListener;)V")
						);
				if (pattern != null) {
					MethodInsnNode invoke = (MethodInsnNode)pattern.get(0);
					Hook.MOUSE_WHEEL_LISTENER.put(new RSMethod(mn, invoke.name));
					Hook.ABSTRACT_MOUSE_WHEEL_LISTENER.put(new RSMethod(supermd.bytecodeMethod, invoke.name));
				}
    		}
    		if(supermd.referencedFrom.size()>0 && new Wildcard("(?)I").matches(mn.desc)){
				Hook.MOUSE_WHEEL_LISTENER.put(new RSMethod(mn, "popRotation"));
				Hook.ABSTRACT_MOUSE_WHEEL_LISTENER.put(new RSMethod(supermd.bytecodeMethod, "popRotation"));
    		}
    	}
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		if(fn.desc.equals("I")){
        		Hook.MOUSE_WHEEL_LISTENER.put(new RSField(fn, "rotation"));
    		}
    	}
    }
}
