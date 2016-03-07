package com.dank.analysis.impl.node.graphics;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.character.visitor.HitsplatVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.BlockVisitor;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.*;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;

/**
 * Created by RSynapse on 2/20/2016.
 */
public class Graphics extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.name.equals(Hook.SPRITE.resolve().superName) ? new ClassSpec(Hook.GRAPHICS, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (MethodNode methodNode : cn.methods) {
            if (new Wildcard("([III)V").matches(methodNode.desc) && Modifier.isStatic(methodNode.access)) {
                TreeBuilder.build(methodNode).accept(new FieldVisitor());
            }
        }
    }

    public class FieldVisitor extends NodeVisitor {

        @Override
        public void visitField(FieldMemberNode fmn) {

            if (fmn.desc().equals("[I")) {
                Hook.GRAPHICS.put(new RSField(fmn, "raster"));
            }

            if(fmn.desc().equals("I")) {
                VariableNode variable = (VariableNode) fmn.layer(ILOAD);
                if(variable.var() == 1) {
                    Hook.GRAPHICS.put(new RSField(fmn, "rasterWidth"));
                }
                if(variable.var() == 2) {
                    Hook.GRAPHICS.put(new RSField(fmn, "rasterHeight"));
                }
            }
        }
    }
}
