package com.dank.analysis.impl.character.player;

import org.objectweb.asm.commons.cfg.tree.NodeTree;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.character.player.visitor.BufferVisitor;
import com.dank.analysis.impl.character.player.visitor.CombatLevelVisitor;
import com.dank.analysis.impl.character.player.visitor.TeamVisitor;
import com.dank.analysis.impl.character.player.visitor.TotalLevelVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.MemberKey;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 16:39
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class Player extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(Hook.PLAYER_CONFIG) == 1 ? new ClassSpec(Hook.PLAYER, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (!fn.isStatic()) {
                if (fn.desc.equals(Hook.PLAYER_CONFIG.getInternalDesc())) {
                    Hook.PLAYER.put(new RSField(fn, "config"));
                } else if (fn.desc.equals("Ljava/lang/String;")) {
                    Hook.PLAYER.put(new RSField(fn, "name"));
                }
            }
        }
        for (final MethodNode mn : cn.methods) { //pull all the fields set in the buffer first
            if (!mn.isStatic()) {
                if (mn.desc.startsWith("(" + Hook.BUFFER.getInternalDesc()) && mn.isVoid()) {
                    mn.graph().forEach(b -> {
                        final NodeTree tree = b.tree();
                        tree.accept(new BufferVisitor(b));
                        tree.accept(new TeamVisitor());
                    });
                } else if (mn.desc.startsWith("(IIB")) {
                    mn.graph().forEach(b -> b.tree().accept(new Animation()));
                    mn.graph().forEach(b -> b.tree().accept(new QueueSize()));
                }
            }
        }
        //find the fields
        getClassPath().forEach((name, c) -> c.methods.forEach(m -> m.graph().forEach(b -> {
            b.tree().accept(new CombatLevelVisitor(b));
            b.tree().accept(new TotalLevelVisitor());
        })));
    }

    private class Animation extends NodeVisitor {

        @Override
        public void visitJump(JumpNode jn) {
            if (jn.isOpcode(IF_ICMPEQ) && jn.layer(ICONST_M1) != null) {
                FieldMemberNode fmn = (FieldMemberNode) jn.layer(IMUL, GETFIELD);
                if (fmn != null) {
                    ClassNode cn = getClassPath().get(spec.getName());
                    if (cn != null) {
                        RSField rsf = new RSField(new MemberKey(cn.superName, fmn.name(), fmn.desc()), "animation");
                        Hook.CHARACTER.put(rsf);
                    }
                }
            }
        }
    }

    private class QueueSize extends NodeVisitor {

        @Override
        public void visitNumber(final NumberNode nn) {
            if (nn.hasParent() && nn.parent() instanceof JumpNode) {
                FieldMemberNode fmn = (FieldMemberNode) nn.parent().layer(IMUL, GETFIELD);
                if (fmn != null && nn.number() == 9) {
                    ClassNode cn = getClassPath().get(spec.getName());
                    if (cn != null) {
                        RSField rsf = new RSField(new MemberKey(cn.superName, fmn.name(), fmn.desc()), "queueSize");
                        Hook.CHARACTER.put(rsf);
                    }
                }
            }
        }
    }
}
