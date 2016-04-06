package com.marn.asm;
/******************************************************
 * Created by Marneus901                                *
 * � 2012-2014                                        *
 * **************************************************** *
 * Access to this source is unauthorized without prior  *
 * authorization from its appropriate author(s).        *
 * You are not permitted to release, nor distribute this* 
 * work without appropriate author(s) authorization.    *
 ********************************************************/
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.asm.Mask;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class FieldData {
    public String CLASS_NAME;
    public String FIELD_NAME;
    public String REFACTORED_NAME;
    public FieldNode bytecodeField;
    public ArrayList<MethodData> referencedFrom = new ArrayList<MethodData>();

    public void setRefactoredName(String s){
    	REFACTORED_NAME=s;
    }
    public FieldData(String clazz, FieldNode node) {
        CLASS_NAME = clazz;
        FIELD_NAME = node.name;
        REFACTORED_NAME = node.name;
        bytecodeField = node;
    }
    public void addReferenceFrom(MethodData mn) {
        if (!referencedFrom.contains(mn))
            referencedFrom.add(mn);
    }
    private boolean doArgsMatch(FieldSigniture ms, String[] args) {
        for (int i = 0; i < ms.args.length; ++i) {
            if (ms.args[i].equals("?")) {
                continue;
            }
            if (ms.args[i].startsWith("[") && args[i].startsWith("[")) {
                continue;
            }
            if (!ms.args[i].equalsIgnoreCase(args[i])) {
                return false;
            }
        }
        return true;
    }

    protected String[] parseArguments(MethodNode mn) {
        ArrayList<String> args = new ArrayList<String>();
        String signiture = mn.desc;
        signiture = signiture.substring(signiture.indexOf("(") + 1, signiture.indexOf(")"));
        while (signiture != null && !signiture.equals("") && signiture.length() > 0) {
            if (signiture.charAt(0) == 'B' || signiture.charAt(0) == 'C' || signiture.charAt(0) == 'D' || signiture.charAt(0) == 'F' || signiture.charAt(0) == 'I' || signiture.charAt(0) == 'J' || signiture.charAt(0) == 'S' || signiture.charAt(0) == 'Z') {
                args.add(signiture.charAt(0) + "");
                signiture = signiture.substring(1, signiture.length());
            } else if (signiture.startsWith("[")) {
                String arg = "[";
                signiture = signiture.substring(1, signiture.length());
                while (signiture.startsWith("[")) {
                    arg += "[";
                    signiture = signiture.substring(1, signiture.length());
                }
                if (signiture.charAt(0) == 'B' || signiture.charAt(0) == 'C' || signiture.charAt(0) == 'D' || signiture.charAt(0) == 'F' || signiture.charAt(0) == 'I' || signiture.charAt(0) == 'J' || signiture.charAt(0) == 'S' || signiture.charAt(0) == 'Z') {
                    args.add(arg + signiture.charAt(0) + "");
                    signiture = signiture.substring(1, signiture.length());
                } else {
                    args.add(arg + signiture.substring(0, signiture.indexOf(";") + 1));
                    signiture = signiture.substring(signiture.indexOf(";") + 1, signiture.length());
                }
            } else {
                args.add(signiture.substring(0, signiture.indexOf(";") + 1));
                signiture = signiture.substring(signiture.indexOf(";") + 1, signiture.length());
            }
        }
        return args.toArray(new String[]{});
    }

    public boolean isReferenceMatch(FieldSigniture fs) {
        for (MethodData mn : referencedFrom) {
            if (fs.retType.equals("?") || fs.retType.equals(mn.bytecodeMethod.desc.substring(mn.bytecodeMethod.desc.indexOf(")") + 1, mn.bytecodeMethod.desc.length()))) {
                String[] args = parseArguments(mn.bytecodeMethod);
                if (fs.args.length == args.length && doArgsMatch(fs, args)) {
                    if (fs.bytecodePattern == null || fs.bytecodePattern.length() < 1)
                        return true;
                    InstructionFinder finder = new InstructionFinder(mn.bytecodeMethod);
                    for (AbstractInsnNode[] match : finder.findPattern(fs.bytecodePattern)) {
                        for (AbstractInsnNode ins : match) {
                            if (ins instanceof FieldInsnNode) {
                                FieldInsnNode fieldNode = (FieldInsnNode) ins;
                                if (fieldNode.name.equals(FIELD_NAME) && fieldNode.owner.equals(CLASS_NAME))
                                    return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isReferenceMatch(FieldSigniture fs, int opcode) {
        for (MethodData mn : referencedFrom) {
            if (fs.retType.equals("?") || fs.retType.equals(mn.bytecodeMethod.desc.substring(mn.bytecodeMethod.desc.indexOf(")") + 1, mn.bytecodeMethod.desc.length()))) {
                String[] args = parseArguments(mn.bytecodeMethod);
                if (fs.args.length == args.length && doArgsMatch(fs, args)) {
                    if (fs.bytecodePattern == null || fs.bytecodePattern.length() < 1)
                        return true;
                    InstructionFinder finder = new InstructionFinder(mn.bytecodeMethod);
                    for (AbstractInsnNode[] match : finder.findPattern(fs.bytecodePattern)) {
                        for (AbstractInsnNode ins : match) {
                            if (ins instanceof FieldInsnNode) {
                                FieldInsnNode fieldNode = (FieldInsnNode) ins;
                                if (fieldNode.name.equals(FIELD_NAME) && fieldNode.owner.equals(CLASS_NAME) &&
                                		fieldNode.getOpcode() == opcode)
                                    return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public List<AbstractInsnNode> getAbstractInsnList(List<MethodData> methods, Mask... masks) {
    	ArrayList<MethodNode> nodes = new ArrayList<MethodNode>();
    	for(MethodData md : methods)
    		nodes.add(md.bytecodeMethod);
        List<AbstractInsnNode> pattern = Assembly.find(nodes.toArray(new MethodNode[]{}), masks);
        if (pattern != null) return pattern;
        return null;
    }

    public List<AbstractInsnNode[]> getAllMatches(MethodNode mn, String regex) {
        final RegexInstructionMatcher matcher = new RegexInstructionMatcher(mn.instructions);
        return matcher.search(regex);
    }

    public AbstractInsnNode[] getMatchIndex(MethodNode mn, String regex, int index) {
        final RegexInstructionMatcher matcher = new RegexInstructionMatcher(mn.instructions);
        return matcher.search(regex).get(index);
    }

    public boolean isMatch(FieldSigniture[] methods, Mask... masks) {
        if (isMatch(methods)) {
            return getAbstractInsnList(referencedFrom, masks) != null;
        }
        return isMatch(methods);
    }

    
    
    public boolean isMatch(FieldSigniture[] methods) {
        for (FieldSigniture ms : methods) {
            if (!isReferenceMatch(ms)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isMatch(FieldSigniture[] methods, int opcode) {
        for (FieldSigniture ms : methods) {
            if (!isReferenceMatch(ms, opcode)) {
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean equals(Object o){
    	if(o instanceof FieldData){
    		FieldData fd = (FieldData)o;
    		return this.CLASS_NAME.equals(fd.CLASS_NAME) && this.FIELD_NAME.equals(fd.FIELD_NAME);
    	}
    	return false;
    }
    @Override
    public String toString() {
        return Modifier.toString(bytecodeField.access) + " " + bytecodeField.desc + " " + CLASS_NAME + "." + FIELD_NAME;
    }
}

