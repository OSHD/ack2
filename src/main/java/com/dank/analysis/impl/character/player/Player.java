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
import java.util.HashMap;
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
						boolean isTeam=false;
						for(MethodData md : fd.referencedFrom){
							if(new Wildcard("([L*;IIIIIIII?)V").matches(md.METHOD_DESC)){
								isTeam=true;
								break;
							}
						}
						if(isTeam)
							Hook.PLAYER.put(new RSField(fn, "team"));
						else{
							for(MethodData md : fd.referencedFrom){
								if(new Wildcard("(L"+Hook.PLAYER.getInternalName()+";III?)V").matches(md.METHOD_DESC)){
									List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md.bytecodeMethod,
											Mask.INVOKEVIRTUAL.describe("(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
											Mask.GETSTATIC.describe("Ljava/lang/String;"),
											Mask.INVOKEVIRTUAL.describe("(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
											Mask.GETSTATIC.describe("Ljava/lang/String;"),
											Mask.INVOKEVIRTUAL.describe("(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
											Mask.GETFIELD.describe("I").distance(3)
											);
									if (patterns != null) {
										HashMap<String, Integer> counts = new HashMap<String, Integer>();
										HashMap<Integer, String> counts2 = new HashMap<Integer, String>();
										for(ArrayList<AbstractInsnNode> pattern : patterns){
											FieldInsnNode fin = (FieldInsnNode)pattern.get(5);
											if(!fin.desc.equals("I")){
												fin = (FieldInsnNode)pattern.get(1);
											}
											if(!counts.containsKey(fin.name)){
												counts.put(fin.name, 1);
												counts2.put(1, fin.name);
											}
											else{
												counts2.remove(counts.get(fin.name));
												counts.put(fin.name, counts.get(fin.name)+1);
												counts2.put(counts.get(fin.name), fin.name);
											}
										}
										int min=999;
										int max=-1;
										for(String key : counts.keySet()){
											if(counts.get(key)<min){
												min=counts.get(key);
											}
											if(counts.get(key)>max){
												max=counts.get(key);
											}
										}
										if(fd.FIELD_NAME.equals(counts2.get(min))){//else combatLevel
											Hook.PLAYER.put(new RSField(fn, "totalLevel"));
										}
										else{
											
										}
									}
								}
								if(new Wildcard("(L"+Hook.CHARACTER.getInternalName()+";IIIII?)V").matches(md.METHOD_DESC)){
									List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md.bytecodeMethod,
											Mask.GETSTATIC.describe("[L"+Hook.SPRITE.getInternalName()+";").or(Mask.GETFIELD.describe("I")),
											Mask.GETFIELD.describe("I").distance(4).or(Mask.GETSTATIC.describe("[L"+Hook.SPRITE.getInternalName()+";").distance(4)),
											Mask.AALOAD.distance(4)
											);
									if (patterns != null) {
										HashMap<String, Integer> counts = new HashMap<String, Integer>();
										HashMap<Integer, String> counts2 = new HashMap<Integer, String>();
										for(ArrayList<AbstractInsnNode> pattern : patterns){
											FieldInsnNode fin1 = (FieldInsnNode)pattern.get(0);
											FieldInsnNode fin2 = (FieldInsnNode)pattern.get(1);
											if(!fin1.desc.equals("I")){
												fin1 = (FieldInsnNode)pattern.get(1);
												fin2 = (FieldInsnNode)pattern.get(0);
											}
											if(!counts.containsKey(fin2.name)){
												counts.put(fin2.name, 1);
												counts2.put(1, fin1.name);
											}
											else{
												counts2.remove(counts.get(fin2.name));
												counts.put(fin2.name, counts.get(fin2.name)+1);
												counts2.put(counts.get(fin2.name), fin1.name);
											}
										}
										int min=999;
										int max=-1;
										for(String key : counts.keySet()){
											if(counts.get(key)<min){
												min=counts.get(key);
											}
											if(counts.get(key)>max){
												max=counts.get(key);
											}
										}
										if(fd.FIELD_NAME.equals(counts2.get(min))){
											Hook.PLAYER.put(new RSField(fn, "prayerIcon"));
										}
										else{
											Hook.PLAYER.put(new RSField(fn, "skullIcon"));
										}
									}
									break;
								}
							}
						}
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
						for(MethodData md2 : fd.referencedFrom){
							if(new Wildcard("(L*;?)V").matches(md2.METHOD_DESC)){
								HashMap<String, Integer> counts = new HashMap<String, Integer>();
								HashMap<Integer, String> counts2 = new HashMap<Integer, String>();
								for(FieldData fd2 : md2.fieldReferences){
									if(fd2.CLASS_NAME.equals(cn.name) && fd2.bytecodeField.desc.equals("I")){
										if(!counts.containsKey(fd2.FIELD_NAME)){
											counts.put(fd2.FIELD_NAME, 1);
											counts2.put(1, fd2.FIELD_NAME);
										}
										else{
											counts2.remove(counts.get(fd2.FIELD_NAME));
											counts.put(fd2.FIELD_NAME, counts.get(fd2.FIELD_NAME)+1);
											counts2.put(counts.get(fd2.FIELD_NAME), fd2.FIELD_NAME);
										}
									}
								}
								int max=-1;
								for(String key : counts.keySet()){
									if(counts.get(key)>max){
										max=counts.get(key);
									}
								}
								FieldData drawHeight = DynaFlowAnalyzer.getField(cn.name, counts2.get(max));
								if(drawHeight!=null)
									Hook.PLAYER.put(new RSField(drawHeight.bytecodeField, "height"));
								break;
							}
						}
					}
				}
			}
		}
	}


}
