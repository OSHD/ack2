package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 03:27
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class SpellTargetsVisitor extends TreeVisitor {

    public SpellTargetsVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(ICONST_4) == 2 && block.count(IAND) == 1 && block.count(GETSTATIC) == 1 && block.count(GETFIELD) == 0;
    }

    @Override
    public void visitField(final FieldMemberNode fmn) {
        if (fmn.desc().equals("I")) {
            Hook.CLIENT.put(new RSField(fmn, "currentSpellTargets"));
        }
    }
}
