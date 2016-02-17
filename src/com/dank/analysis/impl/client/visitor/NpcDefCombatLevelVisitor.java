package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.impl.misc.GStrings;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * @author Septron
 * @since February, 27
 */
public class NpcDefCombatLevelVisitor extends TreeVisitor {

    public NpcDefCombatLevelVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return true;
    }

    @Override
    public void visitField(FieldMemberNode fmn) {
        if (GStrings.compare(fmn.key(), "level-") && fmn.parent() != null && fmn.parent().parent() != null) {
            final FieldMemberNode lvl = (FieldMemberNode) fmn.parent().parent().layer(IMUL, GETFIELD);
            if (lvl != null && !lvl.owner().equals(Hook.PLAYER.getInternalName())) {
                Hook.NPC_DEFINITION.put(new RSField(lvl, "combatLevel"));
            }
        }
    }
}
