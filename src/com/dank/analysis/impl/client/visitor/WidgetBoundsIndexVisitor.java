package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 26-02-2015
 * Time: 00:32
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class WidgetBoundsIndexVisitor extends NodeVisitor {

    @Override
    public void visit(AbstractNode an) {
        if (an.opcode() == BASTORE) {
            final FieldMemberNode boolArray = an.firstField();
            if (boolArray != null && boolArray.isOpcode(GETSTATIC) && boolArray.desc().equals("[Z")) {
                final FieldMemberNode fmn = (FieldMemberNode) an.layer(IMUL, GETFIELD);
                if (fmn != null && fmn.owner().equals(Hook.WIDGET.getInternalName())) {
                    Hook.WIDGET.put(new RSField(fmn, "boundsIndex"));
                }
            }
        }
    }

    @Override
    public void visitField(FieldMemberNode fmn) {
        if (fmn.owner().equals(Hook.WIDGET.getInternalName()) && fmn.putting()) {
            final FieldMemberNode engineCycle = (FieldMemberNode) fmn.layer(IMUL, GETSTATIC);
            if(Hook.CLIENT.get("engineCycle")==null) return;
            if (engineCycle != null && engineCycle.key().equals(Hook.CLIENT.get("engineCycle").key())) {
                Hook.WIDGET.put(new RSField(fmn, "loopCycle"));
            }
        }
    }
}
