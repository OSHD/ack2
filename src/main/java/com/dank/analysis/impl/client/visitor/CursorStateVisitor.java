package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 02:10
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class CursorStateVisitor extends TreeVisitor {

    public CursorStateVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.owner.desc.startsWith("(IIII") && block.owner.desc.endsWith("V") && block.count(new MemberQuery(GETSTATIC, "I")) == 1
                && block.count(ICONST_2) == 1 && block.count(IF_ICMPNE) == 1 && block.weight(5, 40);
    }

    @Override
    public void visitJump(final JumpNode jn) {
        final FieldMemberNode fmn = (FieldMemberNode) jn.layer(IMUL, GETSTATIC);
        if (fmn != null && fmn.opcode() == GETSTATIC && fmn.desc().equals("I")) {
            Hook.CLIENT.put(new RSField(fmn, "cursorState"));
        }
    }
}
