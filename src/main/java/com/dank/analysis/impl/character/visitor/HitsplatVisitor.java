package com.dank.analysis.impl.character.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 20-02-2015
 * Time: 23:31
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class HitsplatVisitor extends NodeVisitor {

    @Override
    public void visitNumber(NumberNode nn) {
        if (nn.number() == 70) {
            nn.tree().accept(new NodeVisitor() {
                @Override
                public void visit(AbstractNode an) {
                    if (an.opcode() == IASTORE) {
                        final FieldMemberNode fmn = an.firstField();
                        final VariableNode vn = (VariableNode) an.last(ILOAD);
                        if (fmn != null && vn != null) {
                            if (vn.var() == 1) {
                                Hook.CHARACTER.put(new RSField(fmn, "hitsplatTypes"));
                            } else if (vn.var() == 2) {
                                Hook.CHARACTER.put(new RSField(fmn, "hitsplatDamages"));
                            }
                        }
                    }
                }

                @Override
                public void visitJump(JumpNode jn) {
                    if (jn.opcode() != GOTO) {
                        final FieldMemberNode cycles = (FieldMemberNode) jn.layer(IALOAD, GETFIELD);
                        if (cycles != null) {
                            Hook.CHARACTER.put(new RSField(cycles, "hitsplatCycles"));
                        }
                    }
                }
            });
        }
    }
}
