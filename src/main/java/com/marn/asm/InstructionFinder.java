package com.marn.asm;

import java.util.ArrayList;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;

public class InstructionFinder {
	private MethodNode currentMethod;
	public InstructionFinder(MethodNode method){
		this.currentMethod=method;
	}
	public ArrayList<AbstractInsnNode[]> findPattern(String pattern){
		ArrayList<AbstractInsnNode[]> matches = new ArrayList<AbstractInsnNode[]>();
		if(pattern==null || pattern.equals(""))
			return matches;
		ArrayList<String> patternInstructions = new ArrayList<String>();
		String temp = pattern;
		if(temp.indexOf(" ")>0){
			for(String s=temp.substring(0, temp.indexOf(" "));temp.indexOf(" ")>0;s=temp.substring(0, (temp.indexOf(" ")>0?temp.indexOf(" "):temp.length()))){
				patternInstructions.add(s);
				temp=temp.substring(s.length()+1, temp.length());
			}
		}
		patternInstructions.add(temp);
		for(int i=0;i<currentMethod.instructions.size();++i){
			if(i+patternInstructions.size()>currentMethod.instructions.size())
				break;
			AbstractInsnNode curr = currentMethod.instructions.get(i);
			while(curr.getOpcode()<0 && curr.getNext()!=null)
				curr=curr.getNext();
			boolean found=true;
			for(int k=0;k<patternInstructions.size();++k){
				AbstractInsnNode instruction = currentMethod.instructions.get(i+k);
				while(instruction.getOpcode()<0 && instruction.getNext()!=null)
					instruction=instruction.getNext();
				if(i+k>currentMethod.instructions.size()){
					found=false;
					break;
				}
				if(patternInstructions.get(k).equalsIgnoreCase("instruction")){}
				else if(patternInstructions.get(k).equalsIgnoreCase("ifinstruction")){
					int opcode = instruction.getOpcode();
					if(opcode>166 || opcode<153){
						found=false;
						break;
					}
				}
				else if(patternInstructions.get(k).equalsIgnoreCase("invoke")){
					int opcode = instruction.getOpcode();
					if(opcode>181 || opcode<187){
						found=false;
						break;
					}
				}
				else if(Printer.OPCODES[instruction.getOpcode()].equalsIgnoreCase(patternInstructions.get(k))){
				}
				else{
					found=false;
					break;
				}
			}
			if(found){
				AbstractInsnNode[] match = new AbstractInsnNode[patternInstructions.size()];
				for(int k=0;k<patternInstructions.size();++k){
					AbstractInsnNode instruction = currentMethod.instructions.get(i+k);
					while(instruction.getOpcode()<0 && instruction.getNext()!=null)
						instruction=instruction.getNext();
					match[k]=instruction;
				}
				matches.add(match);
			}
		}
		return matches;
	}
}
