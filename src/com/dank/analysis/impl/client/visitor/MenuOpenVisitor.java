package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 19-02-2015
 * Time: 18:17
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class MenuOpenVisitor extends NodeVisitor {

    @Override
    public void visitJump(final JumpNode jn) {
        if (jn.isOpcode(IFEQ) && jn.children() == 1) {
            final FieldMemberNode fmn = jn.firstField();
            if (fmn != null && fmn.desc().equals("Z") && fmn.isOpcode(GETSTATIC)) {
                Hook.CLIENT.put(new RSField(fmn, "menuOpen"));
            }
        }
    }
}
