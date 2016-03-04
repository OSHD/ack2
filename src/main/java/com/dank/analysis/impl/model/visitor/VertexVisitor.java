package com.dank.analysis.impl.model.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project:
 * Time: 01:58
 * Date: 15-02-2015
 * Created by Dogerina.
 */
public class VertexVisitor extends TreeVisitor {

    public VertexVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(new MemberQuery("[I")) == 3 && block.weight() < 30;
    }

    @Override
    public void visitField(final FieldMemberNode fmn) {
        if (fmn.opcode() == GETFIELD) {
            if (Hook.MODEL.get("verticesX") == null) {
                Hook.MODEL.put(new RSField(fmn, "verticesX"));
            } else if (Hook.MODEL.get("verticesY") == null) {
                Hook.MODEL.put(new RSField(fmn, "verticesY"));
            } else if (Hook.MODEL.get("verticesZ") == null) {
                Hook.MODEL.put(new RSField(fmn, "verticesZ"));
            }
        }
    }
}
