package com.dank.analysis.impl.client.visitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.ArrayLoadNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.StoreNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 27-02-2015
 * Time: 10:53
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class WidgetSpriteVisitor extends TreeVisitor {

    private static final List<StoredVar> STORED_VARS = new ArrayList<>();

    public WidgetSpriteVisitor(BasicBlock block) {
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
        Hook.WIDGET.put(new RSField(STORED_VARS.get(0).value, "xSprites"));
        Hook.WIDGET.put(new RSField(STORED_VARS.get(1).value, "ySprites"));
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return true;
    }

    @Override
    public void visitStore(StoreNode sn) {
        if (sn.isOpcode(ISTORE)) {
            final ArrayLoadNode iaload = (ArrayLoadNode) sn.layer(IADD, IALOAD);
            if (iaload != null && iaload.layer(ILOAD) != null) { //should never be null but just in case...
                final FieldMemberNode fmn = (FieldMemberNode) iaload.layer(GETFIELD);
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
