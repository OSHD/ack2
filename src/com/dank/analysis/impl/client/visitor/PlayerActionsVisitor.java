package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.impl.misc.GStrings;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 17:32
 * Date: 15-02-2015
 * Created by Dogerina.
 */
public class PlayerActionsVisitor extends TreeVisitor {

    public PlayerActionsVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(GETSTATIC) > 1;
    }

    @Override
    public void visitField(final FieldMemberNode fmn) {
        if (GStrings.compare(fmn.key(), "Attack") && fmn.hasParent() && fmn.parent().opcode() == INVOKEVIRTUAL) {
            final FieldMemberNode actions = (FieldMemberNode) fmn.parent().layer(AALOAD, GETSTATIC);
            if (actions != null && actions.desc().equals("[Ljava/lang/String;")) {
                Hook.CLIENT.put(new RSField(actions, "playerActions"));
            }
        }
    }
}
