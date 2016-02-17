package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 19-02-2015
 * Time: 18:57
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class CharacterTargetIndexVisitor extends NodeVisitor {

    @Override
    public void visitNumber(final NumberNode nn) {
        if (nn.number() == 32768 && nn.parent().opcode() == ISUB) {
            final FieldMemberNode fmn = (FieldMemberNode) nn.parent().layer(IMUL, GETFIELD);
            if (fmn == null)
                return;
            Hook.CHARACTER.put(new RSField(fmn, "targetIndex"));
        }
    }
}
