package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 02:49
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class SelectedNameVisitor extends TreeVisitor {

    public SelectedNameVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return true;
    }

    @Override
    public void visitMethod(final MethodMemberNode mmn) {
        if (block.count(INVOKESTATIC) > 0 && mmn.key().equals(Hook.CLIENT.get("addMenuRow").key())) {
            if (mmn.hasChild(ICONST_2)) {
                final FieldMemberNode spell = (FieldMemberNode) mmn.layer(INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKEVIRTUAL,
                        INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKEVIRTUAL, GETSTATIC);
                final FieldMemberNode prefix = (FieldMemberNode) mmn.layer(INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKEVIRTUAL,
                        INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKESPECIAL, DUP, GETSTATIC);
                if (spell != null && prefix != null) {
                    Hook.CLIENT.put(new RSField(spell, "selectedSpellName"));
                    Hook.CLIENT.put(new RSField(prefix, "menuActionPrefix"));
                }
            } else if (mmn.hasChild(ICONST_1)) {
                final FieldMemberNode item = (FieldMemberNode) mmn.layer(INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKEVIRTUAL,
                        INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKEVIRTUAL, GETSTATIC);
                if (item != null) {
                    Hook.CLIENT.put(new RSField(item, "selectedItemName"));
                }
            }
        }
    }
}
