package com.dank.analysis.impl.character.npc;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.character.IdleInterpreter;
import com.dank.analysis.impl.character.Inter;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.lang.reflect.Modifier;

/**
 * Project: DankWise
 * Time: 00:16
 * Date: 14-02-2015
 * Created by Dogerina.
 */
public class Npc extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.name.equals(Hook.NPC.getInternalName()) ? new ClassSpec(Hook.NPC, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (Modifier.isStatic(fn.access) || !fn.desc.startsWith("L")) continue;
            Hook.NPC.put(new RSField(fn, "definition"));
        }
        for (final MethodNode mn : cn.methods) {
            if (mn.isStatic()) continue; // Make sure arg0 is Buffer, j

            //nvm
            if (new Wildcard("(L" + Hook.BUFFER.getInternalName() + ";?)V").matches(mn.desc))
                getRestOfAnimations(mn);

        }
        getClassPath().forEach((name, c) -> c.methods.forEach(methodNode -> getIdleAnimations(methodNode)));
        getClassPath().forEach((name, c) -> c.methods.forEach(methodNode -> getRestOfAnimations(methodNode)));

    }

    public void getRestOfAnimations(MethodNode methodNode) {
        Inter interpreter = new Inter();
        //I thought we were targeting the same method we hooked into before for the other animations.
        // so were... targeting the IO method?

//            AnimationInterpreter interpreter = new AnimationInterpreter("aj");

        Analyzer<BasicValue> analyzer = new Analyzer<>(interpreter);
        try {
            analyzer.analyze(methodNode.owner.name, methodNode);
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }
    }

    public void getIdleAnimations(MethodNode methodNode) {
        if (Modifier.isStatic(methodNode.access) && new Wildcard("(*)V").matches(methodNode.desc)) {
            IdleInterpreter interpreter = new IdleInterpreter(Hook.NPC.get("definition").desc.substring(1, 3));
            Analyzer analyzer = new Analyzer(interpreter);
            try {
                analyzer.analyze(methodNode.owner.name, methodNode);
            } catch (AnalyzerException e) {
                e.printStackTrace();
            }
//            List<RSField> results = interpreter.getFields();
//            if (!results.isEmpty()) {
//
//                Hook.CHARACTER.put(results.get(0));
//                if (Hook.CHARACTER.get("getWalkAnimation") != null) {
//                    System.out.printlnw(">" + methodNode.owner.name + "." + methodNode.name);
//                }
//            }
        }
    }
}
