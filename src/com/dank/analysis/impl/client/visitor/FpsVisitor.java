package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ConstantNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 02-03-2015
 * Time: 21:57
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class FpsVisitor extends NodeVisitor {

    @Override
    public void visitConstant(ConstantNode cn) {
        if (cn.toString().toLowerCase().contains("fps")) {
            final FieldMemberNode fps = (FieldMemberNode) cn.parent().parent().layer(IMUL, GETSTATIC);
            if (fps != null) {
                Hook.CLIENT.put(new RSField(fps, "fps"));
            }
        }
    }
}
