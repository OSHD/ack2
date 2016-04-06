package com.dank.analysis.impl.misc;

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

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.awt.*;
import java.lang.reflect.Modifier;
import java.util.List;

//All fields and methods (non abstract) identified as of r111
public class Bitmap extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        if (Modifier.isAbstract(cn.access) && cn.fieldCount(Image.class) == 1) {
            return new ClassSpec(Hook.BITMAP, cn);
        }
        return null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(MethodNode mn : cn.methods){
    		if(mn.isStatic())
    			continue;
    		MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
    		if(mn.access==17 && md.referencedFrom.size()>0 && new Wildcard("(?)V").matches(mn.desc)){
                Hook.BITMAP.put(new RSMethod(mn, "drawGraphics"));
                List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
						Mask.GETFIELD.own(cn.name).describe("[I"),
						Mask.GETFIELD.own(cn.name).distance(3).describe("I"),
						Mask.GETFIELD.own(cn.name).distance(3).describe("I"),
						Mask.INVOKESTATIC.describe("([III)V")
						);
				if (pattern != null) {
					FieldInsnNode f1 = (FieldInsnNode)pattern.get(0);
					FieldInsnNode f2 = (FieldInsnNode)pattern.get(1);
					FieldInsnNode f3 = (FieldInsnNode)pattern.get(2);
					FieldData pixels = DynaFlowAnalyzer.getField(f1.owner, f1.name);
					FieldData width = DynaFlowAnalyzer.getField(f2.owner, f2.name);
					FieldData height = DynaFlowAnalyzer.getField(f3.owner, f3.name);

		            Hook.BITMAP.put(new RSField(pixels.bytecodeField, "pixels"));
		            Hook.BITMAP.put(new RSField(width.bytecodeField, "width"));
		            Hook.BITMAP.put(new RSField(height.bytecodeField, "height"));
				}
    		}
    	}
        cn.fields.stream().filter(field -> field.desc.equals("Ljava/awt/Image;")).forEach(node -> {
            Hook.BITMAP.put(new RSField(node, "image"));
        });
    }
}
