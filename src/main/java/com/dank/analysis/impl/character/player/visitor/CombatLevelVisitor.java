package com.dank.analysis.impl.character.player.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 16:52
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class CombatLevelVisitor extends TreeVisitor {

    public CombatLevelVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) { //check that this block references localPlayer and an int field
        return block.count(new MemberQuery(GETSTATIC, Hook.PLAYER.getInternalDesc())) > 0
                && block.count(new MemberQuery(GETFIELD, Hook.PLAYER.getInternalName(), "I")) > 0;
    }

    @Override
    public void visitField(final FieldMemberNode fmn) {
        if (fmn.opcode() == GETFIELD && fmn.desc().equals("I")) {
            for (final FieldMemberNode _fmn : BufferVisitor.SETS) {
                if (_fmn.key().equals(fmn.key())) { //check that the field was read from the buffer
                    Hook.PLAYER.put(new RSField(_fmn, "combatLevel"));
                }
            }
        }
    }
}
