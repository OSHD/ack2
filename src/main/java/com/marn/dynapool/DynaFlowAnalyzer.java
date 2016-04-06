package com.marn.dynapool;

import java.util.HashMap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import com.dank.asm.ClassPath;
import com.marn.asm.ClassData;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;

public class DynaFlowAnalyzer {
	public static HashMap<String, ClassData> clientClasses = new HashMap<String, ClassData>();
	public static HashMap<String, FieldData> clientFields = new HashMap<String, FieldData>();
	public static HashMap<String, MethodData> clientMethods = new HashMap<String, MethodData>();
	public static void loadClient(ClassPath jarContents){
		long start = System.currentTimeMillis();
		System.out.println();
		System.out.println("Building ASM DynaNode pool...");
		int linkCounter = 0;
		System.out.println("Parsing client classes...");
		for(ClassNode cn : jarContents.getClasses()){
			clientClasses.put(cn.name, new ClassData(cn));
		}

		System.out.println("Done parsing "+clientClasses.size()+" classes!");
		System.out.println("Parsing client fields...");
		for(ClassData cd : clientClasses.values()){
			for (FieldNode fn : cd.bytecodeClass.fields) {
				FieldData fd = new FieldData(cd.CLASS_NAME, fn);
				clientFields.put(cd.CLASS_NAME + "." + fn.name, fd);
				cd.addField(fd);
				linkCounter++;
			}
			clientClasses.put(cd.CLASS_NAME, cd);
		}
		System.out.println("Done parsing "+clientFields.size()+" fields!");
		System.out.println("Parsing client methods...");
		for(ClassData cd : clientClasses.values()){
			for (MethodNode mn : cd.bytecodeClass.methods) {
				MethodData md = new MethodData(cd.CLASS_NAME, mn);
				clientMethods.put(cd.CLASS_NAME+"."+mn.name+mn.desc, md);
				cd.addMethod(md);
				linkCounter++;
			}
			clientClasses.put(cd.CLASS_NAME, cd);
		}
		System.out.println("Done parsing "+clientMethods.size()+" methods!");
		System.out.println("Done building node pool with "+linkCounter+" node links made! ("+(System.currentTimeMillis()-start)+"ms)");
		System.out.println();
		start = System.currentTimeMillis();
		System.out.println("Building reference tree...");
		int fieldCount=0;
		int invokeCount=0;
		int checkcastCount=0;
		int instanceCount=0;
		HashMap<String, ClassData> refreshClasses = new HashMap<String, ClassData>();
		HashMap<String, MethodData> refreshMethods = new HashMap<String, MethodData>();
		HashMap<String, FieldData> refreshFields = new HashMap<String, FieldData>();
		for (String s : clientClasses.keySet()) {
			ClassData cd = clientClasses.get(s);
			for (FieldData fd : cd.fields) {
				ClassData cd2 = clientClasses.get(fd.bytecodeField.desc.replace("L", "").replace(";", ""));
				if(cd2!=null){
					if(refreshClasses.containsKey(cd2.CLASS_NAME))
						cd2 = refreshClasses.get(cd2.CLASS_NAME);
					cd2.addInstanceReference(fd);
					refreshClasses.put(cd2.CLASS_NAME, cd2);
					instanceCount++;
				}
			}
		}
		for(ClassData cd : refreshClasses.values()){
			clientClasses.put(cd.CLASS_NAME, cd);
		}
		for (MethodData md : clientMethods.values()){
			if(refreshMethods.containsKey(md.real_attributes()))
				md = refreshMethods.get(md.real_attributes());
			for (AbstractInsnNode instr : md.bytecodeMethod.instructions.toArray()) {
				if (instr.getOpcode() == Opcodes.GETFIELD || instr.getOpcode() == Opcodes.PUTFIELD ||
						instr.getOpcode() == Opcodes.GETSTATIC || instr.getOpcode() == Opcodes.PUTSTATIC) {
					FieldInsnNode fins = (FieldInsnNode) instr;
					if (clientFields.containsKey(fins.owner+"."+fins.name)){
						FieldData fd = clientFields.get(fins.owner+"."+fins.name);
						if(fd!=null){
							fd.addReferenceFrom(md);
							md.addFieldReference(fd);
							refreshFields.put(fins.owner+"."+fins.name, fd);
							fieldCount++;
						}
					}
				}
				if(instr.getOpcode()==Opcodes.INVOKEDYNAMIC || instr.getOpcode()==Opcodes.INVOKEINTERFACE ||
						instr.getOpcode()==Opcodes.INVOKESPECIAL || instr.getOpcode()==Opcodes.INVOKESTATIC ||
						instr.getOpcode()==Opcodes.INVOKEVIRTUAL){
					MethodInsnNode mins = (MethodInsnNode) instr;
					if (clientMethods.containsKey(mins.owner + "." + mins.name+mins.desc)){
						MethodData methodInvoking = clientMethods.get(mins.owner+"."+mins.name+mins.desc);
						if(refreshMethods.containsKey(methodInvoking.real_attributes()))
							methodInvoking = refreshMethods.get(methodInvoking.real_attributes());
						md.addMethodReference(methodInvoking);
						methodInvoking.addReferenceFrom(md);
						invokeCount++;
					}
				}
				if(instr.getOpcode()==Opcodes.CHECKCAST){
					TypeInsnNode insn = (TypeInsnNode)instr;
					ClassData cd = clientClasses.get(insn.desc);
					if(cd!=null){
						if(refreshClasses.containsKey(cd.CLASS_NAME))
							cd = refreshClasses.get(cd.CLASS_NAME);
						cd.addMethod(md);
						refreshClasses.put(cd.CLASS_NAME, cd);
						checkcastCount++;
					}
				}
			}
			refreshMethods.put(md.real_attributes(), md);
		}
		System.out.println("Finished building reference tree with "+(fieldCount+invokeCount+checkcastCount+instanceCount)+" total references! ("+(System.currentTimeMillis()-start)+"ms)");
		System.out.println("\tField References : "+fieldCount+" Invokes : "+invokeCount+"\n\tCheckcasts : "+checkcastCount+" Instances : "+instanceCount);
		start = System.currentTimeMillis();
		System.out.println("Refreshing ASM DynaNode pool...");
		int nodeCounter=0;
		for(ClassData cd : refreshClasses.values()){
			clientClasses.put(cd.CLASS_NAME, cd);
			nodeCounter++;
		}
		for(MethodData md : refreshMethods.values()){
			clientMethods.put(md.real_attributes(), md);
			nodeCounter++;
		}
		for(FieldData fd : refreshFields.values()){
			clientFields.put(fd.CLASS_NAME+"."+fd.FIELD_NAME, fd);
			nodeCounter++;
		}
		System.out.println("Refreshed node pool; "+nodeCounter+" total nodes refreshed! ("+(System.currentTimeMillis()-start)+"ms)");
	}
	public static ClassData getClass(String name){
		return clientClasses.get(name);
	}
	public static FieldData getField(String owner, String name){
		return clientFields.get(owner+"."+name);
	}
	public static MethodData getMethod(String owner, String name, String desc){
		return clientMethods.get(owner+"."+name+desc);
	}
}
