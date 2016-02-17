package com.dank.analysis.impl.character.player.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 27-02-2015
 * Time: 18:48
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class TeamVisitor extends NodeVisitor {

    @Override
    public void visitNumber(NumberNode nn) {
        if (nn.opcode() == ICONST_0 && nn.parent().opcode() == PUTFIELD) {
            final FieldMemberNode fmn = (FieldMemberNode) nn.parent();
            if (fmn.desc().equals("I") && fmn.owner().equals(Hook.PLAYER.getInternalName())) {
                Hook.PLAYER.put(new RSField(fmn, "team"));
            }
        }
    }
}
