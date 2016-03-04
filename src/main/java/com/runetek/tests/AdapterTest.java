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
import com.runetek.codegen.AdapterGen;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

/**
 * @author Dogerina
 * @since 08-06-2015
 */
public class AdapterTest {

    public static void main(String... args) {
        AdapterGen adapter = new AdapterGen();
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
