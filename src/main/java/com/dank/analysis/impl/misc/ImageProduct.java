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
public class ImageProduct extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(int[][].class) == 2 && cn.fieldCount(Object[].class) == 1 &&
                cn.fieldCount(Object[][].class) == 1 ? new ClassSpec(Hook.IMAGE_PRODUCT, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
    }
}
