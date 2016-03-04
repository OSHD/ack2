package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 26-02-2015
 * Time: 00:55
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class EngineCycleVisitor extends NodeVisitor {

    @Override
    public void visitOperation(ArithmeticNode an) {
        if (an.remainding()) {
            final NumberNode nn = an.firstNumber();
            if (nn != null && nn.number() == 40) {
                final FieldMemberNode fmn = (FieldMemberNode) an.layer(IMUL, GETSTATIC);
                if (fmn == null) return;
                Hook.CLIENT.put(new RSField(fmn, "engineCycle"));
            }
        }
    }
}
