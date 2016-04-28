package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

//All methods identified as of r113
public class SeekableFile extends Analyser {
	@Override
	public ClassSpec specify(ClassNode cn) {
		if(cn.access!=33)
			return null;
		if(!cn.ownerless())
			return null;
		return cn.fieldCount(Hook.FILE_ON_DISK.getInternalDesc())==1 ? new ClassSpec(Hook.SEEKABLE_FILE, cn) : null;
	}
	@Override
	public void evaluate(ClassNode cn) {
		for(MethodNode mn : cn.methods){
			if(mn.isStatic())
				continue;
			MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
			if(new Wildcard("(?)J").matches(mn.desc)){
				Hook.SEEKABLE_FILE.put(new RSMethod(mn, "getFileLength"));
				for(FieldData fd : md.fieldReferences){
					if(fd.CLASS_NAME.equals(cn.name) && fd.bytecodeField.desc.equals("J")){
						Hook.SEEKABLE_FILE.put(new RSField(fd.bytecodeField, "fileLength"));
						break;
					}
				}
			}
			if(new Wildcard("(?)V").matches(mn.desc)){
				if(new Wildcard("(J)V").matches(mn.desc)){
					Hook.SEEKABLE_FILE.put(new RSMethod(mn, "setPosition"));
					for(FieldData fd : md.fieldReferences){
						if(fd.CLASS_NAME.equals(cn.name) && fd.bytecodeField.desc.equals("J")){
							Hook.SEEKABLE_FILE.put(new RSField(fd.bytecodeField, "position"));
							break;
						}
					}
				}
				else{
					for(MethodData md2 : md.methodReferences){
						if(md2.CLASS_NAME.equals(cn.name) && new Wildcard("(?)V").matches(md2.METHOD_DESC)){
							Hook.SEEKABLE_FILE.put(new RSMethod(md.bytecodeMethod, "close"));
							Hook.SEEKABLE_FILE.put(new RSMethod(md2.bytecodeMethod, "finalizeWrite"));
							break;
						}
					}
				}
			}
			if(new Wildcard("([B?)V").matches(mn.desc)){
				Hook.SEEKABLE_FILE.put(new RSMethod(mn, "readBytes"));
				for(MethodData md2 : md.methodReferences){
					if(new Wildcard("([BII?)V").matches(md2.METHOD_DESC)){
						Hook.SEEKABLE_FILE.put(new RSMethod(md2.bytecodeMethod, "readSection"));
						for(MethodData md3 : md2.methodReferences){
							if(md3.CLASS_NAME.equals(cn.name) && new Wildcard("(?)V").matches(md3.METHOD_DESC)){
								Hook.SEEKABLE_FILE.put(new RSMethod(md3.bytecodeMethod, "finalizeRead"));
								break;
							}
						}
						break;
					}
				}
			}
			if(new Wildcard("([BII?)V").matches(mn.desc)){
				boolean isWrite=false;
				for(MethodData md2 : md.methodReferences){
					if(!md2.CLASS_NAME.equals(cn.name) && new Wildcard("([BII?)V").matches(md2.METHOD_DESC)){
						isWrite=true;
						break;
					}
				}
				if(isWrite)
					Hook.SEEKABLE_FILE.put(new RSMethod(md.bytecodeMethod, "writeBytes"));
			}
		}
		for(FieldNode fn : cn.fields){
			if(fn.isStatic())
				continue;
			if(fn.desc.equals(Hook.FILE_ON_DISK.getInternalDesc())){
				Hook.SEEKABLE_FILE.put(new RSField(fn, "file"));
			}
		}
	}
}
