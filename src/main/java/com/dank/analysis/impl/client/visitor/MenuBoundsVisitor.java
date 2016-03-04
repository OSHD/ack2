package com.dank.analysis.impl.client.visitor;

import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 19-02-2015
 * Time: 17:13
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class MenuBoundsVisitor extends TreeVisitor {

    //this 1 is bitchy as f0k
    public MenuBoundsVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        //return true;
        return block.count(new MemberQuery(GETSTATIC, "I")) >= 4 && block.count(INVOKESTATIC) >= 1
                && block.count(IADD) == 0 && block.count(ILOAD) == 0;
    }
    //if breaks uncomment the return true, comment the other return and comment visitMethod

    @Override
    public void visitMethod(final MethodMemberNode mmn) {
        if (mmn.isStatic() && mmn.desc().startsWith("(IIII") && mmn.desc().endsWith("V")) {
            final List<AbstractNode> layers = mmn.layerAll(IMUL, GETSTATIC);
            if (layers == null) return;
            for (final AbstractNode layer : layers) {
                final FieldMemberNode fmn = (FieldMemberNode) layer;
                if (Hook.CLIENT.get("menuX") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "menuX"));
                } else if (Hook.CLIENT.get("menuY") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "menuY"));
                } else if (Hook.CLIENT.get("menuWidth") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "menuWidth"));
                } else if (Hook.CLIENT.get("menuHeight") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "menuHeight"));
                }
            }
        }
    }

    @Override
    public void visitField(FieldMemberNode fmn) {
        if (fmn.opcode() == GETSTATIC && fmn.key().equals(Hook.CLIENT.get("menuItemCount").key())) {
            FieldMemberNode height = (FieldMemberNode) fmn.preLayer(IMUL, IADD, PUTSTATIC);
            if (height == null)
                return;
            Hook.CLIENT.put(new RSField(height, "menuHeight"));
            fmn.tree().accept(new NodeVisitor() {
                @Override
                public void visitField(FieldMemberNode fmn) {
                    if (fmn.layer(IMUL, ILOAD) == null)
                        return;
                    if (Hook.CLIENT.get("menuX") == null) {
                        Hook.CLIENT.put(new RSField(fmn, "menuX"));
                    } else if (Hook.CLIENT.get("menuY") == null) {
                        Hook.CLIENT.put(new RSField(fmn, "menuY"));
                    } else if (Hook.CLIENT.get("menuWidth") == null) {
                        Hook.CLIENT.put(new RSField(fmn, "menuWidth"));
                    }
                }
            });
        }
    }
}
