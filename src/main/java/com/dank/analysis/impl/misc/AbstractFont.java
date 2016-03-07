package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

/**
 * Project: DankWise
 * Time: 23:35
 * Date: 15-02-2015
 * Created by Dogerina.
 */
public class AbstractFont extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        //6 int fields since 79. 5 prior to that
        return cn.fieldCount(byte[][].class) == 1 && cn.fieldCount(byte[].class) == 1 &&
                cn.fieldCount(int[].class) == 5 ? new ClassSpec(Hook.ABSTRACT_FONT, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
    }
}
