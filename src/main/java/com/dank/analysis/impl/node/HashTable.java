package com.dank.analysis.impl.node;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
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

//All fields and methods identified as of r111
public class HashTable extends Analyser {
	@Override
	public ClassSpec specify(ClassNode cn) {
		return cn.fieldCount("I") == 2 && cn.fieldCount('L' + Hook.NODE.getInternalName()+ ';') == 2 && cn.ownerless() ? new ClassSpec(Hook.HASHTABLE, cn) : null;
	}
	@Override
	public void evaluate(ClassNode cn) {
		for(MethodNode mn : cn.methods){
			MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
			if(md!=null){
				if(mn.access==1 && new Wildcard("(L"+Hook.NODE.getInternalName()+";J)V").matches(mn.desc) && md.referencedFrom.size()>0) {
					Hook.HASHTABLE.put(new RSMethod(mn, "put"));
				}
				if(mn.access==1 && new Wildcard("(J)L"+Hook.NODE.getInternalName()+";").matches(mn.desc) && md.referencedFrom.size()>0) {
					Hook.HASHTABLE.put(new RSMethod(mn, "get"));
				}
				if(mn.access==1 && new Wildcard("()L"+Hook.NODE.getInternalName()+";").matches(mn.desc) && md.referencedFrom.size()>0) {
					List<AbstractInsnNode> pattern = Assembly.find(mn,
							Mask.GETFIELD
							);
					if (pattern != null) {
						Hook.HASHTABLE.put(new RSMethod(mn, "next"));
					}
					else{
						Hook.HASHTABLE.put(new RSMethod(mn, "resetIndex"));
					}
				}
				if(mn.access==0 && new Wildcard("()V").matches(mn.desc) && md.referencedFrom.size()>0) {
					Hook.HASHTABLE.put(new RSMethod(mn, "clear"));
				}
			}
		}
		for (FieldNode fn : cn.fields) {
			if(fn.isStatic())
				continue;
			FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
			if (fn.desc.equals("[L"+Hook.NODE.getInternalName()+";")) {
				Hook.HASHTABLE.put(new RSField(fn, "buckets"));
			}
			if(fn.desc.equals("I")){
				boolean index=false;
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("()V").matches(md.METHOD_DESC)){
						index=true;
						break;
					}
				}
				if(index)
					Hook.HASHTABLE.put(new RSField(fn, "index"));
				else
					Hook.HASHTABLE.put(new RSField(fn, "size"));
			}
			if(fn.desc.equals("L"+Hook.NODE.getInternalName()+";")){
				boolean head=false;
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(J)L"+Hook.NODE.getInternalName()+";").matches(md.METHOD_DESC)){
						head=true;
						break;
					}
				}
				if(head)
					Hook.HASHTABLE.put(new RSField(fn, "head"));
				else
					Hook.HASHTABLE.put(new RSField(fn, "tail"));
			}
		}
	}
}
