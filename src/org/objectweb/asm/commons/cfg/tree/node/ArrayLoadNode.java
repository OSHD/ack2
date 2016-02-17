package org.objectweb.asm.commons.cfg.tree.node;

import org.objectweb.asm.commons.cfg.tree.NodeTree;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Project:
 * Date: 25-02-2015
 * Time: 20:25
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class ArrayLoadNode extends AbstractNode {

    public ArrayLoadNode(NodeTree tree, AbstractInsnNode insn, int collapsed, int producing) {
        super(tree, insn, collapsed, producing);
    }

    /**
     * @return the array reference
     */
    public AbstractNode array() {
        if (super.children() == 1 && super.hasChild(DUP2)) {
            return super.child(0).first();
        }
        return super.child(0);
    }

    /**
     * @return the element index
     */
    public AbstractNode elementIndex() { //cant name index because conflicts with int index() from super
        if (super.children() == 1 && super.hasChild(DUP2)) {
            return super.child(0).child(1);
        }
        return super.child(1);
    }
}
