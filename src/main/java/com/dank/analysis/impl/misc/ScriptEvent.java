package com.dank.analysis.impl.misc;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.dank.analysis.Analyser;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

public class ScriptEvent extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        //6 int fields since 79. 5 prior to that
        return cn.fieldCount(Object[].class) == 1 && cn.fieldCount(int.class) == 6 ? new ClassSpec(Hook.SCRIPT_EVENT, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
        	FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
            if (!fn.isStatic()) {
                if (fn.desc.equals("[Ljava/lang/Object;")) {
                    Hook.SCRIPT_EVENT.put(new RSField(fn, "args"));
                }else if (fn.desc.equals("Ljava/lang/String;")) {
                    Hook.SCRIPT_EVENT.put(new RSField(fn, "opbase"));
                }else if (fn.desc.equals("Z")) {
                    Hook.SCRIPT_EVENT.put(new RSField(fn, "consumable"));
                }else if (fn.desc.equals("L"+Hook.WIDGET.getInternalName()+";")) {
                	boolean isSrc=false;
                	for(MethodData md : fd.referencedFrom){
                		if(new Wildcard("([L"+Hook.WIDGET.getInternalName()+";?)V").matches(md.METHOD_DESC))
                			isSrc=true;
                	}
                	if(isSrc)
                        Hook.SCRIPT_EVENT.put(new RSField(fn, "src"));
                	else
                        Hook.SCRIPT_EVENT.put(new RSField(fn, "target"));
                }else if (fn.desc.equals("I")) {
                	MethodData processLogic=null;
                	for(MethodData md : fd.referencedFrom){
                		if(new Wildcard("(?)V").matches(md.METHOD_DESC)){
                			processLogic=md;
                			break;
                		}
                	}
                	if(processLogic!=null){
                		List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(processLogic.bytecodeMethod,
                				Mask.ILOAD.operand(9).or(Mask.ILOAD.operand(10)),
                				Mask.PUTFIELD.own(cn.name).describe("I").distance(4)
        						);
        				if (patterns != null) {
        					for(ArrayList<AbstractInsnNode> pattern : patterns){
        						VarInsnNode var = (VarInsnNode) pattern.get(0);
        						FieldInsnNode fin = (FieldInsnNode) pattern.get(1);
        						if(fin.name.equals(fn.name)){
        							if(var.var==9)
        								Hook.SCRIPT_EVENT.put(new RSField(fn, "mouseX"));
        							else if(var.var==10)
        		                        Hook.SCRIPT_EVENT.put(new RSField(fn, "mouseY"));
        						}
        					}
        				}
                	}
                	else{
                		MethodData buildComponentEvents=null;
                    	for(MethodData md : fd.referencedFrom){
                    		if(new Wildcard("([L"+Hook.WIDGET.getInternalName()+";IIIIII?)V").matches(md.METHOD_DESC)){
                    			buildComponentEvents=md;
                    			break;
                    		}
                    	}
                    	if(buildComponentEvents!=null){
                    		List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(buildComponentEvents.bytecodeMethod,
                    				Mask.GETSTATIC.describe("[I"),
                    				Mask.IALOAD.distance(2),
                    				Mask.IMUL,
                    				Mask.PUTFIELD.describe("I").own(cn.name)
            						);
            				if (patterns != null) {
            					boolean[] map=new boolean[]{false, false};
            					for(ArrayList<AbstractInsnNode> pattern : patterns){
            						FieldInsnNode array = (FieldInsnNode)pattern.get(0);
            						FieldInsnNode key = (FieldInsnNode)pattern.get(3);
            						FieldData arrayData = DynaFlowAnalyzer.getField(array.owner, array.name);
            						boolean keyChar=false;
            						for(MethodData md2 : arrayData.referencedFrom){
            							if(new Wildcard("(?)V").matches(md2.METHOD_DESC)){
            								List<ArrayList<AbstractInsnNode>> patterns2 = Assembly.findAll(md2.bytecodeMethod,
            	                    				Mask.GETSTATIC.describe("[I"),
            	                    				Mask.GETSTATIC.describe("I").distance(2),
            	                    				Mask.GETSTATIC.distance(3),
            	                    				Mask.IASTORE
            	            						);
            	            				if (patterns != null) {
            	            					for(ArrayList<AbstractInsnNode> pattern2 : patterns2){
            	            						FieldInsnNode fin1 = (FieldInsnNode) pattern2.get(0);
            	            						if(fin1.name.equals(array.name)){
	            	            						FieldInsnNode fin = (FieldInsnNode) pattern2.get(2);
	            	            						if(fin.desc.equals("C")){
	            	            							keyChar=true;
	            	            							break;
	            	            						}
            	            						}
            	            					}
            	            				}
            	            				break;
            							}
            						}
            						if(keyChar && !map[0]){
        		                        Hook.SCRIPT_EVENT.put(new RSField(key, "keyChar"));
            							map[0]=true;
            						}
            						else if(!map[1]){
        		                        Hook.SCRIPT_EVENT.put(new RSField(key, "keyCode"));
            							map[1]=true;
            						}
            						else if(map[0] && map[1])
            							break;
            					}
            				}
                    	}
                    	else{
                    		
                    	}
                	}
                }
            }
        }
    }
}
