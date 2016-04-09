package com.dank.analysis.impl.landscape;

import org.objectweb.asm.tree.ClassNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 02:32
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class TileStub extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(Hook.ENTITY) == 1
                && cn.fieldCount(int.class) == 5
                && cn.fieldCount() == 6
                ? new ClassSpec(Hook.TILE_DECORATION_STUB, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {

    }
}
