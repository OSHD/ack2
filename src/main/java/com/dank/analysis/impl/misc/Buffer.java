package com.dank.analysis.impl.misc;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

//All fields and methods identified as of r113
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
				if (new Wildcard("(?)B").matches(mn.desc)) {
					List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
							Mask.SIPUSH.operand(128),
							Mask.ISUB,
							Mask.I2B
							);
					if (pattern != null) {
						Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readByteA"));
					}
					else{
						pattern = Assembly.find(md.bytecodeMethod,
								Mask.ICONST_0,
								Mask.GETFIELD.distance(2)
								);
						if (pattern != null) {
							Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readNegByte"));
						}
						else{
							pattern = Assembly.find(md.bytecodeMethod,
									Mask.SIPUSH.operand(128)
									);
							if (pattern != null) {
								Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readByteS"));
							}
							else{
								Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readByte"));
							}
						}
					}
				}
				if (new Wildcard("(?)I").matches(mn.desc)) {
					if(md.methodReferences.size()==3){
						List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md.bytecodeMethod,
								Mask.LDC
								);
						if (patterns != null) {
							boolean found=false;
							for(ArrayList<AbstractInsnNode> pattern : patterns){
								LdcInsnNode ldc = (LdcInsnNode) pattern.get(0);
								if(ldc.cst.equals(32768)){
									Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readSmart"));
									found=true;
									AbstractInsnNode ain = pattern.get(0);
									ain=ain.getPrevious();
									if(ain.getOpcode()!=Opcodes.INVOKEVIRTUAL){
										ain=pattern.get(0);
										for(int i=0;i<4;++i){
											ain=ain.getNext();
											if(ain.getOpcode()==Opcodes.INVOKEVIRTUAL)
												break;
										}
									}
									Hook.BUFFER.put(new RSMethod((MethodInsnNode)ain, "readUShort"));
									for(MethodData md2 : md.methodReferences){
										if(new Wildcard("(?)I").matches(md2.METHOD_DESC) && !md2.METHOD_NAME.equals(((MethodInsnNode)ain).name)){
											Hook.BUFFER.put(new RSMethod(md2.bytecodeMethod, "readUByte"));
											break;
										}
									}
									break;
								}
								if(ldc.cst.equals(49152)){
									Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readSmallSmart"));
									found=true;
									break;
								}
							}
							if(!found){
								Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readMIUShort"));
							}
						}
					}
					else{
						List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
								Mask.SIPUSH.operand(32767),
								Mask.IF_ICMPLE.or(Mask.IF_ICMPGE.or(Mask.IF_ICMPLT.or(Mask.IF_ICMPGT))).distance(2)
								);
						if (pattern != null) {
							pattern = Assembly.find(md.bytecodeMethod,
									Mask.SIPUSH.operand(128),
									Mask.ISUB.distance(2)
									);
							if (pattern != null) {
								Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readShortA"));
							}
							else{
								pattern = Assembly.find(md.bytecodeMethod,
										Mask.BIPUSH.operand(8)
										);
								if (pattern != null) {
									AbstractInsnNode ain = pattern.get(0);
									for(int i=0;i<7;++i){
										ain=ain.getPrevious();
										if(ain.getOpcode()==Opcodes.ICONST_1 || ain.getOpcode()==Opcodes.ICONST_2){
											break;
										}
									}
									if(ain.getOpcode()==Opcodes.ICONST_1){
										Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readLEShort"));
									}
									else if(ain.getOpcode()==Opcodes.ICONST_2){
										Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readShort"));
									}
								}
							}
						}
						else{
							int count=0;
							for(FieldData fd : md.fieldReferences){
								if(fd.bytecodeField.desc.equals("[B"))
									count++;
							}
							if(count==1){
								pattern = Assembly.find(md.bytecodeMethod,
										Mask.BALOAD,
										Mask.SIPUSH,
										Mask.ISUB,
										Mask.SIPUSH,
										Mask.IAND
										);
								if (pattern != null) {
									Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readUByteA"));
								}
								else{
									pattern = Assembly.find(md.bytecodeMethod,
											Mask.ICONST_0
											);
									if (pattern != null) {
										Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readNegUByte"));
									}
									else{
										pattern = Assembly.find(md.bytecodeMethod,
												Mask.SIPUSH.operand(128)
												);
										if (pattern != null) {
											Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readUByteS"));
										}
									}
								}
							}
							if(count==2){
								pattern = Assembly.find(md.bytecodeMethod,
										Mask.ICONST_2,
										Mask.ISUB.distance(5),
										Mask.BALOAD,
										Mask.SIPUSH,
										Mask.ISUB,
										Mask.SIPUSH,
										Mask.IAND
										);
								if (pattern != null) {
									Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readLEUShortA"));
								}
								else{
									pattern = Assembly.find(md.bytecodeMethod,
											Mask.ICONST_1,
											Mask.ISUB.distance(5),
											Mask.BALOAD,
											Mask.SIPUSH,
											Mask.ISUB,
											Mask.SIPUSH,
											Mask.IAND
											);
									if (pattern != null) {
										Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readUShortA"));
									}
									else{
										pattern = Assembly.find(md.bytecodeMethod,
												Mask.ICONST_2,
												Mask.ISUB.distance(5),
												Mask.BALOAD,
												Mask.SIPUSH,
												Mask.IAND,
												Mask.IADD
												);
										if (pattern != null) {
											Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readLEUShort"));
										}
										else{
											boolean isShiftedShort=false;
											for(MethodData md2 : md.referencedFrom){
												if(new Wildcard("(?)Ljava/lang/String;").matches(md2.METHOD_DESC)){
													isShiftedShort=true;
													break;
												}
											}
											if(isShiftedShort){
												Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readShortShift"));
											}
										}
									}
								}
							}
							if(count==3){
								Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readMediumInt"));
							}
							if(count==4){
								pattern = Assembly.find(md.bytecodeMethod,
										Mask.ICONST_1,
										Mask.BIPUSH.distance(9)
										);
								if (pattern != null) {
									IntInsnNode bipush = (IntInsnNode) pattern.get(1);
									if(bipush.operand==8)
										Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readMEInt1"));
									else if(bipush.operand==16)
										Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readMEInt2"));
									else if(bipush.operand==24)
										Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readLEInt"));
									else
										System.out.println(":: "+md.CLASS_NAME+"."+md.METHOD_NAME+md.METHOD_DESC);
								}
							}
						}
					}
				}
				if (new Wildcard("(I?)V").matches(mn.desc)) {
					List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
							Mask.BIPUSH.operand(-128),
							Mask.IAND.distance(2)
							);
					if (pattern != null) {
						Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeFlags"));
						for(MethodData md2 : md.methodReferences){
							if(new Wildcard("(I?)V").matches(md2.METHOD_DESC)){
								Hook.BUFFER.put(new RSMethod(md2.bytecodeMethod, "writeByte"));
								break;
							}
						}
					}
					else{
						if(md.methodReferences.size()>1){
							Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeSmart"));
							for(MethodData md2 : md.methodReferences){
								if(new Wildcard("(I?)V").matches(md2.METHOD_DESC)){
									pattern = Assembly.find(md2.bytecodeMethod,
											Mask.BIPUSH.operand(8),
											Mask.ISHR
											);
									if (pattern != null) {
										Hook.BUFFER.put(new RSMethod(md2.bytecodeMethod, "writeShort"));
										break;
									}
								}
							}
						}
						List<AbstractInsnNode> shift8 = Assembly.find(md.bytecodeMethod,
								Mask.BIPUSH.operand(8),
								Mask.IAND.or(Mask.ISHR).distance(2)
								);
						List<AbstractInsnNode> shift16 = Assembly.find(md.bytecodeMethod,
								Mask.BIPUSH.operand(16),
								Mask.IAND.or(Mask.ISHR).distance(2)
								);
						List<AbstractInsnNode> shift24 = Assembly.find(md.bytecodeMethod,
								Mask.BIPUSH.operand(24),
								Mask.IAND.or(Mask.ISHR).distance(2)
								);
						List<AbstractInsnNode> add128 = Assembly.find(md.bytecodeMethod,
								Mask.SIPUSH.operand(128),
								Mask.IADD.distance(2)
								);
						List<AbstractInsnNode> sub128 = Assembly.find(md.bytecodeMethod,
								Mask.SIPUSH.operand(128),
								Mask.ISUB.distance(2)
								);
						List<AbstractInsnNode> negMask = Assembly.find(md.bytecodeMethod,
								Mask.ICONST_0,
								Mask.I2B.distance(3)
								);
						List<AbstractInsnNode> noMask = Assembly.find(md.bytecodeMethod,
								Mask.ILOAD,
								Mask.I2B
								);
						int count=0;
						for(FieldData fd : md.fieldReferences){
							if(fd.bytecodeField.desc.equals("[B"))
								count++;
						}
						if(count==1){
							if(sub128!=null){
								Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeByteS"));
							}
							if(add128!=null){
								Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeByteA"));
							}
							if(negMask!=null){
								Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeNegByte"));
							}
							if(negMask==null && add128==null && sub128==null){
								count=0;
								for(FieldData fd : md.fieldReferences){
									if(fd.bytecodeField.desc.equals("I"))
										count++;
								}
								if(count==1){
									Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeSizeByte"));
								}
							}
						}
						else if(count==2){
							pattern = Assembly.find(md.bytecodeMethod,
									Mask.ICONST_2
									);
							if (pattern != null) {
								Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeSizeShort"));
							}
							else{
								if(shift8!=null && add128!=null){
									//figure out which is written first, the shift8 or the add128
									//block control flow required
									for(BasicBlock bb : md.bytecodeMethod.flowVisitor.blocks){
										AbstractInsnNode sipush = bb.get(Opcodes.SIPUSH);
										if(sipush!=null){
											if(sipush.index()>20){//Referenced second in the block
												Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeShortA"));
											}
											else{
												Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeLEShortA"));
											}
											break;
										}
									}
								}
								else{
									for(BasicBlock bb : md.bytecodeMethod.flowVisitor.blocks){
										AbstractInsnNode bipush = bb.get(Opcodes.BIPUSH);
										if(bipush!=null){
											if(bipush.index()>20){//Referenced second in the block
												Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeLEShort"));
											}
											else{
												//writeShort
											}
											break;
										}
									}
								}
							}
						}
						else if(count==3){
							for(BasicBlock bb : md.bytecodeMethod.flowVisitor.blocks){
								if(bb.size()>30){
									AbstractInsnNode ain = bb.get(Opcodes.BIPUSH);
									if(((IntInsnNode)ain).operand==16){
										Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeMediumInt"));
									}
									else{
										Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeMediumIntA"));
									}
									break;
								}
							}
						}
						else if(count==4){
							count=0;
							for(FieldData fd : md.fieldReferences){
								if(fd.bytecodeField.desc.equals("I"))
									count++;
							}
							if(count==4){
								Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeSizeInt"));
							}
							else{
								if(md.referencedFrom.size()>0){
									//writeInt
								}
								else{
									BasicBlock bb = md.bytecodeMethod.flowVisitor.blocks.get(0);
									if(bb.size()<40){
										AbstractInsnNode ain = bb.get(Opcodes.BIPUSH);
										if(ain!=null && ((IntInsnNode)ain).operand==8){
											Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeMEInt2"));
										}
										if(ain!=null && ((IntInsnNode)ain).operand==16){
											Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeMEInt1"));
										}
									}
									else{
										Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeLEInt"));
									}
								}
							}
						}
					}
				}
				if (new Wildcard("(Ljava/lang/CharSequence;?)V").matches(mn.desc)) {
					Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeEncodedString"));
				}
				if (new Wildcard("(Ljava/lang/String;?)V").matches(mn.desc)) {
					boolean isJString=false;
					for(MethodData md2 : md.referencedFrom){
						if(new Wildcard("(JLjava/lang/String;?)I").matches(md2.METHOD_DESC)){
							isJString=true;
							break;
						}
					}
					if(isJString)
						Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeJString"));
					else
						Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeString"));
				}
				if (new Wildcard("(?)Ljava/lang/String;").matches(mn.desc)) {
					int count=0;
					for(FieldData fd : md.fieldReferences){
						if(fd.bytecodeField.desc.equals("[B"))
							count++;
					}
					if(count==1){
						Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readCheckedString"));
						for(MethodData md2 : md.methodReferences){
							if(new Wildcard("(?)Ljava/lang/String;").matches(md2.METHOD_DESC)){
								Hook.BUFFER.put(new RSMethod(md2.bytecodeMethod, "readString"));
								break;
							}
						}
					}
					else if(count==2){
						//Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "getString"));
					}
					else if(count==3){
						List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
								Mask.ISUB,
								Mask.ISUB.distance(2)
								);
						if (pattern != null) {
							Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readJString"));
						}
						else{
							Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "readJString2"));
						}
					}
				}
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
					else{
						Hook.BUFFER.put(new RSMethod(md.bytecodeMethod, "writeLELong"));
					}
				}
				if (new Wildcard("(Ljava/math/BigInteger;Ljava/math/BigInteger;?)V").matches(mn.desc)) {
					Hook.BUFFER.put(new RSMethod(mn, "applyRSA"));
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
					else{
						Hook.BUFFER.put(new RSMethod(mn, "encodeXTEA2"));
					}
				}
				if(new Wildcard("([III?)V").matches(mn.desc)) {
					if(md.referencedFrom.size()>0){
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
					else{
						Hook.BUFFER.put(new RSMethod(mn, "decodeXTEA2"));
					}
				}
			}
		}
	}
}
