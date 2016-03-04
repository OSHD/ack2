package com.dank.analysis.impl.landscape;

import org.objectweb.asm.tree.ClassNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 02:29
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class BoundaryStub extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(Hook.ENTITY) == 2
                && cn.fieldCount(int.class) == 7 && cn.fieldCount() == 9
                ? new ClassSpec(Hook.BOUNDARY_STUB, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {

    }
}
