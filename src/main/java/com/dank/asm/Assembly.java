package com.dank.asm;


import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class Assembly {

    public static String name(AbstractInsnNode ain) {
        try {
            return Assembly.name(ain);
        } catch (ArrayIndexOutOfBoundsException e) {
            return "NULL";
        }
    }

    public static AbstractInsnNode prev(AbstractInsnNode ain, int max, Mask mask) {
        for (int i = 0; i < max && (ain = ain.previous()) != null; i++) {
            if (mask.matches(ain)) return ain;
        }
        return null;
    }

    public static AbstractInsnNode next(AbstractInsnNode ain, int max, Mask mask) {
        for (int i = 0; i < max && (ain = ain.next()) != null; i++) {
            if (mask.matches(ain)) return ain;
        }
        return null;
    }

    public static List<AbstractInsnNode> next(AbstractInsnNode ain, Mask... masks) {
        List<AbstractInsnNode> insns = new ArrayList<>();
        for (Mask mask : masks) {
            AbstractInsnNode next = next(ain, mask.distance, mask);
            if (next == null) return null;
            ain = next;
            insns.add(next);
        }
        return insns;
    }

    public static List<AbstractInsnNode> find(ClassNode cn, Mask... masks) {
        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                List<AbstractInsnNode> result = next(ain, masks);
                if (result != null) return result;
            }
        }
        return null;
    }

    public static List<AbstractInsnNode> find(MethodNode mn, Mask... masks) {
        for (AbstractInsnNode ain : mn.instructions.toArray()) {
            List<AbstractInsnNode> result = next(ain, masks);
            if (result != null) return result;
        }
        return null;
    }

    public static List<List<AbstractInsnNode>> findAll(AbstractInsnNode[] ains, Mask mask) {
        List<List<AbstractInsnNode>> all = new ArrayList<>();
        for (AbstractInsnNode ain : ains) {
            List<AbstractInsnNode> result = next(ain, mask);
            if (result != null) all.add(result);
        }
        return all.isEmpty() ? null : all;
    }

    public static List<List<AbstractInsnNode>> findAll(MethodNode mn, Mask... masks) {
        List<List<AbstractInsnNode>> all = new ArrayList<>();
        for (AbstractInsnNode ain : mn.instructions.toArray()) {
            List<AbstractInsnNode> result = next(ain, masks);
            if (result != null) all.add(result);
        }
        return all.isEmpty() ? null : all;
    }

    public static List<List<AbstractInsnNode>> findAll(ClassNode cn, Mask... masks) {
        List<List<AbstractInsnNode>> all = new ArrayList<>();
        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                List<AbstractInsnNode> result = next(ain, masks);
                if (result != null) all.add(result);
            }
        }
        return all.isEmpty() ? null : all;
    }
}
