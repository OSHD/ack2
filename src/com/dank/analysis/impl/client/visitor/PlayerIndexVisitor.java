package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: RS3Injector
 * Time: 00:49
 * Date: 09-02-2015
 * Created by Dogerina.
 */
public class PlayerIndexVisitor extends TreeVisitor {

    public PlayerIndexVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public void visitOperation(ArithmeticNode an) {
        if (an.isOpcode(ISHL)) {
            final NumberNode eight = (NumberNode) an.layer(BIPUSH);
            if (eight != null && eight.number() == 8) {
                final FieldMemberNode fmn = (FieldMemberNode) an.layer(IMUL, GETSTATIC);
                if (fmn != null) {
                    Hook.CLIENT.put(new RSField(fmn, "myPlayerIndex"));
                }
            }
        }
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(GETSTATIC) != 0 && block.count(PUTSTATIC) != 0;
    }
}
