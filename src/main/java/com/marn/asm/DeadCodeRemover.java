package com.marn.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;

import com.dank.asm.ClassPath;

public class DeadCodeRemover {
	public static void removeDeadCode(ClassPath cp){
		long start=System.currentTimeMillis();
		System.out.println("Removing dead code...");
		for(ClassNode cn : cp.getClasses())
			removeDeadCode(cn);
		System.out.println("Removed dead code in "+(System.currentTimeMillis()-start)+"ms");
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void removeDeadCode(ClassNode cn){
		org.objectweb.asm.tree.analysis.Analyzer asmAnalyzer = new org.objectweb.asm.tree.analysis.Analyzer(new BasicInterpreter());
		for(MethodNode mn : cn.methods){
			try{
				asmAnalyzer.analyze(cn.name, mn);
				Frame[] analyzerFrames = asmAnalyzer.getFrames();
				AbstractInsnNode[] ains = mn.instructions.toArray();
				for(int i = 0; i < analyzerFrames.length; i++) {
					if(analyzerFrames[i] == null && !(ains[i] instanceof LabelNode)) {
						mn.instructions.remove(ains[i]);
					}
				}

			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
