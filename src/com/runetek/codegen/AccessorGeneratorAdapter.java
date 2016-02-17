/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the license, or (at your option) any later version.
 */
package com.runetek.codegen;

import com.dank.hook.Hook;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

public class AccessorGeneratorAdapter implements Opcodes {

    private final Hook hook;

    public AccessorGeneratorAdapter(Hook hook) {
        this.hook = hook;
    }

    public ClassNode write(Hook hook) {
        ClassNode cn = new ClassNode();
        cn.access = ACC_INTERFACE;
        cn.name = "com/runetek/services/" + hook.getDefinedName();
        if (hook.getSuperType() != null)
            cn.superName = "com/runetek/services/" + hook.getSuperType().getDefinedName(); //TODO supertypes for jdk classes...

        return cn;
    }

   /* public List<MethodNode> createMethods() {
        List<MethodNode> methods = new ArrayList<>();
        for (Map.Entry<String, String> hookToDesc : hook.hookToDesc.entrySet()) {
            String hookName = hookToDesc.getKey();
            String methodName = normalizeName(hookName, hookToDesc.getValue());
            MethodNode getter = new MethodNode(ACC_PUBLIC, methodName, hookToDesc.getValue(), null, null);
        }
        return methods;
    }

    private String normalizeName(String name, String desc) {

    }*/
}
