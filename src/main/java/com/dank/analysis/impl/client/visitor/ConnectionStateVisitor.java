package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 28-02-2015
 * Time: 19:09
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class ConnectionStateVisitor extends NodeVisitor {

    @Override
    public void visitJump(JumpNode jn) {
        if (jn.opcode() == IF_ICMPEQ || jn.opcode() == IF_ICMPNE) {
            final NumberNode nn = (NumberNode) jn.layer(BIPUSH);
            if (nn != null && nn.number() == 11) {
                final FieldMemberNode fmn = (FieldMemberNode) jn.layer(IMUL, GETSTATIC);
                if (fmn != null) {
                    Hook.CLIENT.put(new RSField(fmn, "connectionState"));
                }
            }
        }
    }
}
