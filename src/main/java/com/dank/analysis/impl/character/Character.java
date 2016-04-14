package com.dank.analysis.impl.character;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.character.visitor.*;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.NumberQuery;
import org.objectweb.asm.commons.cfg.tree.NodeTree;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


public class Character extends Analyser {
	@Override
	public ClassSpec specify(ClassNode cn) {
		if(cn.access!=1057)
			return null;
		if(!cn.superName.equals(Hook.ENTITY.getInternalName()))
			return null;
		boolean str=false, in=false, bool=false, inar=false;
		for(FieldNode fn : cn.fields){
			if(fn.isStatic())
				continue;
			if(fn.desc.equals("Ljava/lang/String;"))
				str=true;
			if(fn.desc.equals("I"))
				in=true;
			if(fn.desc.equals("Z"))
				bool=true;
			if(fn.desc.equals("[I"))
				inar=true;
		}
		if(str && in && bool && inar)
			return new ClassSpec(Hook.CHARACTER, cn);
		return null;
	}
	@Override
	public void evaluate(ClassNode cn) {
		for(MethodNode mn : cn.methods){
			if(mn.isStatic())
				continue;
			MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
			if(md.referencedFrom.size()>0 && new Wildcard("(?)V").matches(mn.desc)){
				Hook.CHARACTER.put(new RSMethod(mn, "resetPathQueue"));
			}
			if(new Wildcard("(III?)V").matches(mn.desc)){
				Hook.CHARACTER.put(new RSMethod(mn, "updateHitData"));
			}
			if(new Wildcard("(?)Z").matches(mn.desc)){
				Hook.CHARACTER.put(new RSMethod(mn, "isVisible"));
			}
		}
		RSMethod resetPathQueue = (RSMethod)Hook.CHARACTER.get("resetPathQueue");
		MethodData resetPathData = DynaFlowAnalyzer.getMethod(resetPathQueue.owner, resetPathQueue.name, resetPathQueue.desc);
		for(FieldData fd : resetPathData.fieldReferences){
			boolean isIndex=false;
			for(MethodData md : fd.referencedFrom){
				if(new Wildcard("(L"+Hook.CHARACTER.getInternalName()+";I?)V").matches(md.METHOD_DESC))
					isIndex=true;
			}
			if(isIndex)
				Hook.CHARACTER.put(new RSField(fd.bytecodeField, "currentQueueIndex"));
			else
				Hook.CHARACTER.put(new RSField(fd.bytecodeField, "queueSize"));
		}
		for(FieldNode fn : cn.fields){
			if(fn.isStatic())
				continue;
			FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
			if(fn.desc.equals("Ljava/lang/String;")){
				Hook.CHARACTER.put(new RSField(fd.bytecodeField, "overheadText"));
			}
			if(fn.desc.equals("I")){
				for(MethodData md : fd.referencedFrom){
					if(md.referencedFrom.size()==0)
						continue;
					if(new Wildcard("(L"+Hook.CHARACTER.getInternalName()+";I?)V").matches(md.METHOD_DESC)){
						if(md.fieldReferences.size()==2){
							List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
									Mask.GETFIELD.describe("I"),
									Mask.IMUL.distance(2),
									Mask.GETFIELD.describe("I").distance(3),
									Mask.IMUL.distance(2),
									Mask.ILOAD,
									Mask.INVOKESTATIC.distance(2)
									);
							if (pattern != null) {
								FieldInsnNode fin1 = (FieldInsnNode) pattern.get(0);
								FieldInsnNode fin2 = (FieldInsnNode) pattern.get(2);
								if(fin1.name.equals(fn.name)){
									Hook.CHARACTER.put(new RSField(fd.bytecodeField, "strictX"));
									break;
								}
								else if(fin2.name.equals(fn.name)){
									Hook.CHARACTER.put(new RSField(fd.bytecodeField, "strictY"));
									break;
								}
							}
						}
						else{
							List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md.bytecodeMethod,
									Mask.GETFIELD.describe("I"),
									Mask.IMUL.distance(2),
									Mask.INVOKESTATIC.distance(2),
									Mask.ASTORE
									);
							if(patterns!=null){
								for(ArrayList<AbstractInsnNode> pattern : patterns){
									FieldInsnNode fin = (FieldInsnNode)pattern.get(0);
									if(fin.name.equals(fn.name)){
										int count=0;
										for(FieldData fd2 : md.fieldReferences){
											if(fd2.CLASS_NAME.equals(fd.CLASS_NAME) && fd2.FIELD_NAME.equals(fd.FIELD_NAME))
												count++;
										}
										if(count==3)
											Hook.CHARACTER.put(new RSField(fd.bytecodeField, "interAnimId"));
										if(count==9)
											Hook.CHARACTER.put(new RSField(fd.bytecodeField, "animation"));
										break;
									}
								}
							}
							patterns = Assembly.findAll(md.bytecodeMethod,
									Mask.GETFIELD,
									Mask.GETFIELD.distance(5),
									Mask.ARRAYLENGTH
									);
							if (patterns != null) {
								for(ArrayList<AbstractInsnNode> pattern : patterns){
									FieldInsnNode fin1 = (FieldInsnNode) pattern.get(0);
									if(!fin1.desc.equals("I"))
										fin1=(FieldInsnNode) pattern.get(1);
									AbstractInsnNode node = pattern.get(2);
									if(node.getOpcode()!=Opcodes.ALOAD)
										node=node.getPrevious();
									if(node.getOpcode()!=Opcodes.ALOAD)
										node=node.getPrevious();
									if(node.getOpcode()!=Opcodes.ALOAD)
										node=node.getPrevious();
									if(node.getOpcode()!=Opcodes.ALOAD)
										node=node.getPrevious();
									if(node.getOpcode()!=Opcodes.ALOAD)
										node=node.getPrevious();
									if(node.getOpcode()!=Opcodes.ALOAD)
										node=node.getPrevious();
									VarInsnNode var = (VarInsnNode)node;
									if(var.var==3 && fin1.name.equals(fn.name)){
										Hook.CHARACTER.put(new RSField(fd.bytecodeField, "interAnimFrameId"));
										break;
									}
								}
							}

							patterns = Assembly.findAll(md.bytecodeMethod,
									Mask.GETFIELD,
									Mask.IMUL.distance(4),
									Mask.GETFIELD.distance(4),
									Mask.IMUL.distance(4),
									Mask.IADD,
									Mask.PUTFIELD
									);
							if (patterns != null) {
								int verify=0;
								for(ArrayList<AbstractInsnNode> pattern : patterns){
									FieldInsnNode fin1 = (FieldInsnNode) pattern.get(0);
									if(!fin1.desc.equals("I"))
										fin1 = (FieldInsnNode) pattern.get(2);
									if(fin1.owner.equals(fd.CLASS_NAME) && fin1.name.equals(fd.FIELD_NAME))
										verify++;
								}
								if(verify==38){
									Hook.CHARACTER.put(new RSField(fd.bytecodeField, "npcBoundDim"));
									break;
								}
							}
						}
					}
					if(new Wildcard("(L"+Hook.CHARACTER.getInternalName()+";IIIII?)V").matches(md.METHOD_DESC)){
						List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
								Mask.ALOAD.operand(0),
								Mask.BIPUSH.distance(3).or(Mask.GETFIELD.describe("I").distance(3)),
								Mask.GETFIELD.describe("I").distance(3).or(Mask.BIPUSH.distance(3)),
								Mask.IADD.distance(3),
								Mask.INVOKESTATIC.distance(2)
								);
						if (pattern != null) {
							AbstractInsnNode insn = pattern.get(1);
							if(insn.getOpcode()!=Opcodes.GETFIELD)
								insn = pattern.get(2);
							FieldInsnNode fin = (FieldInsnNode)insn;
							if(fin.name.equals(fn.name)){
								Hook.CHARACTER.put(new RSField(fd.bytecodeField, "modelHeight"));
								break;
							}
						}
						pattern = Assembly.find(md.bytecodeMethod,
								Mask.GETFIELD.describe("I").own(Hook.CHARACTER.getInternalName()),
								Mask.IMUL.distance(4),
								Mask.IMUL.distance(2),
								Mask.GETFIELD.describe("I").own(Hook.CHARACTER.getInternalName()).distance(3),
								Mask.IMUL.distance(2),
								Mask.IDIV
								);
						if (pattern != null) {
							FieldInsnNode fin1 = (FieldInsnNode) pattern.get(0);
							FieldInsnNode fin2 = (FieldInsnNode) pattern.get(3);
							if(fin1.name.equals(fn.name)){
								Hook.CHARACTER.put(new RSField(fd.bytecodeField, "hitpoints"));
								break;
							}
							else if(fin2.name.equals(fn.name)){
								Hook.CHARACTER.put(new RSField(fd.bytecodeField, "maxHitpoints"));
								break;
							}
						}
						List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md.bytecodeMethod,
								Mask.GETFIELD.or(Mask.GETSTATIC),
								Mask.IMUL.distance(3),
								Mask.GETSTATIC.or(Mask.GETFIELD.distance(3)).distance(3),
								Mask.IMUL.distance(3),
								Mask.IF_ICMPLE.or(Mask.IF_ICMPGT)
								);
						if (patterns != null) {
							for(ArrayList<AbstractInsnNode> patt : patterns){
								AbstractInsnNode insn = patt.get(0);
								if(insn.getOpcode()!=Opcodes.GETFIELD)
									insn = patt.get(2);
								FieldInsnNode fin = (FieldInsnNode)insn;
								if(fin.name.equals(fn.name) && fin.owner.equals(fd.CLASS_NAME)){
									Hook.CHARACTER.put(new RSField(fd.bytecodeField, "healthBarCycle"));
									break;
								}
							}
						}
					}
					if(new Wildcard("(L"+Hook.CHARACTER.getInternalName()+";?)V").matches(md.METHOD_DESC)){

						outer:for (final BasicBlock block : md.bytecodeMethod.graph()) {
							final NodeTree tree = block.tree();//Unsure if this method works
							for(AbstractInsnNode insn : block.instructions){
								if(insn.getOpcode()==Opcodes.GETFIELD || insn.getOpcode()==Opcodes.PUTFIELD){
									FieldInsnNode fin = (FieldInsnNode)insn;
									if(fin.owner.equals(cn.name) && fin.name.equals(fn.name)){
										int index=block.getIndex();
										if(index==99)
											Hook.CHARACTER.put(new RSField(fd.bytecodeField, "npcTurnAround"));
										if(index==125)
											Hook.CHARACTER.put(new RSField(fd.bytecodeField, "npcTurnLeft"));
										if(index==128)
											Hook.CHARACTER.put(new RSField(fd.bytecodeField, "npcTurnRight"));
										break outer;
									}
								}
							}
						}
						
						List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
								Mask.ILOAD.operand(2),
								Mask.SIPUSH.cst(2048),
								Mask.GETFIELD.describe("I").distance(3),
								Mask.IMUL.distance(3),
								Mask.ISUB,
								Mask.IF_ICMPLE
								);
						if (pattern != null) {
							FieldInsnNode fin1 = (FieldInsnNode) pattern.get(2);
							if(fin1.name.equals(fn.name)){
								Hook.CHARACTER.put(new RSField(fd.bytecodeField, "npcDegToTurn"));
								break;
							}
						}
						pattern = Assembly.find(md.bytecodeMethod,
								Mask.INVOKESTATIC,
								Mask.LDC,
								Mask.DMUL,
								Mask.D2I,
								Mask.SIPUSH,
								Mask.IAND,
								Mask.PUTFIELD.distance(3)
								);
						if (pattern != null) {
							FieldInsnNode fin1 = (FieldInsnNode) pattern.get(6);
							if(fin1.name.equals(fn.name)){
								Hook.CHARACTER.put(new RSField(fd.bytecodeField, "orientation"));
								break;
							}
						}
						pattern = Assembly.find(md.bytecodeMethod,
								Mask.INVOKESTATIC,
								Mask.GETFIELD.describe("[I"),
								Mask.GETFIELD.describe("I").distance(3),
								Mask.IMUL.distance(2),
								Mask.IALOAD
								);
						if (pattern != null) {
							FieldInsnNode fin1 = (FieldInsnNode) pattern.get(2);
							if(fin1.name.equals(fn.name)){
								Hook.CHARACTER.put(new RSField(fd.bytecodeField, "animFrameId"));
								break;
							}
						}

						int count=0;
						for(FieldData fd2 : md.fieldReferences){
							if(fd2.CLASS_NAME.equals(fd.CLASS_NAME) && fd2.FIELD_NAME.equals(fd.FIELD_NAME))
								count++;
						}
						if(count==3){
							boolean found=true;
							int verify=0;
							for(MethodData md2 : fd.referencedFrom){
								if(md2.referencedFrom.size()==0)
									continue;
								if(new Wildcard("(L"+Hook.CHARACTER.getInternalName()+";?)V").matches(md2.METHOD_DESC))
									verify++;
								if(new Wildcard("(L"+Hook.CHARACTER.getInternalName()+";I?)V").matches(md2.METHOD_DESC))
									found=false;
							}
							if(found && verify==1){
								Hook.CHARACTER.put(new RSField(fd.bytecodeField, "walkAnimation"));
								break;
							}
						}
						if(count==2){
							List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md.bytecodeMethod,
									Mask.ALOAD,
									Mask.GETFIELD.describe("I").distance(2),
									Mask.IMUL.distance(2),
									Mask.PUTFIELD.describe("I")
									);
							if (patterns != null) {
								count=0;
								for(ArrayList<AbstractInsnNode> p : patterns){
									FieldInsnNode fin1 = (FieldInsnNode) p.get(1);
									if(fin1.name.equals(fn.name)){
										count++;
									}
								}
								if(count==2){
									Hook.CHARACTER.put(new RSField(fd.bytecodeField, "runAnimation"));
								}
								else if(count==7){
									Hook.CHARACTER.put(new RSField(fd.bytecodeField, "getNextAnimation"));
								}
								else if(count==8){
								}
								else if(count==11){//unknown7 aka standTurnAnimIndex
									Hook.CHARACTER.put(new RSField(fd.bytecodeField, "standTurnAnimIndex"));
								}
							}
						}
						if(count==1){
							int verify=0;
							for(MethodData md2 : fd.referencedFrom){
								if(md2.referencedFrom.size()==0)
									continue;
								if(new Wildcard("(L"+Hook.CHARACTER.getInternalName()+";?)V").matches(md2.METHOD_DESC) &&
										!md2.METHOD_NAME.equals(md.METHOD_NAME)){
									List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md2.bytecodeMethod,
											Mask.GETFIELD.or(Mask.PUTFIELD)
											);
									if (patterns != null) {
										for(ArrayList<AbstractInsnNode> p : patterns){
											FieldInsnNode fin=(FieldInsnNode)p.get(0);
											if(fin.name.equals(fn.name) && fin.owner.equals(fn.owner.name))
												verify++;
										}
									}
								}
							}
							if(verify==20){
								Hook.CHARACTER.put(new RSField(fd.bytecodeField, "idleAnimation"));
							}
						}
					}
					if(new Wildcard("(?)V").matches(md.METHOD_DESC) && md.bytecodeMethod.isStatic()){
						for(FieldData fd2 : md.fieldReferences){
							if(fd2.CLASS_NAME.equals(cn.name) && fd2.bytecodeField.desc.equals("I") ){
								Hook.CHARACTER.put(new RSField(fd.bytecodeField, "targetIndex"));
								break;
							}
						}
					}
				}
			}
			if(fn.desc.equals("[I")){
				for(MethodData md : fd.referencedFrom){
					if(new Wildcard("(L"+Hook.CHARACTER.getInternalName()+";IIIII?)V").matches(md.METHOD_DESC)){
						List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md.bytecodeMethod,
								Mask.ALOAD,
								Mask.GETFIELD.describe("[I").own(Hook.CHARACTER.getInternalName()),
								Mask.ILOAD,
								Mask.IALOAD
								);
						if (patterns != null) {
							for(ArrayList<AbstractInsnNode> pattern : patterns){
								FieldInsnNode fin = (FieldInsnNode) pattern.get(1);
								if(fin.name.equals(fn.name)){
									AbstractInsnNode ain = pattern.get(3);
									ain=ain.getNext();
									if(ain!=null){
										if(ain.getOpcode()==Opcodes.AALOAD){
											Hook.CHARACTER.put(new RSField(fd.bytecodeField, "hitsplatDamages"));
											break;
										}
										if(ain.getOpcode()==Opcodes.GETSTATIC){
											Hook.CHARACTER.put(new RSField(fd.bytecodeField, "hitsplatCycles"));
											break;
										}
										if(ain.getOpcode()==Opcodes.INVOKESTATIC){
											Hook.CHARACTER.put(new RSField(fd.bytecodeField, "hitsplatTypes"));
											break;
										}
									}
								}
							}
						}
						break;
					}
				}
			}
		}
	}
}
