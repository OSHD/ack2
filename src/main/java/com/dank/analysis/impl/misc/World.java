package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

//All fields identified as of r113
public class World extends Analyser {
	@Override
	public ClassSpec specify(ClassNode cn) {
		if(cn.access!=33)
			return null;
		if(!cn.ownerless())
			return null;
		return cn.fieldCount("Ljava/lang/String;")==2 && cn.fieldCount("I")==5 ? new ClassSpec(Hook.WORLD, cn) : null;
	}

	@Override
	public void evaluate(ClassNode cn) {
		for(FieldNode fn : cn.fields){
			if(fn.isStatic())
				continue;
			FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
			if(fn.desc.equals("Ljava/lang/String;")){
				boolean isActivity=false;
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(L"+Hook.WORLD.getInternalName()+";L"+Hook.WORLD.getInternalName()+";IZ?)I").matches(md.bytecodeMethod.desc)){
						isActivity=true;
					}
				}
				if(isActivity){
					Hook.WORLD.put(new RSField(fd.bytecodeField, "activity"));
				}
				else{
					Hook.WORLD.put(new RSField(fd.bytecodeField, "domain"));
				}
			}

			if(fn.desc.equals("I")){
				boolean isMask=false;
				boolean isWorld=false;
				for(MethodData md : fd.referencedFrom){
					if(!md.bytecodeMethod.isStatic() && new Wildcard("(?)Z").matches(md.METHOD_DESC))
						isMask=true;
					if(new Wildcard("(?)V").matches(md.METHOD_DESC))
						isWorld=true;
				}
				if(isWorld){
					if(isMask){
						Hook.WORLD.put(new RSField(fd.bytecodeField, "mask"));
					}
					else{
						Hook.WORLD.put(new RSField(fd.bytecodeField, "world"));
					}
				}
				else{
					boolean isIndex=false;
					for(MethodData md : fd.referencedFrom){
						if(new Wildcard("([L"+Hook.WORLD.getInternalName()+";II[I[I?)V").matches(md.METHOD_DESC))
							isIndex=true;
					}
					if(isIndex){
						boolean isPopulation=false;
						for(MethodData md : fd.referencedFrom){
							if(new Wildcard("(L"+Hook.SCRIPT_EVENT.getInternalName()+";I?)V").matches(md.METHOD_DESC))
								isPopulation=true;
						}
						if(isPopulation)
							Hook.WORLD.put(new RSField(fd.bytecodeField, "population"));
						else
							Hook.WORLD.put(new RSField(fd.bytecodeField, "index"));
					}
					else{
						Hook.WORLD.put(new RSField(fd.bytecodeField, "location"));
					}
				}
			}
		}
	}
}
