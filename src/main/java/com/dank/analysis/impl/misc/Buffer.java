package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 16:42
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class Buffer extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(int.class) == 1 && cn.fieldCount(byte[].class) == 1 && cn.fieldCount() == 2
                && cn.superName(Hook.NODE.getInternalName()) ? new ClassSpec(Hook.BUFFER, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (!fn.isStatic()) {
                if (fn.desc.equals("I")) {
                    Hook.BUFFER.put(new RSField(fn, "caret"));
                } else {
                    Hook.BUFFER.put(new RSField(fn, "payload"));
                }
            } else {
            	if (fn.desc.equals("[I")) {
            		Hook.BUFFER.put(new RSField(fn, "crcTable"));
            	}
            }
        }
        for (final MethodNode mn : cn.methods) {
        	if (!mn.isStatic()) {
        		if (mn.desc.startsWith("(Ljava/math/BigInteger;Ljava/math/BigInteger;")) {
        			if (!mn.desc.equals("(Ljava/math/BigInteger;Ljava/math/BigInteger;)V"))
        				Hook.BUFFER.put(new RSMethod(mn, "applyRSA"));
        		}
        	}
        }
    }
}
