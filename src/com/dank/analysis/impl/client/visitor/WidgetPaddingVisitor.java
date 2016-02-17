package com.dank.analysis.impl.client.visitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.NumberQuery;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.StoreNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 27-02-2015
 * Time: 08:09
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class WidgetPaddingVisitor extends TreeVisitor {

    private static final List<StoredVar> STORED_VARS = new ArrayList<>();

    public WidgetPaddingVisitor(BasicBlock block) {
        super(block);
    }

    public static void onEnd() {
        STORED_VARS.sort(new Comparator<StoredVar>() {
            @Override
            public int compare(StoredVar o1, StoredVar o2) {
                return o1.var.var() - o2.var.var();
            }
        });
        if (STORED_VARS.size() < 2) throw new RuntimeException("Fail");
        Hook.WIDGET.put(new RSField(STORED_VARS.get(0).value, "columnPadding"));
        Hook.WIDGET.put(new RSField(STORED_VARS.get(1).value, "rowPadding"));
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return block.count(IADD) > 0 && block.get(new NumberQuery(BIPUSH, 32)) != null;
    }

    @Override
    public void visitStore(StoreNode sn) {
        if (sn.isOpcode(ISTORE)) {
            final ArithmeticNode an = (ArithmeticNode) sn.layer(IADD, IMUL, IADD);
            if (an != null && an.firstNumber() != null) {
                final FieldMemberNode fmn = (FieldMemberNode) an.layer(IMUL, GETFIELD);
                if (fmn != null && fmn.owner().equals(Hook.WIDGET.getInternalName())) {
                    StoredVar var = new StoredVar();
                    var.value = fmn;
                    var.var = sn;
                    STORED_VARS.add(var);
                }
            }
        }
    }

    private final class StoredVar {
        private FieldMemberNode value;
        private StoreNode var;
    }
}
