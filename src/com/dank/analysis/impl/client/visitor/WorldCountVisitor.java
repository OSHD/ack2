package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 22:10
 * Date: 12-02-2015
 * Created by Dogerina.
 */
public class WorldCountVisitor extends TreeVisitor {

    public WorldCountVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(new MemberQuery(Hook.CLIENT.get("worlds"))) > 0 && block.count(new MemberQuery("I")) > 0;
    }

    @Override
    public void visitOperation(final ArithmeticNode an) {
        if (an.opcode() != IMUL || !an.hasChild(GETSTATIC)) {
            return;
        }
        FieldMemberNode fmn = an.firstField();
        if (fmn.desc().equals("I")) {
            Hook.CLIENT.put(new RSField(fmn, "worldCount"));
        }
    }
}
