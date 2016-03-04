/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the license, or (at your option) any later version.
 */
package com.runetek.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dank.hook.Hook;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

/**
 * @author Dogerina
 * Generates accessor classes for the {@link com.runetek.services} package
 * TODO typed wrapper generation and generics for accessor interfaces
 */
public class AccessorGen implements Opcodes {

    //TODO source code generation?

    public ClassNode generate(Hook hook) {
        ClassNode cn = new ClassNode();
        cn.superName = "com/runetek/services/RSService";
        cn.access = ACC_INTERFACE | ACC_PUBLIC;
        cn.name = "com/runetek/services/RS" + hook.getDefinedName();
            if (hook.getSuperType() != null) //TODO supertypes for jdk classes...? such as RSCanvas extends Canvas
            cn.superName = "com/runetek/services/RS" + hook.getSuperType().getDefinedName();
        List<MethodNode> methods = createMethods(hook);
        cn.methods.addAll(methods);
        return cn;
    }

    private List<MethodNode> createMethods(Hook hook) { //TODO specify generics
        List<MethodNode> methods = new ArrayList<>();
        for (Map.Entry<String, String> hookToDesc : hook.hookToDesc.entrySet()) {
            String hookName = hookToDesc.getKey();
            String hookDesc = hookToDesc.getValue();
            if (hookDesc.equals("Undefined"))
                continue;
            String methodName = normalizeName(hookName, hookDesc);
            String methodDesc = normalizeDesc(hookDesc);
            MethodNode getter = new MethodNode(ACC_ABSTRACT, methodName, methodDesc, null, null);
            methods.add(getter);
        }
        return methods;
    }

    private String normalizeDesc(String desc) {
        return !desc.contains("(") ? "()" + desc : desc;
    }

    private String normalizeName(String name, String desc) {
        if (desc.startsWith("("))
            return name;
        String transformed = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return desc.equals("Z") ? "is" + transformed : "get" + transformed;
    }
}
