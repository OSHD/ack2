package com.dank.analysis.impl.widget.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.ArrayLoadNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.ReferenceNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 25-02-2015
 * Time: 21:24
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class GenericVisitor extends NodeVisitor {

    @Override
    public void visitMethod(MethodMemberNode mmn) {
        if (!mmn.isStatic() && mmn.name().contains("length") && mmn.firstField() != null) {
            Hook.WIDGET.put(new RSField(mmn.firstField(), "tooltip"));
        }
    }

    @Override
    public void visitArrayLoad(ArrayLoadNode aln) {
        if (aln.parent().opcode() == IASTORE && aln.array().opcode() == GETFIELD && aln.opcode() == AALOAD) {
            Hook.WIDGET.put(new RSField((ReferenceNode) aln.array(), "varpOpcodes"));
        }
    }

    @Override
    public void visitOperation(ArithmeticNode an) {
        FieldMemberNode fmn;
        if (an.opcode() == IAND && (fmn = (FieldMemberNode) an.layer(IMUL, GETFIELD)) != null) {
            //System.out.println(an.method().key());
            Hook.WIDGET.put(new RSField(fmn, "id"));
        } else if (an.opcode() == IOR && (fmn = (FieldMemberNode) an.layer(IMUL, GETFIELD)) != null) {
            Hook.WIDGET.put(new RSField(fmn, "config"));
        }
    }

    @Override
    public void visitJump(final JumpNode jn) {
        if (jn.opcode() == IF_ICMPNE && jn.firstNumber() != null) {
            final FieldMemberNode fmn = (FieldMemberNode) jn.layer(IMUL, GETFIELD);
            if (fmn != null && jn.firstNumber().number() == 9) {
                Hook.WIDGET.put(new RSField(fmn, "type"));
            }
        }
    }
}
