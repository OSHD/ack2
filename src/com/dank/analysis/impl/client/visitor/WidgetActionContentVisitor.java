package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 26-02-2015
 * Time: 00:15
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class WidgetActionContentVisitor extends NodeVisitor {

    @Override
    public void visitJump(JumpNode jn) {
        if (jn.opcode() == IF_ICMPNE) {
            if (jn.hasChild(ICONST_4)) {
                final FieldMemberNode fmn = (FieldMemberNode) jn.layer(IMUL, GETFIELD);
                if (fmn != null && fmn.owner().equals(Hook.WIDGET.getInternalName())
                        && !fmn.name().equals(Hook.WIDGET.get("type").name)) {
                    Hook.WIDGET.put(new RSField(fmn, "buttonType"));
                }
            } else if (jn.hasChild(SIPUSH)) {
                final NumberNode nn = jn.firstNumber();
                if (nn.number() == 1337 || nn.number() == 1338) {
                    final FieldMemberNode fmn = (FieldMemberNode) jn.layer(IMUL, GETFIELD);
                    if (fmn != null && fmn.owner().equals(Hook.WIDGET.getInternalName())) {
                        Hook.WIDGET.put(new RSField(fmn, "contentType"));
                    }
                }
            }
        }
    }
}
