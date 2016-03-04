package com.dank.analysis.impl.definition.item;

import java.lang.reflect.Modifier;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import com.dank.DankEngine;
import com.dank.analysis.Analyser;
import com.dank.analysis.impl.definition.item.visitor.IdVisitor;
import com.dank.analysis.impl.misc.GStrings;
import com.dank.analysis.visitor.MultiplierVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 27-02-2015
 * Time: 18:50
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class ItemDefinition extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        final String class_des = "L" + cn.name + ";";
        boolean copy_function = false;
        for (final MethodNode mn : cn.methods) {
            if (Modifier.isStatic(mn.access)) continue;
            if (mn.desc.endsWith("V")) {
                if (mn.desc.startsWith("(" + class_des + class_des)) copy_function = true;
            }
        }
        return !copy_function ? null : new ClassSpec(Hook.ITEM_DEFINITION, cn);
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (fn.desc.equals("Ljava/lang/String;") && !fn.isStatic()) {
                Hook.ITEM_DEFINITION.put(new RSField(fn, "name"));
            }
        }

        final String dual = "L" + cn.name + ";L" + cn.name + ";";
        for (final MethodNode mn : cn.methods) {
            mn.graph().forEach(b -> b.tree().accept(new IdVisitor(b)));
            if (mn.isVoid()) {
                if (!mn.isStatic() && mn.desc.startsWith("(" + dual)) {
                    for (final BasicBlock block : mn.graph()) {
                        block.tree().accept(new StackableVisitor());
                    }
                } else if (mn.name.equals("<init>")) {
                    for (final BasicBlock block : mn.graph()) {
                        block.tree().accept(new ActionVisitor());
                    }
                }
            } else if (mn.desc.endsWith(";")) {
                for (final BasicBlock block : mn.graph()) {
                    block.tree().accept(new ColorVisitor());
                }
            }
        }
    }

    private final class ColorVisitor extends NodeVisitor {

        @Override
        public void visitMethod(MethodMemberNode mmn) {
            if (mmn.opcode() == INVOKEVIRTUAL) {
                final MethodNode mn = DankEngine.lookupMethod(mmn.owner(), mmn.name(), mmn.desc());
                if (mn != null && !contains(mn.instructions, IFNONNULL)) {
                    mmn.tree().accept(new NodeVisitor() {
                        @Override
                        public void visitField(FieldMemberNode fmn) {
                            if (fmn.desc().equals("[S")) {
                                if (Hook.ITEM_DEFINITION.get("colors") == null) {
                                    Hook.ITEM_DEFINITION.put(new RSField(fmn, "colors"));
                                } else if (Hook.ITEM_DEFINITION.get("modifiedColors") == null) {
                                    Hook.ITEM_DEFINITION.put(new RSField(fmn, "modifiedColors"));
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

    private final class ActionVisitor extends NodeVisitor {

        @Override
        public void visitField(FieldMemberNode fmn) {
            final FieldMemberNode actions = (FieldMemberNode) fmn.tree().first(f -> f.isOpcode(PUTFIELD)
                    && ((FieldMemberNode) f).isType(String[].class) && Hook.resolve(((FieldMemberNode) f).key()) == null);
            if (actions != null && actions.isType(String[].class)) {
                if (GStrings.compare(fmn.key(), "Drop")) {
                    Hook.ITEM_DEFINITION.put(new RSField(actions, "actions"));
                } else if (GStrings.compare(fmn.key(), "Take")) {
                    Hook.ITEM_DEFINITION.put(new RSField(actions, "groundActions"));
                }
            }
        }
    }

    private final class StackableVisitor extends NodeVisitor {

        @Override
        public void visitNumber(NumberNode nn) {
            if (nn.opcode() == LDC && nn.parent().opcode() == PUTFIELD) {
                final FieldMemberNode stackable = (FieldMemberNode) nn.parent();
                if (stackable != null && nn.number() * MultiplierVisitor.getDecoder(stackable.key()) == 1) {
                    Hook.ITEM_DEFINITION.put(new RSField(stackable, "stackable"));
                }
            } else if (nn.opcode() == ICONST_1) {
                final FieldMemberNode storeValue = (FieldMemberNode) nn.preLayer(IMUL, PUTFIELD);
                if (storeValue != null) {
                    final VariableNode aload_2 = (VariableNode) nn.parent().layer(GETFIELD, ALOAD);
                    if (aload_2 == null || aload_2.var() != 2) return;
                    Hook.ITEM_DEFINITION.put(new RSField(storeValue, "storeValue"));
                }
            }
        }
    }
}
