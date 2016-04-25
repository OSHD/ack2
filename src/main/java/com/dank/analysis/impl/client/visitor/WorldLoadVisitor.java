package com.dank.analysis.impl.client.visitor;

import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.DankEngine;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;

/**
 * Project: DankWise
 * Time: 21:53
 * Date: 12-02-2015
 * Created by Dogerina.
 */
public class WorldLoadVisitor extends TreeVisitor {

    private static final AtomicInteger index = new AtomicInteger(0); //static because this MAY need to visit multiple blocks

    public WorldLoadVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        if (block.owner.desc.contains(";")) {
            return false;
        }
        AbstractInsnNode a = block.get(AASTORE);
        return a != null && a.previous().opcode() == DUP_X2 && a.previous().previous().opcode() == INVOKESPECIAL;
    }

    @Override
    public void visitField(FieldMemberNode fmn) {
        if (fmn.isStatic() && fmn.desc().startsWith("[L") && fmn.next().child(0).child(0).opcode() != SIPUSH) {
            Hook.CLIENT.put(new RSField(fmn, "worlds"));
            Hook.CLIENT.put(new RSMethod(fmn.method(), "loadWorlds"));
        }
        if (Hook.WORLD.getInternalName() != null && fmn.owner().equals(Hook.WORLD.getInternalName())) {
            MethodNode method = DankEngine.mGraph.getCaller(fmn.tree().method());
            if (method != null) {
            	for (BasicBlock block0 : method.graph()) {
                    FieldInsnNode k = (FieldInsnNode) block0.get(new MemberQuery(PUTSTATIC, "Z"));
                    if (k != null) {
                        Hook.CLIENT.put(new RSField(k, "worldSelectorDisplayed"));
                        break;
                    }
                }
            }
            
        }
    }
}
