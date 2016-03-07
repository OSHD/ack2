package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.util.Wildcard;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Project: DankWise
 * Time: 23:35
 * Date: 15-02-2015
 * Created by Dogerina.
 */
public class FontImpl extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        for(MethodNode mn : cn.methods) {
            if(new Wildcard("(" + Type.getDescriptor(byte[].class) + "IIIII?)V").matches(mn.desc) &&
                    cn.superName.equals(Hook.FONT_IMPL.getSuperType().getInternalName())) {
                return new ClassSpec(Hook.FONT_IMPL, cn);
            }
        }
        return null;
    }

    @Override
    public void evaluate(ClassNode cn) {
    }
}
