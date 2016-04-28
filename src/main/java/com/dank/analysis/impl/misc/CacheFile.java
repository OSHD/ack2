package com.dank.analysis.impl.misc;

import java.util.HashMap;

import org.objectweb.asm.tree.ClassNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.ClassData;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

//All fields and methods identified as of r113
public class CacheFile extends Analyser {
	@Override
	public ClassSpec specify(ClassNode cn) {
		if(cn.access!=49)
			return null;
		if(!cn.ownerless())
			return null;
		return cn.fieldCount(Hook.SEEKABLE_FILE.getInternalDesc())==2 && cn.fieldCount(int.class)==2 ? new ClassSpec(Hook.CACHE_FILE, cn) : null;
	}
	@Override
	public void evaluate(ClassNode cn) {
		ClassData cd = DynaFlowAnalyzer.getClass(cn.name);
		if(cd!=null){
			for(MethodData md : cd.methods){
				if(md.bytecodeMethod.isStatic())
					continue;
				if(new Wildcard("(I[BI?)Z").matches(md.bytecodeMethod.desc)){
					Hook.CACHE_FILE.put(new RSMethod(md.bytecodeMethod, "writeFile"));
					for(FieldData fd : md.fieldReferences){
						if(fd.CLASS_NAME.equals(cn.name) && fd.bytecodeField.desc.equals("I")){
							Hook.CACHE_FILE.put(new RSField(fd.bytecodeField, "length"));
							break;
						}
					}
				}
				if(new Wildcard("(I[BIZ?)Z").matches(md.bytecodeMethod.desc)){
					Hook.CACHE_FILE.put(new RSMethod(md.bytecodeMethod, "writeFileParts"));
					for(FieldData fd : md.fieldReferences){
						if(fd.CLASS_NAME.equals(cn.name) && fd.bytecodeField.desc.equals("I")){
							Hook.CACHE_FILE.put(new RSField(fd.bytecodeField, "cacheId"));
							break;
						}
					}
				}
				if(new Wildcard("(I?)[B").matches(md.bytecodeMethod.desc)){
					Hook.CACHE_FILE.put(new RSMethod(md.bytecodeMethod, "readFile"));
				}
			}
			for(FieldData fd : cd.fields){
				if(fd.bytecodeField.isStatic())
					continue;
				if(fd.bytecodeField.desc.equals(Hook.SEEKABLE_FILE.getInternalDesc())){
					boolean isData=false;//else index
					for(MethodData md : fd.referencedFrom){
						if(new Wildcard("(I[BI?)Z").matches(md.bytecodeMethod.desc)){
							isData=true;
							break;
						}
					}
					if(isData){
						Hook.CACHE_FILE.put(new RSField(fd.bytecodeField, "dataFile"));
					}
					else{
						Hook.CACHE_FILE.put(new RSField(fd.bytecodeField, "indexFile"));
					}
				}
			}
		}
	}
}
