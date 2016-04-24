package com.dank.analysis.impl.character.player;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.character.AnimInterp2;
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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;


public class Player extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(Hook.PLAYER_CONFIG) == 1 ? new ClassSpec(Hook.PLAYER, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
    	RSMember member = Hook.ENTITY.get("getAnimatedModel");
    	RSMethod rotatedModel=null;
    	if(member!=null)
    		rotatedModel = (RSMethod)member;
        for(MethodNode mn : cn.methods) {
        	MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
            if(new Wildcard("(L"+Hook.BUFFER.getInternalName()+";?)V").matches(mn.desc)) {
            	Hook.PLAYER.put(new RSMethod(mn, "updatePlayer"));
                AnimInterp2.run(cn.name,mn);//Character idle/walk/runAnimation hooks
            }
            if(new Wildcard("(?)L"+Hook.MODEL.getInternalName()+";").matches(mn.desc) && rotatedModel!=null && mn.name.equals(rotatedModel.name)) {
            	rotatedModel=new RSMethod(mn, "getAnimatedModel");
            	Hook.PLAYER.put(rotatedModel);
            }
            if(new Wildcard("(IIB?)V").matches(mn.desc)) {
            	boolean isSetPos=true;
            	for(FieldData fd : md.fieldReferences){
            		if(!(fd.bytecodeField.desc.equals("I") || fd.bytecodeField.desc.equals("[I") || fd.bytecodeField.desc.equals("[B"))){
            			isSetPos=false;
            		}
            	}
            	if(isSetPos){
            		Hook.PLAYER.put(new RSMethod(mn, "setPosition"));
            		List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md.bytecodeMethod,
    						Mask.GETFIELD.describe("[I"),
    						Mask.ICONST_0,
    						Mask.ILOAD,
    						Mask.IASTORE
    						);
    				if (patterns != null) {
    					for(ArrayList<AbstractInsnNode> pattern : patterns){
    						FieldInsnNode fin = (FieldInsnNode) pattern.get(0);
	    					VarInsnNode iload = (VarInsnNode) pattern.get(2);
	    					FieldData fd = DynaFlowAnalyzer.getField(DynaFlowAnalyzer.getClass(fin.owner).bytecodeClass.superName, fin.name);//Its overriden in the bytecode
    						if(fd!=null){
	    						if(iload.var==1){
	    							Hook.CHARACTER.put(new RSField(fd.bytecodeField, "queueX"));
	    						}
	    						else if(iload.var==2){
	    							Hook.CHARACTER.put(new RSField(fd.bytecodeField, "queueY"));
	    						}
    						}
    					}
    				}
    				List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
    						Mask.GETFIELD.describe("[B")
    						);
    				if (pattern != null) {
						FieldInsnNode fin = (FieldInsnNode) pattern.get(0);
						FieldData fd = DynaFlowAnalyzer.getField(DynaFlowAnalyzer.getClass(fin.owner).bytecodeClass.superName, fin.name);//Its overriden in the bytecode
						Hook.CHARACTER.put(new RSField(fd.bytecodeField, "queueRun"));
    				}
            	}
            	else
            		Hook.PLAYER.put(new RSMethod(mn, "updateMovement"));
            }
            if(new Wildcard("(?)I").matches(mn.desc)) {
        		Hook.PLAYER.put(new RSMethod(mn, "getConfigId"));
            }
        }
        for (final FieldNode fn : cn.fields) {
            if (!fn.isStatic()) {
            	FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
                if (fn.desc.equals(Hook.PLAYER_CONFIG.getInternalDesc())) {
                    Hook.PLAYER.put(new RSField(fn, "config"));
                } else if (fn.desc.equals(Hook.MODEL.getInternalDesc())) {
                    Hook.PLAYER.put(new RSField(fn, "model"));
                } else if (fn.desc.equals("Ljava/lang/String;")) {
                    Hook.PLAYER.put(new RSField(fn, "name"));
                } else if (fn.desc.equals("I")) {
                	boolean isCmbLvl=false;
                	for(MethodData md : fd.referencedFrom){
                		if(new Wildcard("(L"+Hook.NPC_DEFINITION.getInternalName()+";III?)V").matches(md.METHOD_DESC)){
                			isCmbLvl=true;
                			break;
                		}
                	}
                	if(isCmbLvl)
                		Hook.PLAYER.put(new RSField(fn, "combatLevel"));
                	else{
                		System.out.println(":: "+fd.CLASS_NAME+"."+fd.FIELD_NAME);
                		boolean isTeam=false;
                    	for(MethodData md : fd.referencedFrom){
                    		if(new Wildcard("([L*;IIIIIIII?)V").matches(md.METHOD_DESC)){
                    			isTeam=true;
                    			break;
                    		}
                    	}
                    	if(isTeam)
                    		Hook.PLAYER.put(new RSField(fn, "team"));
                	}
                } else if(fn.desc.equals("Z")){
                	boolean isVisible=false;
                	for(MethodData md : fd.referencedFrom){
                		if(md.CLASS_NAME.equals(rotatedModel.owner) && md.METHOD_NAME.equals(rotatedModel.name) && md.METHOD_DESC.equals(rotatedModel.desc)){
                			isVisible=true;
                			break;
                		}
                	}
                	if(isVisible){
                		Hook.PLAYER.put(new RSField(fn, "visible"));
                	}
                }
            }
        }
    }


}
