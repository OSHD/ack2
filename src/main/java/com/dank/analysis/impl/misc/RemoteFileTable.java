package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMember;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

public class RemoteFileTable extends Analyser {
	@Override
	public ClassSpec specify(ClassNode cn) {
		return cn.superName.equals(Hook.REFERENCE_TABLE.getInternalName()) ? new ClassSpec(Hook.REMOTE_FILE_TABLE, cn) : null;
	}
	@Override
	public void evaluate(ClassNode cn) {
		RSMember loadBuffer = Hook.REFERENCE_TABLE.get("loadBuffer");
		if(loadBuffer!=null){
			MethodData md = DynaFlowAnalyzer.getMethod(cn.name, loadBuffer.name, loadBuffer.desc);
			if(md!=null){
				Hook.REMOTE_FILE_TABLE.put(new RSMethod(md.bytecodeMethod, "loadBuffer"));
			}
		}
		for(FieldNode fn : cn.fields){
			if(fn.isStatic())
				continue;
			FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
			if(fn.desc.equals("Z")){
				boolean isReqChild = false;
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(?)V").matches(md.METHOD_DESC)){
						isReqChild=true;
						break;
					}
				}
				if(isReqChild){
					Hook.REMOTE_FILE_TABLE.put(new RSField(fd.bytecodeField, "requestingChildren"));
					for(MethodData md : fd.referencedFrom){
						if(new Wildcard("(?)V").matches(md.METHOD_DESC)){
							Hook.REMOTE_FILE_TABLE.put(new RSMethod(md.bytecodeMethod, "processChildRequests"));
						}
					}
				}
				else{
					
				}
			}
			if(fn.desc.equals("I")){
				boolean isIndex = false;
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(I[BZZ?)V").matches(md.METHOD_DESC)){
						isIndex=true;
						break;
					}
				}
				if(isIndex){
					Hook.REMOTE_FILE_TABLE.put(new RSField(fd.bytecodeField, "index"));
				}
				else{
					
				}
			}
			if(fn.desc.equals(Hook.CACHE_FILE.getInternalDesc())){
				boolean isCache=false;
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(I?)V").matches(md.METHOD_DESC)){
						isCache=true;
						break;
					}
				}
				if(isCache)
					Hook.REMOTE_FILE_TABLE.put(new RSField(fd.bytecodeField, "cacheFile"));
				else
					Hook.REMOTE_FILE_TABLE.put(new RSField(fd.bytecodeField, "referenceFile"));
			}
		}
	}
}
