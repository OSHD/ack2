package com.dank.analysis.impl.client.visitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import com.dank.DankEngine;
import com.dank.analysis.impl.misc.GStrings;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 17:50
 * Date: 15-02-2015
 * Created by Dogerina.
 */
public class SelectionVisitor extends TreeVisitor {

    public static final List<FieldMemberNode> SPELL_POSSIBILITIES = new ArrayList<>();

    public SelectionVisitor(BasicBlock block) {
        super(block);
    }

    public static void onEnd() {
        SelectionVisitor.SPELL_POSSIBILITIES.sort(new Comparator<FieldMemberNode>() {
            @Override
            public int compare(FieldMemberNode o1, FieldMemberNode o2) {
                return Integer.compare(o1.getReferenceCount(DankEngine.classPath), o2.getReferenceCount(DankEngine.classPath));
            }
        });
        Hook.CLIENT.put(new RSField(SelectionVisitor.last(), "spellSelected"));
    }

    private static FieldMemberNode last() {
        return SPELL_POSSIBILITIES.get(SPELL_POSSIBILITIES.size() - 1);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(new MemberQuery(GETSTATIC, "Ljava/lang/String;")) == 1;
    }

    @Override
    public void visitField(final FieldMemberNode fmn) {
        if (GStrings.compare(fmn.key(), "Walk here")) {
            for (final BasicBlock b : block.owner.graph()) {
                b.tree().accept(new TreeVisitor(b) {
                    @Override
                    public boolean validateBlock(BasicBlock block) {
                        return b.count(new MemberQuery(GETSTATIC, "I")) == 1 && b.count(ICONST_1) == 1 //item
                                || b.count(new MemberQuery(GETSTATIC, "Z")) == 1
                                && (b.count(IFEQ) == 1 || b.count(IFNE) == 1); //spell
                    }

                    @Override
                    public void visitField(final FieldMemberNode fmn) {
                        if (fmn.opcode() == GETSTATIC) {
                            if (fmn.desc().equals("I") && Hook.CLIENT.get("itemSelectionStatus") == null
                                    && fmn.owner().equals("client")) {
                                Hook.CLIENT.put(new RSField(fmn, "itemSelectionStatus"));
                            } else if (fmn.desc().equals("Z") && Hook.CLIENT.get("spellSelected") == null) {
                                for (final FieldMemberNode _fmn : SPELL_POSSIBILITIES) {
                                    if (fmn.key().equals(_fmn.key())) {
                                        return;
                                    }
                                }
                                SPELL_POSSIBILITIES.add(fmn);
                            }
                        }
                    }
                });
            }
        }
    }
}
