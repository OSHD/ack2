package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.hook.Hook;
import com.dank.hook.RSMethod;

/**
 * Project: DankWise
 * Date: 01-03-2015
 * Time: 23:57
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class WidgetConfigVisitor extends NodeVisitor {

    private final MethodNode mn;

    public WidgetConfigVisitor(MethodNode mn) {
        this.mn = mn;
    }

    @Override
    public void visitField(FieldMemberNode fmn) {
        if (fmn.desc().equals(Hook.NODETABLE.getInternalDesc())) {
            Hook.CLIENT.put(new RSMethod(mn, "getWidgetConfig"));
        }
    }
}
