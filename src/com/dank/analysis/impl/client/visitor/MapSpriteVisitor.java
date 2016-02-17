package com.dank.analysis.impl.client.visitor;

import com.dank.DankEngine;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Project: RS3Injector
 * Time: 19:25
 * Date: 10-02-2015
 * Created by Dogerina.
 */
public class MapSpriteVisitor extends TreeVisitor {

    public MapSpriteVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(SIPUSH) > 1;
    }

    @Override
    public void visitNumber(final NumberNode nn) {
        if (nn.number() == 512 && nn.hasNext() && nn.next().opcode() == SIPUSH && nn.nextNumber().number() == 512
                && nn.parent().parent().opcode() == PUTSTATIC) {
            final FieldMemberNode fmn = (FieldMemberNode) nn.parent().parent();
            if (fmn.desc().startsWith("L")) {

                Hook.SPRITE.setInternalName(fmn.type());
                Hook.CLIENT.put(new RSField(fmn, "minimapSprite"));

                for (final ClassNode cn : DankEngine.classPath) {
                    for (final MethodNode mn : cn.methods) {
                        if (mn.desc.startsWith("(IIIIIZ") && mn.desc.endsWith(")" + fmn.desc())) {
                            Hook.CLIENT.put(new RSMethod(mn, "getItemSprite"));
                        }

                    }
                }
            }
        }
    }
}
