package com.dank.analysis.impl.node;

import java.lang.reflect.Modifier;

import com.dank.analysis.impl.widget.visitor.MarginVisitor;
import com.dank.util.Wildcard;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.DankEngine;
import com.dank.analysis.Analyser;
import com.dank.analysis.impl.client.visitor.RunescriptOpcodeHandlerVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;

/**
 * Project: RS3Injector
 * Time: 06:23
 * Date: 07-02-2015
 * Created by Dogerina.
 */
public class Node extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount("J") == 1 && cn.fieldCount('L' + cn.name + ';') == 2 && cn.ownerless() ? new ClassSpec(Hook.NODE, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (fn.desc.equals("J")) {
                Hook.NODE.put(new RSField(fn, "key"));
            } else if (fn.desc.equals(String.format("L%s;", cn.name))) {
                Hook.NODE.put(Modifier.isPublic(fn.access) ? new RSField(fn, "next") : new RSField(fn, "previous"));
            }
        }
        for (final MethodNode mn : cn.methods) {
            if (!Modifier.isStatic(mn.access) && mn.desc.endsWith("V") && !mn.name.contains("<")) {
                Hook.NODE.put(new RSMethod(mn, "unlink"));
            }
        }

//        NodeVisitor rsohv = new RunescriptOpcodeHandlerVisitor();
//
//        for (ClassNode c : DankEngine.classPath.getClasses()) {
//            for (MethodNode mn : c.methods) {
//                if(new Wildcard("(" + Hook.RUNESCRIPT.getInternalDesc() + "I?)V").matches(mn.desc)) {
//                    System.out.println("??"+mn.key());
//                    TreeBuilder.build(mn).accept(new MarginVisitor());
//                }
//                TreeBuilder.build(mn).accept(rsohv);
//
//            }
//        }
    }
}
