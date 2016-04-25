package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;

public class AnimationSequence extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName.equals(Hook.DUAL_NODE.getInternalName()) && cn.fieldCount("[I") == 5 && cn.fieldCount("Z") == 1 ? new ClassSpec(Hook.ANIMATION_SEQUENCE, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	
    }
}
