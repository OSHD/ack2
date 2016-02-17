package com.dank.analysis.impl.character.visitor;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.query.NumberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

/**
 * Created by RSynapse on 1/15/2016.
 */
public class HeightVisitor extends TreeVisitor {

    public HeightVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return new Wildcard("(L" + Hook.CHARACTER.getInternalName() + ";IIIII?)V").matches(block.owner.desc) &&
                block.count(new NumberQuery(Opcodes.BIPUSH, 15)) > 0 &&
                block.count(new MemberQuery(Opcodes.GETFIELD, Hook.CHARACTER.getInternalName(), "I")) > 0;
    }

    @Override
    public void visitField(FieldMemberNode fmn) {

        if (fmn.opcode() == Opcodes.GETFIELD && fmn.owner().equals(Hook.CHARACTER.getInternalName())) {
            Hook.CHARACTER.put(new RSField(fmn, "modelHeight"));
        }
    }
}
