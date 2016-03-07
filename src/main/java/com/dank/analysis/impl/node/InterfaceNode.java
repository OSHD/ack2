package com.dank.analysis.impl.node;

import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.TypeNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 23:05
 * Date: 12-02-2015
 * Created by Dogerina.
 */
public class InterfaceNode extends Analyser implements Opcodes {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName.equals(Hook.NODE.getInternalName()) && cn.fieldCount(int.class) == 2 && cn.fieldCount(boolean.class) == 1
                ? new ClassSpec(Hook.INTERFACE_NODE, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        getClassPath().getClasses().forEach(c -> c.methods.forEach(m -> m.graph().forEach(b -> {
            if (b.count(ALOAD) > 0 && b.count(IMUL) > 0 && b.count(GETFIELD) > 0) {
                b.tree().accept(new NodeVisitor() {
                    @Override
                    public void visitJump(JumpNode jn) {
                        final FieldMemberNode fmn = (FieldMemberNode) jn.layer(IMUL, GETFIELD);
                        if (fmn != null && fmn.owner().equals(cn.name) && fmn.children() == 1 && fmn.first(ALOAD) != null) {
                            Hook.INTERFACE_NODE.put(new RSField(fmn, "type"));
                        }
                    }
                });
            } else if (b.count(new MemberQuery(GETSTATIC, "L" + Hook.NODETABLE.getInternalName() + ";")) == 1) {
                b.tree().accept(new NodeVisitor() {
                    @Override
                    public void visitType(TypeNode tn) {
                        if (tn.hasPrevious() && tn.type().equals(cn.name)) {
                            final FieldMemberNode fmn = tn.previous().firstField();
                            if (fmn != null) {
                                Hook.CLIENT.put(new RSField(fmn, "interfaceNodes"));
                            }
                        }
                    }
                });
            }
        })));
        for (final FieldNode fn : cn.fields) {
            if (!Modifier.isStatic(fn.access) && !fn.name.equals(Hook.INTERFACE_NODE.get("type").name) && fn.desc.contains("I")) {
                Hook.INTERFACE_NODE.put(new RSField(fn, "owner"));
            }
        }
    }
}
