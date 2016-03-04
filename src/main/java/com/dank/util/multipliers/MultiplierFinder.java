package com.dank.util.multipliers;

import com.dank.DankEngine;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;

public class MultiplierFinder {
	
	public static int findMultiplier(String clazz, String field){
			HashMap<String, Integer> multipliers = new HashMap<String, Integer>();
		for(ClassNode cn : DankEngine.classPath.getClasses()) {
			for(MethodNode mn : cn.methods) {
				for(AbstractInsnNode instr : mn.instructions.toArray()){
					if(instr.opcode()==Opcodes.GETFIELD || instr.opcode()==Opcodes.GETSTATIC){
						FieldInsnNode fieldInsn = (FieldInsnNode)instr;
						if(fieldInsn.owner.equals(clazz) && fieldInsn.name.equals(field)){
							AbstractInsnNode multiInsn = instr;
							for(int i=(fieldInsn.opcode()==Opcodes.GETSTATIC?2:5);i>0;--i){
								if(multiInsn.opcode()==Opcodes.IMUL)
									break;
								if(multiInsn.next()==null)
									break;
								multiInsn=multiInsn.next();
							}
							if(multiInsn.opcode()!=Opcodes.IMUL)
								continue;
							for(int i=(fieldInsn.opcode()==Opcodes.GETSTATIC?2:5);i>0;--i){
								if(multiInsn.opcode()==Opcodes.LDC)
									break;
								if(multiInsn.previous()==null)
									break;
								if(multiInsn.opcode()==Opcodes.AALOAD){
									multiInsn.previous();
									multiInsn.previous();
									multiInsn.previous();
								}
								multiInsn=multiInsn.previous();
							}
							if(multiInsn.opcode()==Opcodes.LDC){
								LdcInsnNode insn = (LdcInsnNode)multiInsn;
								int value = (Integer)insn.cst;
								if(value!=0 && value%2!=0){
									if(multipliers.containsKey(value+""))
										multipliers.put(value+"", multipliers.get(value+"")+1);
									else
										multipliers.put(value+"", 1);
								}
							}
						}
					}
				}
			}
		}		
		int max = 0;
		int multi=1;

		for(String s : multipliers.keySet()){
			if(multipliers.get(s)>max){
				multi=Integer.parseInt(s);
				max = multipliers.get(s);
			}
		}
		return (multi == -1 ? 0 : multi);
	}
}