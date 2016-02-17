package com.dank.analysis.impl.character.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: RS3Injector
 * Time: 19:38
 * Date: 11-02-2015
 * Created by Dogerina.
 */
public class HitpointsVisitor extends TreeVisitor {

    public HitpointsVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(IDIV) > 0 && block.count(GETFIELD) > 1;
    }

    @Override
    public void visitOperation(final ArithmeticNode an) {
        if (an.opcode() == IDIV && an.preLayer(ISTORE) != null && an.layer(IMUL) != null) {
            for (final AbstractNode imul : an) {
                FieldMemberNode fmn = imul.firstField();
                if (fmn != null && fmn.owner().equals(Hook.CHARACTER.getInternalName())
                        && Hook.CHARACTER.get("maxHitpoints") == null) {
                    Hook.CHARACTER.put(new RSField("maxHitpoints", fmn.fin()));
                }
                for (final AbstractNode child : imul) {
                    fmn = child.firstField();
                    if (fmn != null && fmn.owner().equals(Hook.CHARACTER.getInternalName())
                            && Hook.CHARACTER.get("hitpoints") == null) {
                        Hook.CHARACTER.put(new RSField("hitpoints", fmn.fin()));
                    }
                }
            }
        }
    }
}
