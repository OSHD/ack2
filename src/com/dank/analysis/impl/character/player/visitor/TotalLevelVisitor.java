package com.dank.analysis.impl.character.player.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.impl.misc.GStrings;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 09-03-2015
 * Time: 20:22
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class TotalLevelVisitor extends NodeVisitor {

    @Override
    public void visitField(FieldMemberNode fmn) {
        if (GStrings.compare(fmn.key(), "skill-") && fmn.hasGrandparent()) {
            final FieldMemberNode lvl = (FieldMemberNode) fmn.grandparent().layer(IMUL, GETFIELD);
            if (lvl != null && lvl.owner().equals(Hook.PLAYER.getInternalName())) {
                Hook.PLAYER.put(new RSField(lvl, "totalLevel"));
            }
        }
    }
}
