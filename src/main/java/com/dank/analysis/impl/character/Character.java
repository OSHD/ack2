package com.dank.analysis.impl.character;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.character.visitor.*;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.NumberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;

/**
 * Project: DankWise
 * Time: 00:17
 * Date: 14-02-2015
 * Created by Dogerina.
 */
public class Character extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.name.equals(Hook.CHARACTER.getInternalName()) ? new ClassSpec(Hook.CHARACTER, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (Modifier.isStatic(fn.access)) continue;
            if (fn.desc.equals("Ljava/lang/String;")) {
                Hook.CHARACTER.put(new RSField(fn, "overheadText"));
            } else if (fn.desc.equals("[Z")) {
                Hook.CHARACTER.put(new RSField(fn, "queueTraversed"));
            }
        }
        for (final MethodNode mn : cn.methods) {
            if (Modifier.isStatic(mn.access)) continue;
            TreeBuilder.build(mn).accept(new HitsplatVisitor());
        }

        Hook.PLAYER.resolve().methods.forEach(m -> m.graph().forEach(b -> {
            b.tree().accept(new AnimFrameVisitor(b));

        }));

        getClassPath().forEach((name, c) -> c.methods.forEach(m -> m.graph().forEach(b -> {
            b.tree().accept(new HealthCycleVisitor(b));
            b.tree().accept(new PositionVisitor(b));
            b.tree().accept(new HeightVisitor(b));
            b.tree().accept(new InterAnimFrameVisitor(b));
        })));
    }

    public void resolveAnimationMultipliers() {
        MethodNode methodNode = Inter.runAnimationMethod;

        //find multi in line with field -- that's the solution.
        //also... the inverse of the getter is equal to the setter...
        //so if you find the getter you also found the setter....

        //Using the interpreter? using modulas inverse ummmm
    }

    private int pos = 0;

    private final class PositionVisitor extends TreeVisitor {

        public PositionVisitor(BasicBlock block) {
            super(block);
        }

        @Override
        public boolean validateBlock(BasicBlock block) {
            return block.count(new NumberQuery(SIPUSH, 13184)) > 0 && pos < 2;
        }

        public void visitJump(JumpNode jn) {
            String name = null;
            if (jn.opcode() == IF_ICMPGE) {
                name = "strictX";
            } else if (jn.opcode() == IF_ICMPLT) {
                name = "strictY";
            }
            if (name != null && Hook.CHARACTER.get(name) == null) {
                FieldMemberNode fmn = (FieldMemberNode) jn.layer(IMUL, GETFIELD);
                if (fmn == null) {
                    return;
                }
                Hook.CHARACTER.put(new RSField(fmn, name));
                pos++;
            }
        }
    }
}
