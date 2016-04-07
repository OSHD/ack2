package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.marn.asm.FieldData;
import com.marn.dynapool.DynaFlowAnalyzer;

public class ScriptEvent extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        //6 int fields since 79. 5 prior to that
        return cn.fieldCount(Object[].class) == 1 && cn.fieldCount(int.class) == 6 ? new ClassSpec(Hook.SCRIPT_EVENT, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
        	FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
            if (!fn.isStatic()) {
                if (fn.desc.equals("[Ljava/lang/Object;")) {
                    Hook.SCRIPT_EVENT.put(new RSField(fn, "args"));
                }else if (fn.desc.equals("Ljava/lang/String;")) {
                    Hook.SCRIPT_EVENT.put(new RSField(fn, "opbase"));
                }else if (fn.desc.equals("Z")) {
                    Hook.SCRIPT_EVENT.put(new RSField(fn, "hasRan"));
                }
            }
        }
    }
}
