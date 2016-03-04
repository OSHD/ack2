package com.dank.analysis.impl.definition.npc.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.ConversionNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 18-02-2015
 * Time: 04:06
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class IdVisitor extends TreeVisitor {

    public IdVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(I2L) > 0;
    }

    @Override
    public void visitConversion(final ConversionNode cn) {
        if (cn.fromInt() && cn.toLong()) {
            final FieldMemberNode fmn = (FieldMemberNode) cn.layer(IMUL, GETFIELD);
            if (fmn != null) {
                Hook.NPC_DEFINITION.put(new RSField(fmn, "id"));
            }
        }
    }
}
