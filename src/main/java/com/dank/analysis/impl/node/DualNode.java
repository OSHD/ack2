package com.dank.analysis.impl.node;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

/**
 * Project: RS3Injector
 * Time: 06:31
 * Date: 07-02-2015
 */
public class DualNode extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName.equals(Hook.NODE.getInternalName()) && cn.fieldCount('L' + cn.name + ';') == 2 ? new ClassSpec(Hook.DUAL_NODE, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
        for (final MethodNode mn : cn.methods) {
        	if(mn.isStatic())
        		continue;
        	MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
        	if(md.referencedFrom.size()>0 && new Wildcard("()V").matches(md.METHOD_DESC) && !md.METHOD_NAME.equals("<init>")){
                Hook.DUAL_NODE.put(new RSMethod(mn, "unlinkDual"));
                List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
						Mask.GETFIELD.own(cn.name),
						Mask.IFNONNULL
						);
				if (pattern != null) {
					AbstractInsnNode insn = pattern.get(0);
					FieldInsnNode fin = (FieldInsnNode)insn;
					FieldData field = DynaFlowAnalyzer.getField(fin.owner, fin.name);
                    Hook.DUAL_NODE.put(new RSField(field.bytecodeField, "dualPrevious"));
                    for(FieldData fd : md.fieldReferences){
                    	if(!fd.FIELD_NAME.equals(field.FIELD_NAME) && fd.CLASS_NAME.equals(cn.name)){
                            Hook.DUAL_NODE.put(new RSField(fd.bytecodeField, "dualNext"));
                    		break;
                    	}
                    }
				}
        	}
        }
    }
}
