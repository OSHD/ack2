package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

//All fields and methods identified as of r111
public class GameCanvas extends Analyser {
	@Override
	public ClassSpec specify(ClassNode cn) {
		return cn.superName("java/awt/Canvas") ? new ClassSpec(Hook.GAME_CANVAS, cn) : null;
	}
	@Override
	public void evaluate(ClassNode cn) {
		for (FieldNode field : cn.fields) {
			if (field.desc.equals("Ljava/awt/Component;")) {
				Hook.GAME_CANVAS.put(new RSField(field, "component"));
			}
		}
	}
}
