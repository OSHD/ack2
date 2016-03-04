package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 03-03-2015
 * Time: 09:14
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class EngineVarVisitor extends NodeVisitor {

    @Override
    public void visitField(FieldMemberNode fmn) {
        final NumberNode nn = (NumberNode) fmn.layer(NEWARRAY, SIPUSH);
        if (nn != null && nn.number() == 2000 && Hook.CLIENT.get("engineVars") == null
                && !fmn.name().equals(Hook.CLIENT.get("tempVars").name)) {
            Hook.CLIENT.put(new RSField(fmn, "engineVars"));
        }
    }
}
