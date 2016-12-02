package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.asm.Assembly;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

import jdk.nashorn.internal.codegen.types.Type;
import org.objectweb.asm.tree.*;

import java.awt.*;

import static org.objectweb.asm.Opcodes.*;

public class Sprite extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        if(cn.fieldCount("I") != 6) return null;
        if(cn.fieldCount("[I") != 1) return null;
        boolean type_a = false;
        boolean type_b = false;

        for(MethodNode mn : cn.methods) {
            if(!mn.name.equals("<init>")) continue;
            final String desc = mn.desc;
            if(desc.equals("(II)V")) {
                type_a = true;
            } else if(desc.equals("([BLjava/awt/Component;)V")) {
                type_b = true;
            }
            if(type_a && type_b) {
                return new ClassSpec(Hook.SPRITE,cn);
            }
        }
        return null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for(MethodNode mn : cn.methods) {
            if(!mn.name.equals("<init>")) 
            	continue;
            if(!mn.desc.equals("([BLjava/awt/Component;)V")) 
            	continue;

            RSField width = null;
            RSField height = null;

            for(FieldNode fn : cn.fields) {
            	if(fn.isStatic())
            		continue;
            	FieldData fd=DynaFlowAnalyzer.getField(cn.name, fn.name);
                if(fn.desc.equals("[I")) {
                    Hook.SPRITE.put(new RSField(fn, "pixels"));
                    for(MethodData md : fd.referencedFrom){
                    	if(new Wildcard("()V").matches(md.METHOD_DESC)){
                    		if(md.fieldReferences.size()==3){
	                    		int intCount=0, intArr=0;
	                    		for(FieldData fd2 : md.fieldReferences){
	                    			if(fd2.bytecodeField.desc.equals("I"))
	                    				intCount++;
	                    			if(fd2.bytecodeField.desc.equals("[I"))
	                    				intArr++;
	                    		}
	                    		if(intCount==2 && intArr==1)
	                    			Hook.SPRITE.put(new RSMethod(md.bytecodeMethod, "createGraphics"));
                    		}
                    	}
                    	if(new Wildcard("(III)V").matches(md.METHOD_DESC)){
                    		boolean isAdjustRGB=true;
                    		for(FieldData fd2 : md.fieldReferences){
                    			if(!fd2.FIELD_NAME.equals(fd.FIELD_NAME)){
                    				isAdjustRGB=false;
                    				break;
                    			}
                    		}
                    		if(isAdjustRGB)
                    			Hook.SPRITE.put(new RSMethod(md.bytecodeMethod, "adjustRGB"));
                    		else{
                    			Hook.SPRITE.put(new RSMethod(md.bytecodeMethod, "drawImage2"));
                    			for(MethodData md2 : md.methodReferences){
                    				if(new Wildcard("([I[IIIIIIIII)V").matches(md2.METHOD_DESC)){
                            			Hook.SPRITE.put(new RSMethod(md2.bytecodeMethod, "copyPixelsAlpha"));//static TODO
                            			break;
                    				}
                    			}
                    		}
                    	}
                    	if(new Wildcard("(II)V").matches(md.METHOD_DESC)){
                    		for(MethodData md2 : md.methodReferences){
                    			if(new Wildcard("([I[IIIIIII)V").matches(md2.METHOD_DESC)){
                        			Hook.SPRITE.put(new RSMethod(md.bytecodeMethod, "drawInverse"));
                        			Hook.SPRITE.put(new RSMethod(md2.bytecodeMethod, "copyPixels"));//static TODO
                    			}
                    			if(new Wildcard("([I[IIIIIIII)V").matches(md2.METHOD_DESC)){
                        			Hook.SPRITE.put(new RSMethod(md.bytecodeMethod, "drawImage"));
                        			Hook.SPRITE.put(new RSMethod(md2.bytecodeMethod, "shapeImageToPixels"));//static TODO
                    			}
                    		}
                    	}
                    	if(new Wildcard("(IIIIIID?)V").matches(md.METHOD_DESC)){
                			Hook.SPRITE.put(new RSMethod(md.bytecodeMethod, "rotate"));
                    	}
                    	if(new Wildcard("(IIIIIIII[I[I)V").matches(md.METHOD_DESC)){
                			Hook.SPRITE.put(new RSMethod(md.bytecodeMethod, "rotate2"));
                    	}
                    }
                    break;
                }
            }

            //Find the width/height
            for(AbstractInsnNode ain : mn.instructions.toArray()) {
                if(ain.opcode() == INVOKEVIRTUAL) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    if(min.owner.equals("java/awt/Image")) {
                        if(min.name.equals("getWidth")) {
                            AbstractInsnNode next = min.next();
                            if(next.opcode() != PUTFIELD) continue;
                            FieldInsnNode fin = (FieldInsnNode) next;
                            Hook.SPRITE.put(width = new RSField(fin,"width"));
                        } else if(min.name.equals("getHeight")) {
                            AbstractInsnNode next = min.next();
                            if(next.opcode() != PUTFIELD) continue;
                            FieldInsnNode fin = (FieldInsnNode) next;
                            Hook.SPRITE.put(height = new RSField(fin,"height"));
                        }
                    }
                }
            }

            if(width == null && height == null) return;

            for(AbstractInsnNode ain : mn.instructions.toArray()) {
                if(ain.opcode() == GETFIELD) {

                    AbstractInsnNode next = ain.next();
                    if(next.opcode() != PUTFIELD) continue;
                    FieldInsnNode fin = (FieldInsnNode) next;

                    if(width != null && width.matches((FieldInsnNode)ain)) {
                        Hook.SPRITE.put(new RSField(fin,"maxX"));
                    } else if(height != null && height.matches((FieldInsnNode) ain)) {
                        Hook.SPRITE.put(new RSField(fin,"maxY"));
                    }

                } else if(ain.opcode() == ICONST_0) {

                    AbstractInsnNode next = ain.next();
                    if(next.opcode() != PUTFIELD) continue;
                    FieldInsnNode fin = (FieldInsnNode) next;

                    if(Hook.SPRITE.get("paddingX") == null) {
                        Hook.SPRITE.put(new RSField(fin,"paddingX"));
                    } else if(Hook.SPRITE.get("paddingY") == null) {
                        Hook.SPRITE.put(new RSField(fin,"paddingY"));
                        FieldData fd = DynaFlowAnalyzer.getField(fin.owner, fin.name);
                        for(MethodData md : fd.referencedFrom){
                        	if(new Wildcard("()V").matches(md.METHOD_DESC)){
                        		java.util.List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
            							Mask.ICONST_0,
            							Mask.PUTFIELD
            							);
            					if (pattern != null) {
            						FieldInsnNode fin2 = (FieldInsnNode) pattern.get(1);
            						if(fin2.owner.equals(fin.owner) && fin2.name.equals(fin.name)){
		                    			Hook.SPRITE.put(new RSMethod(md.bytecodeMethod, "trim"));
		                    			break;
            						}
            					}
                        	}
                        }
                    }

                }

            }

        }
    }
}
