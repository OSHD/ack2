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
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class AdapterGen implements Opcodes {

    public ClassNode generate(Hook hook) {
        ClassNode cn = new ClassNode();
        cn.superName = "com/runetek/client/Adapter";
        cn.access = ACC_PUBLIC;
        cn.name = "com/runetek/client/" + hook.getDefinedName();
        String service = serviceFor(hook.getDefinedName());
        cn.signature = "<T:L" + service + ";>Ljava/lang/Object;L" + service + "<TT;>;";
        MethodNode mn = new MethodNode();
        if (hook.getSuperType() != null)
            cn.superName = "com/runetek/client/" + hook.getSuperType().getDefinedName();
        //cn.methods.add(constructorFor(hook));
        return cn;
    }

    private String serviceFor(String adapter) {
        return "com/runetek/client/services/RS" + adapter;
    }

    private MethodNode constructorFor(Hook hook) {
        return null;
    }
}
