package com.dank.analysis.visitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.NodeTree;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.node.TargetNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

/**
 * Project: DankWise
 * Time: 20:28
 * Date: 15-02-2015
 * Created by Dogerina.
 */
public class TargetVisitor extends NodeVisitor implements Opcodes {

    private final NodeTree tree;
    private final Explorer explorer;

    public TargetVisitor(final NodeTree tree, final AbstractInsnNode[] stack) {
        this.tree = tree;
        this.explorer = new Explorer(stack);
    }

    @Override
    public void visitJump(final JumpNode jn) {
        final AbstractInsnNode ain = explorer.stack[explorer.findTarget(jn.insn())];
        final TargetNode target = new TargetNode(tree, ain, 0, 0);
        target.addTargeter(jn);
        jn.setTarget(target);
    }

    private final class BranchTarget {

        private final int location;
        private final BranchTarget parent;

        private BranchTarget(final int location) {
            this(null, location);
        }

        private BranchTarget(final BranchTarget parent, final int location) {
            this.location = location;
            this.parent = parent;
        }
    }

    private final class Explorer {

        private final AbstractInsnNode[] stack;
        private final Queue<BranchTarget> paths = new ArrayDeque<>();

        private Explorer(final AbstractInsnNode[] stack) {
            this.stack = stack;
        }

        private int findTarget(JumpInsnNode jin) {
            LabelNode target = jin.label;
            for (int i = 0; i < stack.length; i++) {
                if (stack[i] == target) {
                    AbstractInsnNode next = stack[i];
                    while ((next = next.next()) instanceof LabelNode) {
                        i++;
                    }
                    return i;
                }
            }
            throw new Error();
        }

        private void expand(BranchTarget path) {
            for (int pos = path.location; pos < stack.length; pos++) {
                switch (stack[pos].opcode()) {
                    case IF_ICMPNE:
                    case IF_ICMPEQ:
                    case IF_ICMPLT:
                    case IF_ICMPGT:
                    case IF_ICMPLE:
                    case IF_ICMPGE:
                    case IFNE:
                    case IFGE:
                    case IFLE:
                    case IFEQ:
                    case IFLT:
                    case IFGT: {
                        JumpInsnNode jin = (JumpInsnNode) stack[pos];
                        int target_idx = findTarget(jin);
                        paths.offer(new BranchTarget(path, target_idx)); // Branch succeed
                        paths.offer(new BranchTarget(path, pos + 1));    // Branch failed
                        return;
                    }
                    case GOTO: {
                        JumpInsnNode jin = (JumpInsnNode) stack[pos];
                        int target_idx = findTarget(jin);
                        paths.offer(new BranchTarget(path, target_idx));
                        return;
                    }
                    case ARETURN:
                    case IRETURN:
                    case LRETURN:
                    case DRETURN:
                    case FRETURN: {
                        paths.offer(new BranchTarget(path, -pos));
                        return;
                    }
                }
            }
            throw new InternalError("Expected Jump or Return before EOF");
        }

        private List<AbstractInsnNode[]> compute() {
            final List<AbstractInsnNode[]> matches = new ArrayList<>();
            try {
                final BranchTarget root = new BranchTarget(0);
                expand(root);
                while (!paths.isEmpty()) {
                    final BranchTarget p = paths.poll();
                    if (p.location < 0) {
                        final ArrayDeque<AbstractInsnNode> path = new ArrayDeque<>();
                        path.add(stack[-p.location]);
                        for (BranchTarget i = p.parent; i != null; i = i.parent) {
                            path.add(stack[i.location]);
                        }
                        final AbstractInsnNode[] match = path.toArray(new AbstractInsnNode[path.size()]);
                        matches.add(match);
                    }
                    expand(p);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return matches;
        }
    }
}
