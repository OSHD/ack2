package com.dank.analysis.impl.definition.object;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.StoreNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import com.dank.DankEngine;
import com.dank.analysis.Analyser;
import com.dank.analysis.impl.definition.object.visitor.IdVisitor;
import com.dank.analysis.impl.definition.object.visitor.TempTransformVisitor;
import com.dank.analysis.impl.definition.object.visitor.TransformVisitor;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;

/**
 * Project: DankWise
 * Date: 28-02-2015
 * Time: 15:35
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class ObjectDefinition extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName(Hook.DUAL_NODE.getInternalName())
                && cn.fieldCount(boolean.class) > 3
                && cn.fieldCount(int.class) > 22 ?
                new ClassSpec(Hook.OBJECT_DEFINITION, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (!fn.isStatic()) {
                if (fn.desc.equals("Ljava/lang/String;")) {
                    Hook.OBJECT_DEFINITION.put(new RSField(fn, "name"));
                } else if (fn.desc.equals("[Ljava/lang/String;")) {
                    Hook.OBJECT_DEFINITION.put(new RSField(fn, "actions"));
                }
            }
        }
        for (final MethodNode mn : cn.methods) {
            if (!mn.isStatic()) {
                if (mn.desc.endsWith(";")) {
                    for (final BasicBlock block : mn.graph()) {
                        block.tree().accept(new ColorVisitor());
                    }
                }
                if (mn.desc.endsWith(String.format("L%s;", cn.name))) {
                    for (final BasicBlock block : mn.graph()) {
                        block.tree().accept(new TempTransformVisitor(block));
                    }
                }
                mn.graph().forEach(block -> {
                    block.tree().accept(new IdVisitor(block));
                    block.tree().accept(new ClipType());
                });
            }
        }
        for (final MethodNode mn : cn.methods) {
            if (!mn.isStatic() && mn.desc.endsWith(String.format("L%s;", cn.name))) {
                for (final BasicBlock block : mn.graph()) {
                    block.tree().accept(new TransformVisitor(block)); //requires the other transform to be done first..
                }
                Hook.OBJECT_DEFINITION.put(new RSMethod(mn, "transform"));
            }
        }
        for (final ClassNode c : super.getClassPath()) {
            for (final MethodNode mn : c.methods) {
                mn.graph().forEach(b -> {
                    b.tree().accept(new SizeVisitor(b));
                    b.tree().accept(new MapFunction());
                    b.tree().accept(new Clip());
                });
            }
        }
    }

    private final class SizeVisitor extends TreeVisitor {

        public SizeVisitor(BasicBlock block) {
            super(block);
        }

        @Override
        public boolean validateBlock(BasicBlock block) {
            return block.count(new MemberQuery(GETFIELD, Hook.OBJECT_DEFINITION.getInternalName(), "I")) > 0;
        }

        @Override
        public void visitStore(StoreNode sn) {
            if (sn.isOpcode(ISTORE)) {
                final FieldMemberNode layer = (FieldMemberNode) sn.layer(IDIV, ISUB, IMUL, GETFIELD);
                if (layer != null) {
                    final VariableNode aload = layer.firstVariable();
                    if ((aload != null && aload.var() < 11) || sn.var() < 13) {
                        if (Hook.OBJECT_DEFINITION.get("sizeX") == null) {
                            Hook.OBJECT_DEFINITION.put(new RSField(layer, "sizeX"));
                        } else if (Hook.OBJECT_DEFINITION.get("sizeY") == null) {
                            Hook.OBJECT_DEFINITION.put(new RSField(layer, "sizeY"));
                        }
                    }
                }
            }
        }
    }

    private final class ColorVisitor extends NodeVisitor {

        @Override
        public void visitMethod(MethodMemberNode mmn) {
            if (mmn.opcode() == INVOKEVIRTUAL) {
                final MethodNode mn = DankEngine.lookupMethod(mmn.owner(), mmn.name(), mmn.desc());
                /**
                 * if it does have ifnonnull then the method contains textures and modifiedTextures
                 * but we dont need textures so itsk
                 */
                if (mn != null && !contains(mn.instructions, IFNONNULL)) {
                    mmn.tree().accept(new NodeVisitor() {
                        @Override
                        public void visitField(FieldMemberNode fmn) {
                            if (fmn.desc().equals("[S")) {
                                if (Hook.OBJECT_DEFINITION.get("colors") == null) {
                                    Hook.OBJECT_DEFINITION.put(new RSField(fmn, "colors"));
                                } else if (Hook.OBJECT_DEFINITION.get("modifiedColors") == null) {
                                    Hook.OBJECT_DEFINITION.put(new RSField(fmn, "modifiedColors"));
                                }
                            }
                        }
                    });
                }
            }
        }

        private boolean contains(InsnList iList, int op) {
            for (final AbstractInsnNode ain : iList) {
                if (ain.opcode() == op) {
                    return true;
                }
            }
            return false;
        }
    }

    private class ClipType extends NodeVisitor {

        public void visitJump(JumpNode jn) {
            if (Hook.OBJECT_DEFINITION.get("clipType") == null && jn.opcode() == IFLT) {
                FieldMemberNode fmn = (FieldMemberNode) jn.layer(IMUL, GETFIELD);
                if (fmn != null && fmn.owner().equals(Hook.OBJECT_DEFINITION.getInternalName()) && fmn.desc().equals("I"))
                    Hook.OBJECT_DEFINITION.put(new RSField(fmn, "clipType"));
            }
        }
    }

    private class MapFunction extends NodeVisitor {

        @Override
        public void visitField(FieldMemberNode fmn) {
            if (Hook.OBJECT_DEFINITION.get("mapFunction") == null && fmn.opcode() == GETFIELD
                    && fmn.owner().equals(Hook.OBJECT_DEFINITION.getInternalName()) && fmn.desc().equals("I")) {
                MethodMemberNode method = fmn.firstMethod();
                if (method != null && method.desc().endsWith(Hook.OBJECT_DEFINITION.getInternalDesc())) {
                    VariableNode vn = method.firstVariable();
                    if (vn != null) {
                        Hook.OBJECT_DEFINITION.put(new RSField(fmn, "mapFunction"));
                    }
                }
            }
        }
    }

    private int clipPTR = 0;

    private class Clip extends NodeVisitor {

        private String[] hooks = {"clipped", "modelClipped"};

        public void visitField(FieldMemberNode fmn) {
            if (clipPTR < 2 && fmn.hasParent() && fmn.parent().opcode() == IFEQ && fmn.opcode() == GETFIELD
                    && fmn.owner().equals(Hook.OBJECT_DEFINITION.getInternalName()) && fmn.desc().equals("Z")) {
                VariableNode vn = fmn.firstVariable();
                if (vn != null && vn.var() == 9) {
                    Hook.OBJECT_DEFINITION.put(new RSField(fmn, hooks[clipPTR++]));
                }
            }
        }
    }
}
