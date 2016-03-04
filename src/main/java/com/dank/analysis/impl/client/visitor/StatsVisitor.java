package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 18-02-2015
 * Time: 23:41
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class StatsVisitor extends NodeVisitor {

    @Override
    public void visitField(FieldMemberNode fmn) {
        if (fmn.desc().equals("[I")) {
            final NumberNode nn = (NumberNode) fmn.layer(NEWARRAY, BIPUSH);
            if (nn != null && nn.number() == 25) {
                if (Hook.CLIENT.get("currentLevels") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "currentLevels"));
                } else if (Hook.CLIENT.get("levels") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "levels"));
                } else if (Hook.CLIENT.get("experiences") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "experiences"));
                }
            }
        }
    }
}
