package com.dank.analysis.impl.character.player;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.character.AnimInterp2;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 16:39
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class Player extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(Hook.PLAYER_CONFIG) == 1 ? new ClassSpec(Hook.PLAYER, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {

        for(MethodNode mn : cn.methods) {
            if(mn.equals("a","(Ldx;B)V")) { //TODO r112
                AnimInterp2.run(cn.name,mn);
            }
        }

        for (final FieldNode fn : cn.fields) {
            if (!fn.isStatic()) {
                if (fn.desc.equals(Hook.PLAYER_CONFIG.getInternalDesc())) {
                    Hook.PLAYER.put(new RSField(fn, "config"));
                } else if (fn.desc.equals("Ljava/lang/String;")) {
                    Hook.PLAYER.put(new RSField(fn, "name"));
                }
            }
        }
    }


}
