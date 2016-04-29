package com.dank.analysis.impl.misc;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 16:42
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class Buffer extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(int.class) == 1 && cn.fieldCount(byte[].class) == 1 && cn.fieldCount() == 2
                && cn.superName(Hook.NODE.getInternalName()) ? new ClassSpec(Hook.BUFFER, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (!fn.isStatic()) {
                if (fn.desc.equals("I")) {
                    Hook.BUFFER.put(new RSField(fn, "caret"));
                } else {
                    Hook.BUFFER.put(new RSField(fn, "payload"));
                }
            } else {
            	if (fn.desc.equals("[I")) {
            		Hook.BUFFER.put(new RSField(fn, "crcTable"));
            	}
            }
        }
        for (final MethodNode mn : cn.methods) {
        	if (!mn.isStatic()) {
        		MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
        		if (new Wildcard("(?)Z").matches(mn.desc)) {
        			Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "compareCrcs"));
        		}
        		if (new Wildcard("(?)J").matches(mn.desc)) {
        			Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readLong"));
        		}
        		if (new Wildcard("(J)V").matches(mn.desc)) {
        			List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
							Mask.BIPUSH.operand(56)
							);
					if (pattern != null) {
            			Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeLong"));
					}
        		}
        		if (new Wildcard("(Ljava/math/BigInteger;Ljava/math/BigInteger;?)V").matches(mn.desc)) {
        			Hook.BUFFER.put(new RSMethod(mn, "applyRSA"));
        			for(MethodData md2 : md.methodReferences){
        				if(new Wildcard("(I?)V").matches(md2.METHOD_DESC)){
                			Hook.BUFFER.put(new RSMethod(md2.bytecodeMethod, "writeByte"));
        					break;
        				}
        			}
        			for(MethodData md2 : md.methodReferences){
        				if(new Wildcard("([BII?)V").matches(md2.METHOD_DESC)){
        					List<AbstractInsnNode> pattern = Assembly.find(md2.bytecodeMethod,
        							Mask.ILOAD,
        							Mask.BALOAD,
        							Mask.BASTORE
        							);
        					if (pattern != null) {
                    			Hook.BUFFER.put(new RSMethod(md2.bytecodeMethod, "readBytes"));
        					}
        					else{
                    			Hook.BUFFER.put(new RSMethod(md2.bytecodeMethod, "writeBytes"));
        					}
        				}
        			}
        		}
        		if(md.referencedFrom.size()>0 && new Wildcard("([I?)V").matches(mn.desc)) {
        			List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
							Mask.IADD,
							Mask.IXOR,
							Mask.IADD,
							Mask.ISTORE
							);
					if (pattern != null) {
						Hook.BUFFER.put(new RSMethod(mn, "encodeXTEA"));
					}
        		}
        		if(md.referencedFrom.size()>0 && new Wildcard("([III?)V").matches(mn.desc)) {
        			Hook.BUFFER.put(new RSMethod(mn, "decodeXTEA"));
        			for(MethodData md2 : md.methodReferences){
        				if(new Wildcard("(?)I").matches(md2.METHOD_DESC)){
                			Hook.BUFFER.put(new RSMethod(md2.bytecodeMethod, "readInt"));
        					break;
        				}
        			}
        			for(MethodData md2 : md.methodReferences){
        				if(new Wildcard("(I?)V").matches(md2.METHOD_DESC)){
                			Hook.BUFFER.put(new RSMethod(md2.bytecodeMethod, "writeInt"));
                			for(MethodData md3 : md2.referencedFrom){
                				if(new Wildcard("(I?)I").matches(md3.METHOD_DESC)){
                        			Hook.BUFFER.put(new RSMethod(md3.bytecodeMethod, "writeCrc"));
                					break;
                				}
                			}
        					break;
        				}
        			}
        		}
        	}
        }
    }
}
