package org.objectweb.asm.commons.cfg.tree.node;

import org.objectweb.asm.commons.cfg.tree.NodeTree;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Project:
 * Date: 25-02-2015
 * Time: 20:37
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class StoreNode extends VariableNode {

    public StoreNode(NodeTree tree, AbstractInsnNode insn, int collapsed, int producing) {
        super(tree, insn, collapsed, producing);
    }

    public AbstractNode value() {
        return super.first();
    }
}
