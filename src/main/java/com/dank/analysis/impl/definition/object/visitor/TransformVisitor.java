package com.dank.analysis.impl.definition.object.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: RS3Injector
 * Time: 22:28
 * Date: 09-02-2015
 * Created by Dogerina.
 */
public class TransformVisitor extends TreeVisitor {

    public TransformVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return true;
    }

    @Override
    public void visitField(final FieldMemberNode fmn) {
        if (fmn.opcode() == GETFIELD) {
            if (fmn.desc().equals("I") && !fmn.name().equals(Hook.OBJECT_DEFINITION.get("varp32Index").name)) {
                Hook.OBJECT_DEFINITION.put(new RSField(fmn, "varpIndex"));
            } else if (fmn.desc().equals("[I")) {
                Hook.OBJECT_DEFINITION.put(new RSField(fmn, "transformIds"));
            }
        }
    }
}
