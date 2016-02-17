package com.dank.analysis.impl.character.visitor;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.NumberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

/**
 * @author Septron
 * @since February 14, 2015
 */
public class OrientationVisitor extends TreeVisitor {


    // int local = (Character.objectOrientation - Character.orientation) & 2047;
    // if(local > 1024) {
    //      local -= 2048
    // }

    /**
     *
     * let A = oo
     * let B = o;
     *
     * int A_LOCAL = (C.oo - C.o) & 2047;
     * if(A_LOCAL > 1024) {
     *     A_LOCAL -= 2048;
     * }
     *
     */


    public OrientationVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(new NumberQuery(SIPUSH, 2048)) > 0 && block.count(LDC) > 0;
    }

    @Override
    public void visitNumber(final NumberNode nn) {

        if (nn.parent().opcode() == ISUB) {
            final FieldMemberNode fmn = (FieldMemberNode) nn.parent().layer(IMUL, GETFIELD);
            if (fmn != null && fmn.desc().equals("I")) {
                Hook.CHARACTER.put(new RSField(fmn, "orientation"));
                System.out.println("OK:" + nn.tree().method().key());
            }
        }
    }
}
