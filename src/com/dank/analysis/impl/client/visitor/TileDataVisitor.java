package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 03-03-2015
 * Time: 09:22
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class TileDataVisitor extends NodeVisitor {

    @Override
    public void visitJump(JumpNode jn) {
        if (jn.opcode() == IF_ICMPNE || jn.opcode() == IF_ICMPEQ) {
            final FieldMemberNode fmn = (FieldMemberNode) jn.layer(IAND, BALOAD, AALOAD, AALOAD, GETSTATIC);
            if (fmn != null) {
                Hook.CLIENT.put(new RSField(fmn, "renderRules"));
            }
        }
    }

    @Override
    public void visitOperation(ArithmeticNode an) {
        if (an.opcode() == ISUB) {
            final FieldMemberNode fmn = (FieldMemberNode) an.layer(IALOAD, AALOAD, AALOAD, GETSTATIC);
            if (fmn != null) {
                Hook.CLIENT.put(new RSField(fmn, "tileHeights"));
            }
        }
    }
}
