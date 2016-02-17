package com.dank.analysis.impl.node;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 18-02-2015
 * Time: 04:52
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class ItemTable extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount() == 2 && cn.fieldCount(int[].class) == 2 && cn.superName(Hook.NODE.getInternalName())
                ? new ClassSpec(Hook.ITEM_TABLE, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        final List<Var> vars = new ArrayList<>();
        for (final ClassNode node : getClassPath().getClasses()) {
            for (final MethodNode mn : node.methods) {
                final AtomicBoolean found = new AtomicBoolean(false);
                if (!Modifier.isStatic(mn.access)) {
                    continue;
                }
                for (final BasicBlock block : mn.graph()) {
                    block.tree().accept(new NodeVisitor() {
                        @Override
                        public void visitMethod(MethodMemberNode mmn) {
                            if (mmn.owner().equals(Hook.NODETABLE.getInternalName()) && mmn.desc().startsWith("(J")
                                    && mmn.hasChild(I2L) && mmn.hasChild(GETSTATIC) && mmn.nextType() != null
                                    && mmn.nextType().type().equals(Hook.ITEM_TABLE.getInternalName())) {
                                Hook.CLIENT.put(new RSField(mmn.firstField(), "itemTables"));
                                found.set(true);
                            }
                        }
                    });
                }
                if (!found.get()) continue;
                for (final BasicBlock block : mn.graph()) {
                    block.tree().accept(new NodeVisitor() {
                        @Override
                        public void visit(AbstractNode an) {
                            if (an.opcode() != ARRAYLENGTH || an.previous(ILOAD) == null)
                                return;
                            final FieldMemberNode fmn = an.firstField();
                            final VariableNode var = (VariableNode) an.previous(ILOAD);
                            if (fmn != null && fmn.desc().equals("[I") && fmn.owner().equals(cn.name)) {
                                boolean add = true;
                                for (Var var0 : vars) {
                                    if (var0.field.key().equals(fmn.key())) {
                                        add = false;
                                        break;
                                    }
                                }
                                if (add) {
                                    Var var0 = new Var();
                                    var0.field = fmn;
                                    var0.idx = var.var();
                                    vars.add(var0);
                                }
                            }
                        }
                    });
                }
            }
        }
        while (vars.size() > 2)
            vars.remove(2);
        if (vars.size() < 2)
            throw new RuntimeException("ItemTable fail");
        Hook.ITEM_TABLE.put(new RSField(vars.get(0).field, "ids"));
        Hook.ITEM_TABLE.put(new RSField(vars.get(1).field, "quantities"));
    }

    private class Var {
        private FieldMemberNode field;
        private int idx;
    }
}
