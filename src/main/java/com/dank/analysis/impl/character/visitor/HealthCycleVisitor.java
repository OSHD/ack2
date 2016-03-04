package com.dank.analysis.impl.character.visitor;

import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;

import com.dank.analysis.visitor.MultiplierVisitor;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 19:32
 * Date: 10-02-2015
 * Created by Dogerina.
 */
public class HealthCycleVisitor extends TreeVisitor {

    public HealthCycleVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.owner.desc.startsWith("(" + Hook.CHARACTER.getInternalDesc())
                && block.owner.desc.contains("IIII") && block.count(IADD) == 0
                || block.count(new MemberQuery(PUTFIELD, Hook.CHARACTER.getInternalName(), "I")) > 0;
    }

    @Override
    public void visitField(final FieldMemberNode fmn) {
        if (fmn.opcode() == PUTFIELD && fmn.hasChild(LDC) && fmn.firstNumber() != null && fmn.owner().equals(Hook.CHARACTER.getInternalName())) {
            final int encoded = fmn.firstNumber().number();
            final int multi = MultiplierVisitor.getDecoder(fmn.key()) * encoded;
            if (multi == -1000) {
                Hook.CHARACTER.put(new RSField(fmn, "healthBarCycle"));
            }
        }
    }

    @Override
    public void visitJump(final JumpNode jn) {
        if (Hook.CHARACTER.get("healthBarCycle") != null)
            return;
        final List<AbstractNode> multiplications = jn.findChildren(IMUL);
        if (multiplications != null && multiplications.size() == 2 && jn.children() == 2) {
            for (final AbstractNode imul : jn) {
                for (final AbstractNode child : imul) {
                    if (child.opcode() == GETSTATIC) {
                        //Hook.CLIENT.put(new RSField((FieldMemberNode) child, "engineCycle"));
                    } else if (child.opcode() == GETFIELD && ((FieldMemberNode) child).owner().equals(Hook.CHARACTER.getInternalName())) {
                        Hook.CHARACTER.put(new RSField((FieldMemberNode) child, "healthBarCycle"));
                    }
                }
            }
        }
    }
}
