package com.dank.analysis.impl.landscape;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;

public class ShapedTile extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	int intarrs =0, ints=0, bools=0, all=0;
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		if(fn.desc.equals("I"))
    			ints++;
    		if(fn.desc.equals("[I"))
    			intarrs++;
    		if(fn.desc.equals("Z"))
    			bools++;
    		all++;
    	}
        return cn.ownerless() && cn.access==49 && all==15 && intarrs==10 && bools==1 && ints==4 ? new ClassSpec(Hook.SHAPED_TILE, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    }
}
