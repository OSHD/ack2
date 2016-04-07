package com.dank.analysis.impl.focus;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.util.Wildcard;

public class AbstractMouseWheelListener extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	if(cn.access!=1057)
    		return null;
    	if(!cn.ownerless())
    		return null;
    	int nonstatic = 0;
    	for(FieldNode fn : cn.fields){
    		if(!fn.isStatic())
    			nonstatic++;
    	}
    	if(nonstatic!=0)
    		return null;
    	boolean[] map=new boolean[]{false, false};
    	for(MethodNode mn : cn.methods){
    		if(mn.access==1025 && new Wildcard("(Ljava/awt/Component;?)V").matches(mn.desc))
    			map[0]=true;
    		if(mn.access==1025 && new Wildcard("(?)I").matches(mn.desc))
    			map[1]=true;
    	}
        return nonstatic==0 && map[0] && map[1] ? new ClassSpec(Hook.ABSTRACT_MOUSE_WHEEL_LISTENER, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    }
}
