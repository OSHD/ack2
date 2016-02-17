package com.dank.analysis.impl.definition.npc.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: RS3Injector
 * Time: 22:05
 * Date: 09-02-2015
 * Created by Dogerina.
 */
public class TempTransformVisitor extends TreeVisitor {

    public TempTransformVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return true;
    }

    @Override
    public void visitField(final FieldMemberNode fmn) {
        if (fmn.opcode() == GETSTATIC && fmn.desc().equals("[I") && fmn.parent().first(IMUL) != null) {
            for (final AbstractNode an : fmn.parent().first(IMUL)) {
                if (an.opcode() == GETFIELD) {
                    Hook.NPC_DEFINITION.put(new RSField((FieldMemberNode) an, "varp32Index"));
                    Hook.CLIENT.put(new RSField(fmn, "tempVars"));
                }
            }
        }
    }
}
