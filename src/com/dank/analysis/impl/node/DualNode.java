package com.dank.analysis.impl.node;

import java.lang.reflect.Modifier;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.FlowVisitor;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;

/**
 * Project: RS3Injector
 * Time: 06:31
 * Date: 07-02-2015
 * Created by Dogerina.
 */
public class DualNode extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName.equals(Hook.NODE.getInternalName()) && cn.fieldCount('L' + cn.name + ';') == 2 ? new ClassSpec(Hook.DUAL_NODE, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final MethodNode mn : cn.methods) {
            if (Modifier.isStatic(mn.access)) continue;
            for (final BasicBlock block : new FlowVisitor(mn).graph) {
                block.tree().accept(new NodeVisitor() {
                    @Override
                    public void visitJump(JumpNode jn) {
                        if (jn.opcode() != IFNONNULL) return;
                        final FieldMemberNode fmn = jn.firstField();
                        if (fmn != null && Hook.DUAL_NODE.get("dualPrevious") == null) {
                            Hook.DUAL_NODE.put(new RSMethod(mn, "unlinkDual"));
                            Hook.DUAL_NODE.put(new RSField(fmn, "dualPrevious"));
                        }
                    }
                });
            }
        }
        for (final FieldNode fn : cn.fields) {
            if (!fn.name.equals(Hook.DUAL_NODE.get("dualPrevious").name)) {
                Hook.DUAL_NODE.put(new RSField(fn, "dualNext"));
            }
        }
    }
}
