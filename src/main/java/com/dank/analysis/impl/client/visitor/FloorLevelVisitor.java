package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 19:09
 * Date: 12-02-2015
 * Created by Dogerina.
 */
public class FloorLevelVisitor extends TreeVisitor {

    public FloorLevelVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return true;
    }

    @Override
    public void visitMethod(MethodMemberNode mmn) {
        if (mmn.isStatic() || !mmn.desc().startsWith("(IIIIIL") || !mmn.desc().contains(";IIZ") || !mmn.desc().endsWith("Z")) {
            return;
        }
        ArithmeticNode an = mmn.firstOperation();
        if (an.opcode() == IMUL) {
            FieldMemberNode fmn = an.firstField();
            if (fmn.isStatic() && fmn.desc().equals("I")) {
                FieldMemberNode ls = mmn.firstField();
                if (!ls.isStatic() || !ls.desc().startsWith("L")) {
                    return;
                }
                Hook.LANDSCAPE.setInternalName(ls.type());
                Hook.CLIENT.put(new RSField(ls, "landscape"));
                Hook.CLIENT.put(new RSField(fmn, "floorLevel"));
            }
        }
    }
}
