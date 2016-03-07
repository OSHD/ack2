package com.dank.analysis.impl.client.visitor;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.*;

import java.awt.*;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Created by Greg on 3/5/2016.
 */
public class CurrentNameVisitor extends TreeVisitor {

    public CurrentNameVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return Modifier.isStatic(block.owner.access) &&
                new Wildcard("(L*;L*;L*;Z?)V").matches(block.owner.desc);
    }

    @Override
    public void visitField(FieldMemberNode fmn) {
        if (fmn.desc().equals(Type.getDescriptor(String.class))) {
            AbstractNode decider = fmn.preLayer(Opcodes.ASTORE);
            if(decider != null) {
                Hook.CLIENT.put(new RSField(fmn, "currentLoginName"));
            }
        }
    }
}
