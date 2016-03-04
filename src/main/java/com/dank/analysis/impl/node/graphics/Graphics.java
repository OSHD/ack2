package com.dank.analysis.impl.node.graphics;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created by RSynapse on 2/20/2016.
 */
public class Graphics extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.name.equals(Hook.SPRITE.resolve().superName) ?  new ClassSpec(Hook.GRAPHICS, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {

    }
}
