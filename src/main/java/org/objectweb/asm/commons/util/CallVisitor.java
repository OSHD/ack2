package org.objectweb.asm.commons.util;

import com.dank.asm.ClassPath;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CallVisitor {

    public CallVisitor(final ClassPath cp) {
        int unused = 0;
        int removed = -1;
        long start = System.nanoTime();
        while (removed != 0) {
            removed = 0;
            Set<String> used = new HashSet<>();
            Set<String> inherited = new HashSet<>();
            for (ClassNode cn : cp.getClasses()) {
                for (MethodNode mn : cn.methods) {
                    for (AbstractInsnNode ain : mn.instructions.toArray()) {
                        if (ain instanceof MethodInsnNode) {
                            MethodInsnNode min = (MethodInsnNode) ain;
                            used.add(min.owner + "" + min.name + min.desc);
                        }
                    }
                }
                if (!cn.superName.equals("java/lang/Object")) {
                    if (cn.superName.startsWith("java")) {
                        try {
                            Class<?> clazz = Class.forName(cn.superName.replaceAll("/", ""));
                            for (Method method : clazz.getDeclaredMethods()) {
                                int mods = method.getModifiers();
                                if (Modifier.isPrivate(mods) || Modifier.isStatic(mods)) continue;
                                String desc = Type.getMethodDescriptor(method);
                                inherited.add(method.getName() + desc);
                            }
                        } catch (ClassNotFoundException e) {
                            //e.printStackTrace();
                        }
                    } else {
                        ClassNode node = cp.get(cn.superName);
                        for (MethodNode mn : node.methods) {
                            if ((mn.access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) != 0) {
                                continue;
                            }
                            inherited.add(mn.name + mn.desc);
                        }
                    }
                }
                for (String iface : cn.interfaces) {
                    if (iface.startsWith("java")) {
                        try {
                            Class<?> clazz = Class.forName(iface.replaceAll("/", ""));
                            for (Method method : clazz.getDeclaredMethods()) {
                                String desc = Type.getMethodDescriptor(method);
                                inherited.add(method.getName() + desc);
                            }
                        } catch (ClassNotFoundException e) {
                            //e.printStackTrace();
                        }
                    } else {
                        ClassNode node = cp.get(iface.replaceAll("/", ""));
                        for (MethodNode mn : node.methods) inherited.add(mn.name + mn.desc);
                    }
                }
            }
            for (ClassNode cn : cp.getClasses()) {
                List<MethodNode> remove = new ArrayList<>();
                for (MethodNode mn : cn.methods) {
                    if (mn.name.contains("<") || mn.name.contains(">")) continue;
                    if (!used.contains(cn.name + "" + mn.name + mn.desc) && !inherited.contains(mn.name + mn.desc)) {
                        unused++;
                        removed++;
                        remove.add(mn);
                    }
                }
                for (MethodNode mn : remove) cn.methods.remove(mn);
            }
        }
        long end = System.nanoTime();
        System.out.println("Removed " + unused + " unused methods in " + String.format("%.2f", (end - start) / 1e9) + " secs");
    }
}
