package com.dank.analysis.impl.focus;

import org.objectweb.asm.tree.ClassNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;

public class MouseListener extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        if(cn.interfaces.size() == 3) {
            if(cn.interfaces.get(0).contains("MouseListener") && cn.interfaces.get(1).contains("MouseMotionListener") && cn.interfaces.get(2).contains("FocusListener")) {
                return new ClassSpec(Hook.MOUSE_LISTENER, cn);
            }
        }
        return null;
    }
    @Override
    public void evaluate(ClassNode cn) {

    }
}
