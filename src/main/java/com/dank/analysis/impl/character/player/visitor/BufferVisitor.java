package com.dank.analysis.impl.character.player.visitor;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 16:46
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class BufferVisitor extends TreeVisitor {

    /* The fields that were set while reading from the buffer */
    public static final List<FieldMemberNode> SETS = new ArrayList<>();

    public BufferVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(ALOAD) > 0;
    }

    @Override
    public void visitField(final FieldMemberNode fmn) {
        if (fmn.opcode() == PUTFIELD && fmn.desc().equals("I")) {
            SETS.add(fmn);
        }
    }

    @Override
    public void visitMethod(MethodMemberNode mmn) {
        if (mmn.key().equals(Hook.BUFFER.get("readByte").key())) {
            final FieldMemberNode fmn = (FieldMemberNode) mmn.preLayer(IMUL, PUTFIELD);
            if (fmn != null) {
                if (Hook.PLAYER.get("skullIcon") == null) {
                    Hook.PLAYER.put(new RSField(fmn, "skullIcon"));
                } else if (Hook.PLAYER.get("prayerIcon") == null) {
                    Hook.PLAYER.put(new RSField(fmn, "prayerIcon"));
                }
            }
        }
    }
}
