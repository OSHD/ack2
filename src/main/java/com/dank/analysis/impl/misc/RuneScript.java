package com.dank.analysis.impl.misc;

import com.dank.util.Wildcard;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.misc.visitor.RuneScriptVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSMethod;

/**
 * Project: DankWise
 * Date: 20-02-2015
 * Time: 12:24
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class RuneScript extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName(Hook.DUAL_NODE.getInternalName())
                && cn.fieldCount(int.class) == 4
                && cn.fieldCount(int[].class) == 2
                && cn.fieldCount(String[].class) == 1
                ? new ClassSpec(Hook.RUNESCRIPT, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final ClassNode c : getClassPath()) {
            for (final MethodNode mn : c.methods) {
                if (mn.isStatic() && mn.desc.endsWith(")" + Hook.RUNESCRIPT.getInternalDesc())) {
                    Hook.CLIENT.put(new RSMethod(mn, "getRuneScript"));
                    TreeBuilder.build(mn).accept(new RuneScriptVisitor());
                }
                if (new Wildcard("(" + Hook.SCRIPT_EVENT.getInternalDesc() + "I?)V").matches(mn.desc)) {
//                    System.out.println(TreeBuilder.build(mn));
                    TreeBuilder.build(mn).accept(new NodeVisitor() {
                        @Override
                        public void visitVariable(VariableNode node) {
                            if (node.opcode() == Opcodes.ALOAD) {

                                if (node.var() == 0) {
                                    FieldMemberNode fmn = (FieldMemberNode) node.preLayer(Opcodes.GETFIELD);
                                    if (fmn != null && fmn.owner().equals(Hook.SCRIPT_EVENT.getInternalName())) {
                                    }
                                }
                            }
                        }
                    });

                }
            }
        }
    }
}
