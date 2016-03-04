package com.dank.analysis.impl.client.visitor;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;

import com.dank.DankEngine;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 18-02-2015
 * Time: 02:14
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class SelectedItemIndexVisitor extends TreeVisitor {

    private static final List<FieldMemberNode> results = new ArrayList<>();

    public SelectedItemIndexVisitor(BasicBlock block) {
        super(block);
    }

    public static void onEnd() {
        DankEngine.classPath.forEach((name, cn) -> cn.methods.forEach(m -> m.graph().forEach(b -> b.tree().accept(new NodeVisitor() {
            @Override
            public void visitJump(JumpNode jn) {
                if (jn.layer(ILOAD) != null && (jn.opcode() == IF_ICMPNE || jn.opcode() == IF_ICMPEQ)) {
                    final FieldMemberNode _fmn = (FieldMemberNode) jn.layer(IMUL, GETSTATIC);
                    for (final FieldMemberNode fmn : results) {
                        if (_fmn != null && _fmn.key().equals(fmn.key())) {
                            Hook.CLIENT.put(new RSField(_fmn, "selectedItemIndex"));
                        }
                    }
                }
            }
        }))));
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.owner.desc.startsWith("(IIIILjava/lang/String;Ljava/lang/String;II");
    }

    @Override
    public void visitMethod(final MethodMemberNode mmn) {
        final FieldMemberNode packet = mmn.firstField();
        if (packet != null && packet.desc().equals(Hook.PACKET_BUFFER.getInternalDesc())) {
            final FieldMemberNode fmn = (FieldMemberNode) mmn.layer(IMUL, GETSTATIC);
            if (fmn != null) {
                Hook.CLIENT.put(new RSField(packet, "packet"));
                results.add(fmn);
            }
        }
    }
}
