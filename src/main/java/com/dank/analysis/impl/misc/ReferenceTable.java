package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMember;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

//All fields and methods identified as of r113
public class ReferenceTable extends Analyser {
	@Override
	public ClassSpec specify(ClassNode cn) {
		return cn.fieldCount(int[][].class) == 2 && cn.fieldCount(Object[].class) == 1 &&
				cn.fieldCount(Object[][].class) == 1 ? new ClassSpec(Hook.REFERENCE_TABLE, cn) : null;
	}
	@Override
	public void evaluate(ClassNode cn) {
		MethodData prepareChildBuffers = null;
		MethodData unpack = null;
		MethodData filesCompleted = null;
		for(MethodNode mn : cn.methods){
			if(mn.isStatic())
				continue;
			MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
			if(new Wildcard("(II?)Z").matches(mn.desc)){
				Hook.REFERENCE_TABLE.put(new RSMethod(mn, "hasEntryBuffer"));
			}
			if(new Wildcard("(II?)[B").matches(mn.desc)){
				Hook.REFERENCE_TABLE.put(new RSMethod(mn, "getFile2"));
				for(MethodData md2 : md.referencedFrom){
					if(new Wildcard("(I?)[B").matches(md2.METHOD_DESC)){
						Hook.REFERENCE_TABLE.put(new RSMethod(md2.bytecodeMethod, "getFile"));
						break;
					}
				}
			}
			if(new Wildcard("(II[I?)[B").matches(mn.desc)){
				Hook.REFERENCE_TABLE.put(new RSMethod(mn, "getFile3"));
				for(MethodData md2 : md.referencedFrom){
					if(new Wildcard("(II?)[B").matches(md2.METHOD_DESC)){
						Hook.REFERENCE_TABLE.put(new RSMethod(md2.bytecodeMethod, "getFile4"));
						for(MethodData md3 : md2.referencedFrom){
							if(md3.CLASS_NAME.equals(cn.name) && new Wildcard("(I?)[B").matches(md3.METHOD_DESC)){
								Hook.REFERENCE_TABLE.put(new RSMethod(md3.bytecodeMethod, "getFile5"));
								break;
							}
						}
						break;
					}
				}
			}
			if(new Wildcard("([B?)V").matches(mn.desc)){
				Hook.REFERENCE_TABLE.put(new RSMethod(mn, "unpackTable"));
				unpack=DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
			}
			if(new Wildcard("(I[I?)Z").matches(mn.desc)){
				Hook.REFERENCE_TABLE.put(new RSMethod(mn, "prepareChildBuffers"));
				prepareChildBuffers=DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
			}
			if(new Wildcard("(?)Z").matches(mn.desc)){
				Hook.REFERENCE_TABLE.put(new RSMethod(mn, "filesCompleted"));
				filesCompleted=DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
			}
			if(new Wildcard("(?)V").matches(mn.desc)){
				boolean clearBuffers=false;
				for(FieldData fd : md.fieldReferences){
					if(fd.bytecodeField.desc.equals("[[Ljava/lang/Object;"))
						clearBuffers=true;
				}
				if(clearBuffers)
					Hook.REFERENCE_TABLE.put(new RSMethod(mn, "clearChildBuffers"));
				else{
					
				}
			}
		}
		if(filesCompleted!=null){
			for(FieldData fd : filesCompleted.fieldReferences){
				if(fd.bytecodeField.desc.equals("[I"))
					Hook.REFERENCE_TABLE.put(new RSField(fd.bytecodeField, "entryIndices"));
			}
		}
		if(unpack!=null){
			List<AbstractInsnNode> pattern = Assembly.find(unpack.bytecodeMethod,
					Mask.GETFIELD.describe("[[I"),
					Mask.INVOKESPECIAL.describe("([I)V").own(Hook.LOOKUP_TABLE.getInternalName()).distance(4)
					);
			if (pattern != null) {
				FieldInsnNode fin = (FieldInsnNode)pattern.get(0);
				Hook.REFERENCE_TABLE.put(new RSField(fin, "childIdentifiers"));
			}
			pattern = Assembly.find(unpack.bytecodeMethod,
					Mask.GETFIELD.describe("[I"),
					Mask.INVOKESPECIAL.describe("([I)V").own(Hook.LOOKUP_TABLE.getInternalName()).distance(4)
					);
			if (pattern != null) {
				FieldInsnNode fin = (FieldInsnNode)pattern.get(0);
				Hook.REFERENCE_TABLE.put(new RSField(fin, "entryIdentifiers"));
			}
		}
		if(prepareChildBuffers!=null){
			List<AbstractInsnNode> pattern = Assembly.find(prepareChildBuffers.bytecodeMethod,
					Mask.INVOKEVIRTUAL.describe("(I)Ljava/lang/StringBuilder;"),
					Mask.LDC,
					Mask.INVOKEVIRTUAL.describe("(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
					Mask.GETFIELD.describe("[I").distance(4),
					Mask.INVOKEVIRTUAL.describe("(I)Ljava/lang/StringBuilder;").distance(4),
					Mask.LDC,
					Mask.INVOKEVIRTUAL.describe("(Ljava/lang/String;)Ljava/lang/StringBuilder;")
					);
			FieldInsnNode entryCrc=null;
			if (pattern != null) {
				FieldInsnNode fin = (FieldInsnNode)pattern.get(3);
				entryCrc=fin;
				Hook.REFERENCE_TABLE.put(new RSField(fin, "entryCrcs"));
			}
			pattern = Assembly.find(prepareChildBuffers.bytecodeMethod,
					Mask.GETFIELD.describe("Z"),
					Mask.GETFIELD.distance(5).describe("[Ljava/lang/Object;")
					);
			FieldInsnNode discardEntryBuffers=null;
			if (pattern != null) {
				FieldInsnNode fin = (FieldInsnNode)pattern.get(0);
				discardEntryBuffers=fin;
				Hook.REFERENCE_TABLE.put(new RSField(fin, "discardEntryBuffers"));
			}
			for(FieldData fd : prepareChildBuffers.fieldReferences){
				if(fd.CLASS_NAME.equals(cn.name) && fd.bytecodeField.desc.equals("Z") && !fd.FIELD_NAME.equals(discardEntryBuffers.name))
					Hook.REFERENCE_TABLE.put(new RSField(fd.bytecodeField, "encrypted"));
				if(fd.CLASS_NAME.equals(cn.name) && fd.bytecodeField.desc.equals("[I") && !fd.FIELD_NAME.equals(entryCrc.name))
					Hook.REFERENCE_TABLE.put(new RSField(fd.bytecodeField, "entryChildCounts"));
				if(fd.CLASS_NAME.equals(cn.name) && fd.bytecodeField.desc.equals("I"))
					Hook.REFERENCE_TABLE.put(new RSField(fd.bytecodeField, "discardUnpacked"));
				if(fd.CLASS_NAME.equals(cn.name) && fd.bytecodeField.desc.equals("[[I")){
					Hook.REFERENCE_TABLE.put(new RSField(fd.bytecodeField, "childIndices"));
					for(MethodData md : fd.referencedFrom){
						if(new Wildcard("(I?)[I").matches(md.METHOD_DESC)){
							Hook.REFERENCE_TABLE.put(new RSMethod(md.bytecodeMethod, "getChildIndices"));
							break;
						}
					}
				}
			}
		}
		for(FieldNode fn : cn.fields){
			if(fn.isStatic())
				continue;
			FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
			if(fn.desc.equals("[L"+Hook.LOOKUP_TABLE.getInternalName()+";")){
				Hook.REFERENCE_TABLE.put(new RSField(fn, "childIdentityTables"));
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(Ljava/lang/String;Ljava/lang/String;?)[B").matches(md.METHOD_DESC)){
						Hook.REFERENCE_TABLE.put(new RSMethod(md.bytecodeMethod, "getFileBytes"));
						break;
					}
				}
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(ILjava/lang/String;?)I").matches(md.METHOD_DESC)){
						Hook.REFERENCE_TABLE.put(new RSMethod(md.bytecodeMethod, "getChildIdentifier"));
						break;
					}
				}
			}
			if(fn.desc.equals("L"+Hook.LOOKUP_TABLE.getInternalName()+";")){
				Hook.REFERENCE_TABLE.put(new RSField(fn, "entryIdentityTable"));
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(Ljava/lang/String;?)I").matches(md.METHOD_DESC)){
						Hook.REFERENCE_TABLE.put(new RSMethod(md.bytecodeMethod, "getEntryIdentifier"));
						break;
					}
				}
			}
			if(fn.desc.equals("[[Ljava/lang/Object;")){
				Hook.REFERENCE_TABLE.put(new RSField(fn, "childBuffers"));
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(I?)I").matches(md.METHOD_DESC)){
						Hook.REFERENCE_TABLE.put(new RSMethod(md.bytecodeMethod, "getChildBufferLengthAtIndex"));
						break;
					}
				}
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(?)I").matches(md.METHOD_DESC)){
						Hook.REFERENCE_TABLE.put(new RSMethod(md.bytecodeMethod, "getChildBufferLength"));
						break;
					}
				}
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(I?)V").matches(md.METHOD_DESC)){
						Hook.REFERENCE_TABLE.put(new RSMethod(md.bytecodeMethod, "clearChildBuffer"));
						break;
					}
				}
			}
			if(fn.desc.equals("[Ljava/lang/Object;")){
				Hook.REFERENCE_TABLE.put(new RSField(fn, "entryBuffers"));
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(I?)Z").matches(md.METHOD_DESC)){
						Hook.REFERENCE_TABLE.put(new RSMethod(md.bytecodeMethod, "hasFileBuffer"));
						break;
					}
				}
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(II?)Z").matches(md.METHOD_DESC)){
						Hook.REFERENCE_TABLE.put(new RSMethod(md.bytecodeMethod, "hasEntryBuffer"));
						for(MethodData md2 : md.methodReferences){
							if(new Wildcard("(I?)V").matches(md2.METHOD_DESC)){
								Hook.REFERENCE_TABLE.put(new RSMethod(md2.bytecodeMethod, "loadBuffer"));
								break;
							}
						}
						for(MethodData md2 : md.referencedFrom){
							if(new Wildcard("(Ljava/lang/String;Ljava/lang/String;?)Z").matches(md2.METHOD_DESC)){
								Hook.REFERENCE_TABLE.put(new RSMethod(md2.bytecodeMethod, "hasFileLoaded"));
								break;
							}
						}
						break;
					}
				}
			}
		}

		RSMember entryChildCounts = Hook.REFERENCE_TABLE.get("entryChildCounts");
		RSMember entryIndices = Hook.REFERENCE_TABLE.get("entryIndices");
		RSMember entryIdentifiers = Hook.REFERENCE_TABLE.get("entryIdentifiers");
		RSMember entryCrcs = Hook.REFERENCE_TABLE.get("entryCrcs");
		boolean found=false;
		RSMember discardUnpacked = Hook.REFERENCE_TABLE.get("discardUnpacked");
		if(unpack!=null && entryChildCounts!=null && entryIndices!=null && entryIdentifiers!=null && entryCrcs!=null && discardUnpacked!=null){
			for(FieldData fd : unpack.fieldReferences){
				if(fd.bytecodeField.desc.equals("I")){
					if(fd.FIELD_NAME.equals(discardUnpacked.name))
						continue;
					Hook.REFERENCE_TABLE.put(new RSField(fd.bytecodeField, "entryIndexCount"));
				}
				if(fd.bytecodeField.desc.equals("[I")){
					if(fd.FIELD_NAME.equals(entryChildCounts.name) || fd.FIELD_NAME.equals(entryIdentifiers.name) || 
							fd.FIELD_NAME.equals(entryIndices.name) || fd.FIELD_NAME.equals(entryCrcs.name))
						continue;
					if(!found){
						found=true;
						Hook.REFERENCE_TABLE.put(new RSField(fd.bytecodeField, "childIndexCounts"));
					}
				}
			}
		}
	}
}
