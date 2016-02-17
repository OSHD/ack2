package com.dank.analysis.impl.node;

import java.lang.reflect.Modifier;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ConversionNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * @author Septron
 * @since February 12, 2015
 */
public class HashTable extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount("I") == 2 && cn.fieldCount('L' + Hook.NODE.getInternalName()+ ';') == 2 && cn.ownerless() ? new ClassSpec(Hook.NODETABLE, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (Modifier.isStatic(mn.access) || !mn.desc.startsWith("(J")) continue;
            for (BasicBlock block : mn.graph()) {
                block.tree().accept(new NodeVisitor() {
                    @Override
                    public void visitConversion(ConversionNode cn) {
                        if (cn.opcode() != I2L || cn.children() != 1 || !cn.hasChild(ISUB)) return;
                        final FieldMemberNode fmn = (FieldMemberNode) cn.layer(ISUB, GETFIELD);
                        if (fmn != null && fmn.desc().equals("I")) {
                            Hook.NODETABLE.put(new RSField(fmn, "index"));
                        }
                    }
                });
            }
        }

        for (FieldNode fn : cn.fields) {
            if (fn.desc.equals(String.format("[L%s;", Hook.NODE.getInternalName()))) {
                Hook.NODETABLE.put(new RSField(fn, "buckets"));
            }
            if (fn.desc.equals("I") && !fn.name.equals(Hook.NODETABLE.get("index").name))
                Hook.NODETABLE.put(new RSField(fn, "size"));
        }
    }
}
