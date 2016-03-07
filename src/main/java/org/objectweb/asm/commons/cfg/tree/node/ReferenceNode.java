package org.objectweb.asm.commons.cfg.tree.node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.commons.cfg.tree.NodeTree;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.DankEngine;
import com.dank.asm.ClassPath;

public class ReferenceNode extends AbstractNode {

    public ReferenceNode(NodeTree tree, AbstractInsnNode insn, int collapsed, int producing) {
        super(tree, insn, collapsed, producing);
    }

    public boolean isStatic() {
        return opcode() == GETSTATIC || opcode() == PUTSTATIC || opcode() == INVOKESTATIC;
    }

    public int access() {
        AbstractInsnNode ain = insn();
        if (ain instanceof FieldInsnNode) {
            FieldInsnNode fin = (FieldInsnNode) ain;
            FieldNode fn = DankEngine.lookupField(fin.owner,fin.name,fin.desc);
            return fn.access;
        } else if (ain instanceof MethodInsnNode) {
            MethodInsnNode min = (MethodInsnNode) ain;
            MethodNode mn = DankEngine.lookupMethod(min.owner,min.name,min.desc);
            return mn.access;
        }
        return -1;
    }
    public String key() {
        AbstractInsnNode ain = insn();
        if (ain instanceof FieldInsnNode) {
            FieldInsnNode fin = (FieldInsnNode) ain;
            return fin.owner + "." + fin.name + fin.desc;
        } else if (ain instanceof MethodInsnNode) {
            MethodInsnNode min = (MethodInsnNode) ain;
            return min.owner + "." + min.name + min.desc;
        }
        return null;
    }

    public String owner() {
        AbstractInsnNode insn = insn();
        if (this instanceof FieldMemberNode) {
            return ((FieldInsnNode) insn).owner;
        } else if (this instanceof MethodMemberNode) {
            return ((MethodInsnNode) insn).owner;
        }
        return null;
    }

    public String name() {
        AbstractInsnNode ain = insn();
        if (ain instanceof FieldInsnNode) {
            return ((FieldInsnNode) ain).name;
        } else if (ain instanceof MethodInsnNode) {
            return ((MethodInsnNode) ain).name;
        }
        return null;
    }

    public String desc() {
        AbstractInsnNode ain = insn();
        if (this instanceof FieldMemberNode) {
            return ((FieldInsnNode) ain).desc;
        } else if (this instanceof MethodMemberNode) {
            return ((MethodInsnNode) ain).desc;
        }
        return null;
    }

    public boolean referenced(MethodNode mn) {
        for (AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof FieldInsnNode) {
                FieldInsnNode fin = (FieldInsnNode) ain;
                if (key().equals(fin.owner + "" + fin.name)) return true;
            } else if (ain instanceof MethodInsnNode) {
                MethodInsnNode min = (MethodInsnNode) ain;
                if (key().equals(min.owner + "" + min.name + min.desc)) return true;
            }
        }
        return false;
    }

    public int getReferenceCount(final ClassPath cp) {
        final AtomicInteger refs = new AtomicInteger();
        cp.forEach((name, cn) -> cn.methods.forEach(m -> {
            if (referenced(m)) {
                refs.incrementAndGet();
            }
        }));
        return refs.get();
    }

    public List<MethodNode> getReferences(final ClassPath cp) {
        final List<MethodNode> refs = new ArrayList<>();
        cp.forEach((name, cn) -> cn.methods.forEach(m -> {
            if (referenced(m)) {
                refs.add(m);
            }
        }));
        return refs;
    }
}