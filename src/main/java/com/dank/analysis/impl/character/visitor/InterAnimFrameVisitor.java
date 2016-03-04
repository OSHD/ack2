package com.dank.analysis.impl.character.visitor;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.FlowVisitor;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;

/**
 * Created by RSynapse on 1/17/2016.
 */
public class InterAnimFrameVisitor extends TreeVisitor {


    public static MethodNode interAnimFrameVisitor;

    private FieldMemberNode current;

    public InterAnimFrameVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return Modifier.isFinal(block.owner.access) && Modifier.isStatic(block.owner.access) &&
                new Wildcard("(L" + Hook.CHARACTER.getInternalName() + ";I?)V").matches(block.owner.desc);
    }

    @Override
    public void visitMethod(MethodMemberNode mmn) {
        if (mmn.opcode() == INVOKESTATIC && new Wildcard("(I?)L*;").matches(mmn.desc())) {

//            final List<AbstractNode> multiplications = mmn.findChildren(IMUL);
//
//            if (multiplications != null && multiplications.size() == 1) {// && mmn.children() == 2
//                for (final AbstractNode imul : mmn) {
//                    for (final AbstractNode child : imul) {
//                        if (child.opcode() == GETFIELD && ((FieldMemberNode) child).owner().equals(Hook.CHARACTER.getInternalName())) {
//                            current = (FieldMemberNode) child;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
            if (current != null) {
                FlowVisitor visitor = new FlowVisitor();
                visitor.accept(mmn.method());
                visitor.blocks.stream().filter(b -> b.count(new MemberQuery(Opcodes.PUTFIELD, current.owner(), "I")) == 1 &&
                        b.count(new MemberQuery(Opcodes.GETFIELD, current.owner(), "I")) == 1).forEach(b -> {
                    FieldMemberNode fmn = (FieldMemberNode) b.tree().first(Opcodes.PUTFIELD);
                    if (fmn != null && fmn.name().equals(current.name())) {
                        if (Hook.CHARACTER.get("interAnimId") == null && Hook.CHARACTER.get("animation").name != current.name()) {
                            Hook.CHARACTER.put(new RSField(current, "interAnimId"));
                            interAnimFrameVisitor = block.owner;
                        }
                    }
                });
            }
        }
    }
/*
ASTORE #3
		INVOKEVIRTUAL fc.j (Laf;ILaf;II)Ldt;
			GETFIELD c.q Lfc;
				ALOAD #0
			ALOAD #1
			IMUL
				GETFIELD c.bo I
					ALOAD #0
				LDC java.lang.Integer -1125691628
			ALOAD #2
			IMUL
				LDC java.lang.Integer -622310993
				GETFIELD c.bc I
					ALOAD #0
			LDC java.lang.Integer -988136875
 */

}
