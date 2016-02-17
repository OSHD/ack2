package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 27-02-2015
 * Time: 00:28
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class DestinationVisitor2 extends TreeVisitor {

    public DestinationVisitor2(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock b) {
        return b.count(PUTSTATIC) >= 1;
    }

    @Override
    public void visitField(FieldMemberNode fmn) {
        if (fmn.opcode() == PUTSTATIC && fmn.desc().equals(Hook.SPRITE.getInternalArrayDesc())
                && fmn.firstNumber() != null && fmn.firstNumber().number() == 1000 && fmn.first(ANEWARRAY) != null) {
            if (block.count(PUTSTATIC) == 1 && follow(block.next))  //ghetto as fuck
                return;
            FieldMemberNode destX = fmn.nextField();
            if (destX == null || !destX.desc().equals("I"))
                return;
            FieldMemberNode destY = destX.nextField();
            if (destY == null || !destY.desc().equals("I"))
                return;
            
            System.out.println(fmn.method().key());
            
            Hook.CLIENT.put(new RSField(destX, "destinationX"));
            Hook.CLIENT.put(new RSField(destY, "destinationY"));
        }
    }

    private boolean follow(BasicBlock block) {
        if (block == null)
            return false;
        for (AbstractInsnNode ain : block.instructions) {
            if (ain.opcode() == PUTSTATIC) {
                FieldInsnNode fin = (FieldInsnNode) ain;
                if (!fin.desc.equals("I"))
                    continue;
                if (Hook.CLIENT.get("destinationX") == null) {
                    Hook.CLIENT.put(new RSField(fin, "destinationX"));
                    return follow(block.next);
                } else if (Hook.CLIENT.get("destinationY") == null) {
                    Hook.CLIENT.put(new RSField(fin, "destinationY"));
                    return true;
                }
            }
        }
        return follow(block.next);
    }
}
