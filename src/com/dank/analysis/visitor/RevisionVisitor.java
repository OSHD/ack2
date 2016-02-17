package com.dank.analysis.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

/**
 * Project: DankWise
 * Date: 23-02-2015
 * Time: 15:48
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class RevisionVisitor extends NodeVisitor {

    @Override
    public void visitNumber(final NumberNode nn) {
        if (nn.number() == 765 && nn.next() instanceof NumberNode && nn.next().next() instanceof NumberNode) {
            final NumberNode height = (NumberNode) nn.next();
            if (height.number() == 503) {
                final NumberNode rev = height.nextNumber();
                System.out.println("Rev: " + rev.number());
            }
        }
    }
}
