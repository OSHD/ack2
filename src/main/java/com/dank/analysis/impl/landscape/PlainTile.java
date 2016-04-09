package com.dank.analysis.impl.landscape;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import com.dank.analysis.Analyser;
import com.dank.hook.Hook;

public class PlainTile extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	int ints =0, bools=0, all=0;
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		if(fn.desc.equals("I"))
    			ints++;
    		if(fn.desc.equals("Z"))
    			bools++;
    		all++;
    	}
        return cn.ownerless() && cn.access==49 && all==7 && ints==6 && bools==1 ? new ClassSpec(Hook.PLAIN_TILE, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    }
}
