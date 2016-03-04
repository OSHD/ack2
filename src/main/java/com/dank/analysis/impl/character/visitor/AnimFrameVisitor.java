package com.dank.analysis.impl.character.visitor;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Created by RSynapse on 1/17/2016.
 */
public class AnimFrameVisitor extends TreeVisitor {

    public AnimFrameVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return Modifier.isFinal(block.owner.access) && Modifier.isProtected(block.owner.access) && new Wildcard("()L*;").matches(block.owner.desc);
    }

    @Override
    public void visitMethod(MethodMemberNode mmn) {
        if (mmn.opcode() == INVOKEVIRTUAL && new Wildcard("(L*;?L*;I?)L*;").matches(mmn.desc())) {
            final List<AbstractNode> multiplications = mmn.findChildren(IMUL);
            if (multiplications != null && multiplications.size() == 2) {// && mmn.children() == 2
                for (final AbstractNode imul : mmn) {
                    for (final AbstractNode child : imul) {
                        if (child.opcode() == GETFIELD && ((FieldMemberNode) child).owner().equals(Hook.PLAYER.getInternalName())) {
                            FieldMemberNode current = (FieldMemberNode) child;
                            current.fin().owner = Hook.CHARACTER.getInternalName();
                            if(Hook.CHARACTER.get("animFrameId") == null) {
                                Hook.CHARACTER.put(new RSField((FieldMemberNode) child, "animFrameId"));
//                                System.out.println(Multiplier.getMultiple("mult>");
//                                System.out.println("mult : " + Multiplier.getMultiple(current.fin().owner + "." + current.fin().name));
//                                System.out.println("mult" + Multiplier.getMultiple( Hook.CHARACTER.getInternalName() + "." + current.fin().name));

//                                Hook.CHARACTER.get("animFrameId").setMultiplier(multiplicatio);
                            }
                            else if(Hook.CHARACTER.get("interAnimFrameId") == null) {
                                Hook.CHARACTER.put(new RSField((FieldMemberNode) child, "interAnimFrameId"));
                            }
                        }
                    }
                }
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
