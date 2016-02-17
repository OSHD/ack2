package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.TypeNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 17-02-2015
 * Time: 02:02
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class DequeObjectVisitor extends TreeVisitor {

    public DequeObjectVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(new MemberQuery(GETSTATIC, Hook.DEQUE.getInternalDesc())) == 1
                && block.count(CHECKCAST) == 1 && block.count(new MemberQuery(INVOKEVIRTUAL)) == 1;
    }

    @Override
    public void visitType(final TypeNode tn) {
        final FieldMemberNode fmn = (FieldMemberNode) tn.parent().layer(INVOKEVIRTUAL, GETSTATIC);
        if (fmn != null) {
            if (tn.type().equals(Hook.GRAPHICS_STUB.getInternalName())) {
                Hook.CLIENT.put(new RSField(fmn, "graphicsObjectDeque"));
            } else if (tn.type().equals(Hook.PROJECTILE.getInternalName())) {
                Hook.CLIENT.put(new RSField(fmn, "projectileDeque"));
            }
        }
    }
}
