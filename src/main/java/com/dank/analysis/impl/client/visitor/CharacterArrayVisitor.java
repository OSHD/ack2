package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ArrayLoadNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.tree.ClassNode;

import com.dank.DankEngine;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: RS3Injector
 * Time: 20:04
 * Date: 09-02-2015
 * Created by Dogerina.
 */
public class CharacterArrayVisitor extends TreeVisitor {

    public CharacterArrayVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return true;
    }

    @Override
    public void visitArrayLoad(final ArrayLoadNode an) {
        if (an.opcode() == AALOAD && an.array().opcode() == GETSTATIC && an.elementIndex().opcode() == IALOAD) {
            final FieldMemberNode array = (FieldMemberNode) an.array();
            for (final AbstractNode child : an.elementIndex()) {
                if (child.opcode() == GETSTATIC) {
                    final FieldMemberNode indices = (FieldMemberNode) child;
                    if (indices.desc().equals("[I")) {
                        final ClassNode arrayType = DankEngine.classPath.get(array.type());
                        if (arrayType.fieldCount("I") == 0 && Hook.NPC.getInternalName() == null) {
                            Hook.CHARACTER.setInternalName(arrayType.superName);
                            Hook.NPC.setInternalName(arrayType.name);
                            Hook.CLIENT.put(new RSField(indices, "npcIndices"));
                            Hook.CLIENT.put(new RSField(array, "npcArray"));
                        } else if (arrayType.fieldCount("I") > 0 && Hook.PLAYER.getInternalName() == null) {
                            Hook.PLAYER.setInternalName(arrayType.name);
                            //indices broken because it gets stored to local var first, cba to fix
                            Hook.CLIENT.put(new RSField(indices, "playerIndices"));
                            Hook.CLIENT.put(new RSField(array, "playerArray"));
                        }
                    }
                }
            }
        }
    }
}
