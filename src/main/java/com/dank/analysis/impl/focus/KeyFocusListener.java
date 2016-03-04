package com.dank.analysis.impl.focus;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created by RSynapse on 1/23/2016.
 */
public class KeyFocusListener extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        if(cn.interfaces.size() == 2) {
            if(cn.interfaces.get(0).contains("KeyListener") && cn.interfaces.get(1).contains("FocusListener")) {
                return new ClassSpec(Hook.KEY_FOCUS_LISTENER, cn);
            }
        }
        return null;
    }

    @Override
    public void evaluate(ClassNode cn) {

    }
}
