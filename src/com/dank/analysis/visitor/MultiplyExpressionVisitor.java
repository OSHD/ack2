package com.dank.analysis.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Project: DankWise
 * Date: 28-02-2015
 * Time: 16:13
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class MultiplyExpressionVisitor extends TreeVisitor {

    public MultiplyExpressionVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(FieldInsnNode.class) > 0;
    }

    @Override
    public void visitOperation(ArithmeticNode an) {
        if ((an.opcode() == IMUL || an.opcode() == LMUL) && an.children() == 2) {
            final NumberNode nn = an.firstNumber();
            if (nn != null && nn.opcode() == LDC && an.child(0).opcode() == LDC) {
                final AbstractNode otherChild = an.child(1);
                /* Swap children of multiply instruction */
                nn.delete();
                an.parent().add(1, nn);
            }
        }
    }

    private boolean isSetting(ArithmeticNode an) {
        return an.hasParent() && (an.parent().opcode() == PUTSTATIC || an.parent().opcode() == PUTFIELD);
    }
}
