package com.dank.analysis.impl.model.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 00:45
 * Date: 16-02-2015
 * Created by Dogerina.
 */
public class CursorUidsVisitor extends TreeVisitor {

    public CursorUidsVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(new MemberQuery(GETSTATIC, "[I")) == 1 && block.count(new MemberQuery(PUTSTATIC, "I")) == 1
                && block.count(ICONST_1) == 1 && block.count(IASTORE) == 1;
    }

    @Override
    public void visitField(final FieldMemberNode fmn) {
        if (fmn.desc().equals("[I")) {
            Hook.CLIENT.put(new RSField(fmn, "onCursorUids"));
        } else if (fmn.desc().equals("I")) {
            Hook.CLIENT.put(new RSField(fmn, "onCursorCount"));
        }
    }
}
