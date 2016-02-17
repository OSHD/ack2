package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: RS3Injector
 * Time: 00:40
 * Date: 09-02-2015
 * Created by Dogerina.
 */
public class AudioEffectCountVisitor extends TreeVisitor {

    public AudioEffectCountVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public void visitNumber(NumberNode nn) {
        if (nn.number() == 50 && nn.parent().opcode() == IF_ICMPGE) {
            final JumpNode jn = (JumpNode) nn.parent();
            if (jn.hasChild(IMUL)) {
                final FieldMemberNode fmn = jn.child(0).firstField();
                if (fmn != null) {
                    Hook.CLIENT.put(new RSField(fmn, "audioEffectCount"));
                }
            }
        }
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.get(INVOKESTATIC) == null;
    }
}
