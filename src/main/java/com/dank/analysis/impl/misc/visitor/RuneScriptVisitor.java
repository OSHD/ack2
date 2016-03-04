package com.dank.analysis.impl.misc.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.TypeNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 20-02-2015
 * Time: 23:15
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class RuneScriptVisitor extends NodeVisitor {

    @Override
    public void visitMethod(final MethodMemberNode mmn) {
        if (mmn.desc().endsWith("I")) {
            final FieldMemberNode fmn = (FieldMemberNode) mmn.preLayer(IMUL, PUTFIELD);
            if (fmn != null) {
                if (Hook.RUNESCRIPT.get("intArgCount") == null) {
                    Hook.RUNESCRIPT.put(new RSField(fmn, "intArgCount"));
                } else if (Hook.RUNESCRIPT.get("stringArgCount") == null) {
                    Hook.RUNESCRIPT.put(new RSField(fmn, "stringArgCount"));
                } else if (Hook.RUNESCRIPT.get("intStackCount") == null) {
                    Hook.RUNESCRIPT.put(new RSField(fmn, "intStackCount"));
                } else if (Hook.RUNESCRIPT.get("stringStackCount") == null) {
                    Hook.RUNESCRIPT.put(new RSField(fmn, "stringStackCount"));
                }
            }
        }
    }

    @Override
    public void visitNumber(final NumberNode nn) {
        if (nn.opcode() == NEWARRAY) {
            final FieldMemberNode ops = (FieldMemberNode) nn.preLayer(PUTFIELD);
            if (ops != null) {
                if (Hook.RUNESCRIPT.get("opcodes") == null) {
                    Hook.RUNESCRIPT.put(new RSField(ops, "opcodes"));
                } else if (Hook.RUNESCRIPT.get("intOperands") == null) {
                    Hook.RUNESCRIPT.put(new RSField(ops, "intOperands"));
                }
            }
        }
    }

    @Override
    public void visitType(final TypeNode tn) {
        if (tn.isType(String.class)) {
            final FieldMemberNode operands = (FieldMemberNode) tn.preLayer(PUTFIELD);
            if (operands != null) {
                Hook.RUNESCRIPT.put(new RSField(operands, "stringOperands"));
            }
        }
    }
}
