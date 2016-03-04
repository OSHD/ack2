package com.dank.analysis.impl.definition.npc;

import java.lang.reflect.Modifier;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import com.dank.DankEngine;
import com.dank.analysis.Analyser;
import com.dank.analysis.impl.client.visitor.EngineVarVisitor;
import com.dank.analysis.impl.definition.npc.visitor.IdVisitor;
import com.dank.analysis.impl.definition.npc.visitor.TempTransformVisitor;
import com.dank.analysis.impl.definition.npc.visitor.TransformVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;

/**
 * Project: RS3Injector
 * Time: 21:59
 * Date: 09-02-2015
 * Created by Dogerina.
 */
public class NpcDefinition extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        for (final FieldNode fn : getClassPath().get(Hook.NPC.getInternalName()).fields) {
            if (!Modifier.isStatic(fn.access) && fn.type().equals(cn.name)) {
                return new ClassSpec(Hook.NPC_DEFINITION, cn);
            }
        }
        return null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (!fn.isStatic()) {
                if (fn.desc.equals("Ljava/lang/String;")) {
                    Hook.NPC_DEFINITION.put(new RSField(fn, "name"));
                } else if (fn.desc.equals("[Ljava/lang/String;")) {
                    Hook.NPC_DEFINITION.put(new RSField(fn, "actions"));
                }
            }
        }

        for (final MethodNode mn : cn.methods) {
            if (!Modifier.isStatic(mn.access)) {
                if (mn.desc.endsWith(String.format("L%s;", cn.name))) {
                    for (final BasicBlock block : mn.graph()) {
                        block.tree().accept(new TempTransformVisitor(block));

                    }
                } else if (mn.desc.endsWith(";")) {
                    for (final BasicBlock block : mn.graph()) {
                        block.tree().accept(new ColorVisitor());
                    }
                }
                mn.graph().forEach(block -> block.tree().accept(new IdVisitor(block)));
            }
        }
        for (final MethodNode mn : cn.methods) {
            if (!Modifier.isStatic(mn.access) && mn.desc.endsWith(String.format("L%s;", cn.name))) {
                for (final BasicBlock block : mn.graph()) {
                    block.tree().accept(new TransformVisitor(block)); //requires the other transform to be done first..
                }
                Hook.NPC_DEFINITION.put(new RSMethod(mn, "transform"));
            }
        }
        for (final ClassNode c : DankEngine.classPath.getClasses()) {
            for (final MethodNode mn : c.methods) {
                if (Modifier.isStatic(mn.access) && mn.desc.startsWith("(I") && mn.desc.endsWith("L" + cn.name + ";")) {
                    Hook.CLIENT.put(new RSMethod(mn, "getNpcDefinition"));
                } else if (mn.name.equals("<clinit>") && c.name.equals(Hook.CLIENT.get("tempVars").owner)) {
                    mn.graph().forEach(b -> b.tree().accept(new EngineVarVisitor()));
                }
            }
        }
    }

    private final class ColorVisitor extends NodeVisitor {

        @Override
        public void visitMethod(MethodMemberNode mmn) {
            if (mmn.opcode() == INVOKEVIRTUAL) {
                final MethodNode mn = DankEngine.lookupMethod(mmn.owner(), mmn.name(), mmn.desc());
                if (mn != null && !contains(mn.instructions, IFNONNULL)) { //if it has IFNONNULL then it's textures and modifiedTextures
                    mmn.tree().accept(new NodeVisitor() {
                        @Override
                        public void visitField(FieldMemberNode fmn) {
                            if (fmn.desc().equals("[S")) {
                                if (Hook.NPC_DEFINITION.get("colors") == null) {
                                    Hook.NPC_DEFINITION.put(new RSField(fmn, "colors"));
                                } else if (Hook.NPC_DEFINITION.get("modifiedColors") == null) {
                                    Hook.NPC_DEFINITION.put(new RSField(fmn, "modifiedColors"));
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
}
