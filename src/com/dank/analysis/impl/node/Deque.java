package com.dank.analysis.impl.node;

import java.lang.reflect.Modifier;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * @author Septron
 * @since February 12, 2015
 */
public class Deque extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.ownerless() && cn.getFieldTypeCount() == 1 && cn.fieldCount(String.format("L%s;", Hook.NODE.getInternalName())) == 2 ? new ClassSpec(Hook.DEQUE, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (Modifier.isStatic(mn.access)) continue;
            for (BasicBlock block : mn.graph()) {
                block.tree().accept(new NodeVisitor() {
                    @Override
                     public void visitField(FieldMemberNode fmn) {
                        if (fmn.getting()) {
                            if (fmn.fin().owner.equals(cn.name) && fmn.fin().desc.equals(String.format("L%s;", Hook.NODE.getInternalName()))) {
                                Hook.DEQUE.put(new RSField(fmn, "head"));
                            }
                        }
                    }
                });
            }
            cn.fields.stream().filter(fn -> fn.desc.equals(String.format("L%s;", Hook.NODE.getInternalName()))).filter(fn -> !fn.name.equals(Hook.DEQUE.get("head").name)).forEach(fn -> Hook.DEQUE.put(new RSField(fn, "tail")));
        }
    }
}
