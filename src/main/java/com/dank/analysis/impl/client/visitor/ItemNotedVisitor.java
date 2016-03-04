package com.dank.analysis.impl.client.visitor;

import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 28-02-2015
 * Time: 17:52
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class ItemNotedVisitor extends TreeVisitor {

    public ItemNotedVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(INVOKEVIRTUAL) > 0;
    }

    @Override
    public void visitMethod(MethodMemberNode mmn) {
        /* Check if the method has a call to the copy method */
        if (!mmn.desc().contains(Hook.ITEM_DEFINITION.getInternalDesc() + Hook.ITEM_DEFINITION.getInternalDesc()))
            return;
        /* both calls to static getItemDefinition method within the copy functions params */
        final List<AbstractNode> layers = mmn.layerAll(INVOKESTATIC, IMUL, GETFIELD);
        if (layers != null) {
            for (final AbstractNode an : layers) {
                final FieldMemberNode fmn = (FieldMemberNode) an;
                /* First call is always notedId, so the other one is unnotedId */
                if (Hook.ITEM_DEFINITION.get("notedId") == null) {
                    Hook.ITEM_DEFINITION.put(new RSField(fmn, "notedId"));
                } else if (Hook.ITEM_DEFINITION.get("unnotedId") == null) {
                    Hook.ITEM_DEFINITION.put(new RSField(fmn, "unnotedId"));
                }
            }
        }
    }
}
