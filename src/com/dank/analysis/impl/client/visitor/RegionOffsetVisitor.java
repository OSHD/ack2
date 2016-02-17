package com.dank.analysis.impl.client.visitor;

import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.NumberQuery;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 23:12
 * Date: 14-02-2015
 * Created by Dogerina.
 */
public class RegionOffsetVisitor extends TreeVisitor {

    public RegionOffsetVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(ISHL) > 0 && block.count(new NumberQuery(BIPUSH, 7)) > 0 && block.count(INVOKESTATIC) > 0;
    }

    @Override
    public void visitMethod(final MethodMemberNode mmn) {
        final List<AbstractNode> results = mmn.layerAll(IADD, ISHL, ISUB, IMUL, GETSTATIC);
        if (results != null) {
            for (final AbstractNode an : results) {
                final FieldMemberNode fmn = (FieldMemberNode) an;
                if (Hook.CLIENT.get("hintArrowX") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "hintArrowX"));
                } else if (Hook.CLIENT.get("regionBaseX") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "regionBaseX"));
                } else if (Hook.CLIENT.get("hintArrowY") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "hintArrowY"));
                } else if (Hook.CLIENT.get("regionBaseY") == null) {
                    Hook.CLIENT.put(new RSField(fmn, "regionBaseY"));
                }
            }
        }
    }
}
