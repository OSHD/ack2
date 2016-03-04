/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the license, or (at your option) any later version.
 */
package com.runetek.tests;

import java.io.File;
import java.io.FileOutputStream;

import com.dank.hook.Hook;
import com.runetek.codegen.AccessorGen;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

/**
 * @author unsigned
 * @since 08-06-2015
 */
public class AccessorTest {

    public static void main(String... args) {
        AccessorGen adapter = new AccessorGen();
        ClassNode rsclient = adapter.generate(Hook.CLIENT);
        try {
            File file = new File("./src/" + rsclient.name + ".class");
            if (!file.exists())
                file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            ClassWriter writer = new ClassWriter(0);
            rsclient.accept(writer);
            out.write(writer.toByteArray());
            out.close();
            System.out.println("...Generated " + rsclient.name + ".class!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
