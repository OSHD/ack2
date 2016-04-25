package com.dank.analysis.impl.landscape;

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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

//All fields and methods identified as of r111
public class Landscape extends Analyser implements Opcodes {
	@Override
	public ClassSpec specify(ClassNode cn) {
		int tipint = 0;
		for(FieldNode fn : cn.fields){
			if(fn.isStatic())
				continue;
			if(fn.desc.equals("[[I") || fn.desc.equals("[[[I"))
				tipint++;
		}
		if(tipint!=4)
			return null;
		return new ClassSpec(Hook.LANDSCAPE, cn);
	}
	@Override
	public void evaluate(ClassNode cn) {
		for(MethodNode mn : cn.methods){
			if(mn.isStatic())
				continue;
			MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
			if(md.referencedFrom.size()>0 && new Wildcard("(L"+Hook.PLAIN_TILE.getInternalName()+";IIIIIII)V").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "renderPlainTile"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(L"+Hook.SHAPED_TILE.getInternalName()+";IIIIII)V").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "renderShapedTile"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIII)I").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "getIDTagForXYZ"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIII)Z").matches(md.METHOD_DESC)){
				boolean found=false;
				for(FieldData fd : md.fieldReferences){
					if(fd.bytecodeField.desc.equals("I"))
						found=true;
				}
				if(found)
					Hook.LANDSCAPE.put(new RSMethod(mn, "visibilityBlocked"));
				else
					Hook.LANDSCAPE.put(new RSMethod(mn, "isRenderable"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIII)V").matches(md.METHOD_DESC)){
				int intAccess=0;
				for(FieldData fd : md.fieldReferences){
					if(fd.bytecodeField.desc.equals("I"))
						intAccess++;
				}
				if(intAccess==1)
					Hook.LANDSCAPE.put(new RSMethod(mn, "setTileLogicHeight"));
				else if(intAccess==4)
					Hook.LANDSCAPE.put(new RSMethod(mn, "snapBoundaryModels"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("()V").matches(md.METHOD_DESC)){
				boolean found=true;
				for(FieldData fd : md.fieldReferences){
					if(fd.bytecodeField.desc.equals("[[Z")){
						found=false;
						break;
					}
				}
				boolean isInit=false;
				for(MethodData md2 : md.referencedFrom){
					if(md2.METHOD_NAME.equals("<init>")){
						isInit=true;
						break;
					}
				}
				if(found && !isInit)
					Hook.LANDSCAPE.put(new RSMethod(mn, "clearEntityMarkerCache"));
				else{
					if(isInit)
						Hook.LANDSCAPE.put(new RSMethod(mn, "initToNull"));
				}
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(I)V").matches(md.METHOD_DESC)){
				boolean found=false;
				for(MethodData md2 : md.methodReferences){
					if(md2.METHOD_NAME.equals("<init>")){
						found=true;
						break;
					}
				}
				if(found)
					Hook.LANDSCAPE.put(new RSMethod(mn, "setHeightLevel"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(III)Z").matches(md.METHOD_DESC)){
				boolean found=false;
				for(FieldData fd : md.fieldReferences){
					if(fd.bytecodeField.desc.equals("[[[I")){
						found=true;
						break;
					}
				}
				if(found){
					Hook.LANDSCAPE.put(new RSMethod(mn, "visibleInViewport"));
					for(MethodData md2 : md.methodReferences){
						if(md.METHOD_DESC.equals(md2.METHOD_DESC)){
							Hook.LANDSCAPE.put(new RSMethod(md2.bytecodeMethod, "updateViewportVisibility"));
							break;
						}
					}
					for(MethodData md2 : md.referencedFrom){
						if(new Wildcard("(IIIIII)Z").matches(md2.METHOD_DESC)){
							Hook.LANDSCAPE.put(new RSMethod(md2.bytecodeMethod, "updateViewport"));
						}
					}
				}
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIIIIIIIIIIIIIIIII)V").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "addTile"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(II)V").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "applyBridgeMode"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIIIII)Z").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "isMouseWithinTriangle"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(III)L"+Hook.BOUNDARY_STUB.getInternalName()+";").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "getBoundaryStub"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(III)L"+Hook.BOUNDARY_DECORATION_STUB.getInternalName()+";").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "getBoundaryDecorationStub"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(III)L"+Hook.ENTITY_MARKER.getInternalName()+";").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "getEntityMarker"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(III)L"+Hook.TILE_DECORATION_STUB.getInternalName()+";").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "getTileDecorationStub"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIII)V").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "render"));
				boolean map[]=new boolean[]{false, false};
				for(MethodData md2 : md.methodReferences){
					if(!map[0] && new Wildcard("()V").matches(md2.METHOD_DESC)){
						Hook.LANDSCAPE.put(new RSMethod(md2.bytecodeMethod, "processBoundaries"));
						map[0]=true;
					}
					else if(!map[1]){
						Hook.LANDSCAPE.put(new RSMethod(md2.bytecodeMethod, "renderTile"));
						map[1]=true;
					}
					if(map[0] && map[1])
						break;
				}
			}
			if(md.referencedFrom.size()>0 && new Wildcard("([IIIIII)V").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "drawMinimapTile"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIIL"+Hook.ENTITY.getInternalName()+";IIIIII)Z").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "addEntity"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIIL"+Hook.ENTITY.getInternalName()+";IIZ)Z").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "addTempEntity"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIIIL"+Hook.ENTITY.getInternalName()+";III)Z").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "addObject"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(L"+Hook.ENTITY_MARKER.getInternalName()+";)V").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "removeObject"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIL"+Hook.ENTITY.getInternalName()+";II)V").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "addTileDecoration"));
				Hook.TILE_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 2, Hook.TILE_DECORATION_STUB), "strictX"));
				Hook.TILE_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 3, Hook.TILE_DECORATION_STUB), "strictY"));
				Hook.TILE_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 4, Hook.TILE_DECORATION_STUB), "height"));
				Hook.TILE_DECORATION_STUB.put(new RSField(load(mn, ALOAD, 5, Hook.TILE_DECORATION_STUB), "entity"));
				Hook.TILE_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 6, Hook.TILE_DECORATION_STUB), "uid"));
				Hook.TILE_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 7, Hook.TILE_DECORATION_STUB), "config"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIL"+Hook.ENTITY.getInternalName()+";IL"+Hook.ENTITY.getInternalName()+";L"+Hook.ENTITY.getInternalName()+";)V").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "addItemPile"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIIIIIL"+Hook.ENTITY.getInternalName()+";IZII)Z").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "addEntityMarker"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 1, Hook.ENTITY_MARKER), "floorLevel"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 2, Hook.ENTITY_MARKER), "regionX"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 3, Hook.ENTITY_MARKER), "regionY"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 4, Hook.ENTITY_MARKER), "maxX"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 5, Hook.ENTITY_MARKER), "maxY"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 6, Hook.ENTITY_MARKER), "strictX"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 7, Hook.ENTITY_MARKER), "strictY"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 8, Hook.ENTITY_MARKER), "height"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ALOAD, 9, Hook.ENTITY_MARKER), "entity"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 10, Hook.ENTITY_MARKER), "orientation"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 12, Hook.ENTITY_MARKER), "uid"));
				Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 13, Hook.ENTITY_MARKER), "config"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIL"+Hook.ENTITY.getInternalName()+";L"+Hook.ENTITY.getInternalName()+";IIII)V").matches(md.METHOD_DESC)){
				//TODO remove these and hook inside their perspective class owners
				Hook.LANDSCAPE.put(new RSMethod(mn, "addBoundary"));
				Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 2, Hook.BOUNDARY_STUB), "strictX"));
				Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 3, Hook.BOUNDARY_STUB), "strictY"));
				Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 4, Hook.BOUNDARY_STUB), "height"));
				Hook.BOUNDARY_STUB.put(new RSField(load(mn, ALOAD, 5, Hook.BOUNDARY_STUB), "entityA"));
				Hook.BOUNDARY_STUB.put(new RSField(load(mn, ALOAD, 6, Hook.BOUNDARY_STUB), "entityB"));
				Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 7, Hook.BOUNDARY_STUB), "orientationA"));
				Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 8, Hook.BOUNDARY_STUB), "orientationB"));
				Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 9, Hook.BOUNDARY_STUB), "uid"));
				Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 10, Hook.BOUNDARY_STUB), "config"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(IIIIL"+Hook.ENTITY.getInternalName()+";L"+Hook.ENTITY.getInternalName()+";IIIIII)V").matches(md.METHOD_DESC)){
				Hook.LANDSCAPE.put(new RSMethod(mn, "addBoundaryDecoration"));
				//TODO remove these and hook inside their perspective class owners
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 2, Hook.BOUNDARY_DECORATION_STUB), "strictX"));
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 3, Hook.BOUNDARY_DECORATION_STUB), "strictY"));
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 4, Hook.BOUNDARY_DECORATION_STUB), "height"));
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ALOAD, 5, Hook.BOUNDARY_DECORATION_STUB), "entityA"));
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ALOAD, 6, Hook.BOUNDARY_DECORATION_STUB), "entityB"));
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 7, Hook.BOUNDARY_DECORATION_STUB), "orientationA"));
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 8, Hook.BOUNDARY_DECORATION_STUB), "orientationB"));
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 9, Hook.BOUNDARY_DECORATION_STUB), "insetX"));
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 10, Hook.BOUNDARY_DECORATION_STUB), "insetY"));
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 11, Hook.BOUNDARY_DECORATION_STUB), "uid"));
				Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 12, Hook.BOUNDARY_DECORATION_STUB), "config"));
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(III)I").matches(md.METHOD_DESC)){
				List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
						Mask.GETFIELD,
						Mask.IMUL.distance(2),
						Mask.IRETURN
						);
				if (pattern != null) {
					FieldInsnNode fin = (FieldInsnNode)pattern.get(0);
					if(Hook.TILE_DECORATION_STUB.getInternalName().equals(fin.owner)){
						Hook.LANDSCAPE.put(new RSMethod(mn, "getTileDecorationStubUID"));
					}
					if(Hook.BOUNDARY_STUB.getInternalName().equals(fin.owner)){
						Hook.LANDSCAPE.put(new RSMethod(mn, "getBoundaryStubUID"));
					}
					if(Hook.BOUNDARY_DECORATION_STUB.getInternalName().equals(fin.owner)){
						Hook.LANDSCAPE.put(new RSMethod(mn, "getBoundaryDecorationStubUID"));
					}
					if(Hook.ENTITY_MARKER.getInternalName().equals(fin.owner)){
						Hook.LANDSCAPE.put(new RSMethod(mn, "getEntityMarkerUID"));
					}
				}
			}
			if(md.referencedFrom.size()>0 && new Wildcard("(III)V").matches(md.METHOD_DESC)){
				if(md.fieldReferences.size()==2){
					boolean found=false;
					FieldData type=null;
					for(FieldData fd : md.fieldReferences){
						if(fd.bytecodeField.desc.startsWith("[[[L"))
							found=true;
						else
							type=fd;
					}
					if(found && type!=null){
						if(Hook.TILE_DECORATION_STUB.getInternalDesc().equals(type.bytecodeField.desc)){
							Hook.LANDSCAPE.put(new RSMethod(mn, "removeTileDecorationStub"));
						}
						if(Hook.BOUNDARY_STUB.getInternalDesc().equals(type.bytecodeField.desc)){
							Hook.LANDSCAPE.put(new RSMethod(mn, "removeBoundaryStub"));
						}
						if(Hook.BOUNDARY_DECORATION_STUB.getInternalDesc().equals(type.bytecodeField.desc)){
							Hook.LANDSCAPE.put(new RSMethod(mn, "removeBoundaryDecorationStub"));
						}
						if(Hook.ITEM_PILE.getInternalDesc().equals(type.bytecodeField.desc)){
							Hook.LANDSCAPE.put(new RSMethod(mn, "removeItemPile"));
						}
					}
				}
				else{
					boolean found=false;
					for(FieldData fd : md.fieldReferences){
						if(fd.bytecodeField.desc.equals("Z")){
							found=true;
						}
					}
					if(found)
						Hook.LANDSCAPE.put(new RSMethod(mn, "requestTrace"));
					else{
						boolean isShade=false;
						for(FieldData fd : md.fieldReferences){
							if(fd.bytecodeField.desc.equals("S"))
								isShade=true;
						}
						if(!isShade)
							Hook.LANDSCAPE.put(new RSMethod(md.bytecodeMethod, "removeEntityMarker"));
						else{
							Hook.LANDSCAPE.put(new RSMethod(mn, "shadeModels"));
							boolean[] map=new boolean[]{false, false};
							for(MethodData md2 : md.methodReferences){
								if(!map[0] && new Wildcard("(L*;IIIII)V").matches(md2.METHOD_DESC)){
									Hook.LANDSCAPE.put(new RSMethod(md2.bytecodeMethod, "shadeModel"));
									map[0]=true;
								}
								if(!map[1] && new Wildcard("(L*;III)V").matches(md2.METHOD_DESC)){
									Hook.LANDSCAPE.put(new RSMethod(md2.bytecodeMethod, "offsetModelVertexs"));
									map[1]=true;
								}
							}
						}
					}
				}
			}
		}
		for (final FieldNode fn : cn.fields) {
			if (!Modifier.isStatic(fn.access)) {
				FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
				if(fn.desc.equals("[[[I")){
					boolean isheights=false;
					for(MethodData md : fd.referencedFrom){
						if(new Wildcard("(L*;IIIII)V").matches(md.METHOD_DESC))
							isheights=true;
					}
					if(isheights)
						Hook.LANDSCAPE.put(new RSField(fn, "heightmap"));
					else
						Hook.LANDSCAPE.put(new RSField(fn, "renderableTiles"));
				}
				if(fn.desc.equals("[[I")){
					for(MethodData md : fd.referencedFrom){
						if(new Wildcard("([IIIIII)V").matches(md.METHOD_DESC)){
							List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md.bytecodeMethod,
									Mask.ALOAD,
									Mask.ILOAD,
									Mask.ALOAD,
									Mask.ALOAD,
									Mask.ILOAD,
									Mask.IINC,
									Mask.IALOAD,
									Mask.IALOAD
									);
							if (patterns != null) {
								for(ArrayList<AbstractInsnNode> pattern : patterns){
									VarInsnNode var1 = (VarInsnNode) pattern.get(2);
									VarInsnNode var2 = (VarInsnNode) pattern.get(3);
									if((var1.var+1)==var2.var){
										List<ArrayList<AbstractInsnNode>> patterns2 = Assembly.findAll(md.bytecodeMethod,
												Mask.ALOAD,
												Mask.GETFIELD.own(Hook.LANDSCAPE.getInternalName()),
												Mask.ILOAD,
												Mask.AALOAD,
												Mask.ASTORE
												);
										if (patterns2 != null) {
											for(ArrayList<AbstractInsnNode> pattern2 : patterns2){
												VarInsnNode check = (VarInsnNode) pattern2.get(4);
												FieldInsnNode fin = (FieldInsnNode) pattern2.get(1);
												FieldData tileShape = DynaFlowAnalyzer.getField(fin.owner, fin.name);
												if(check.var==var1.var){
													Hook.LANDSCAPE.put(new RSField(tileShape.bytecodeField, "tileShapePoints"));
												}
												else if(check.var==var2.var){
													Hook.LANDSCAPE.put(new RSField(tileShape.bytecodeField, "tileShapeIndices"));
												}
											}
										}
									}
								}
							}
							break;
						}
					}
				}
				if(fn.desc.equals("I")){
					RSMethod ref = (RSMethod)Hook.LANDSCAPE.get("clearEntityMarkerCache");
					if(ref!=null){
						MethodData refM = DynaFlowAnalyzer.getMethod(ref.owner, ref.name, ref.desc);
						boolean isEntityCachePos=false;
						for(MethodData md : fd.referencedFrom){
							if(refM.equals(md)){
								isEntityCachePos=true;
								break;
							}
						}
						if(isEntityCachePos)
							Hook.LANDSCAPE.put(new RSField(fd.bytecodeField, "entityCachePosition"));
						else{
							ref = (RSMethod)Hook.LANDSCAPE.get("setHeightLevel");
							if(ref!=null){
								refM = DynaFlowAnalyzer.getMethod(ref.owner, ref.name, ref.desc);
								boolean isCurrHL=false;
								for(MethodData md : fd.referencedFrom){
									if(refM.equals(md)){
										isCurrHL=true;
										break;
									}
								}
								if(!isCurrHL)
									Hook.LANDSCAPE.put(new RSField(fd.bytecodeField, "zMapSize"));
								else{
									List<AbstractInsnNode> pattern = Assembly.find(refM.bytecodeMethod,
											Mask.PUTFIELD.own(fd.CLASS_NAME).describe(fd.bytecodeField.desc)
											);
									if (pattern != null && isCurrHL && ((FieldInsnNode)pattern.get(0)).name.equals(fd.FIELD_NAME)){
										Hook.LANDSCAPE.put(new RSField(fd.bytecodeField, "currentHeightLevel"));
									}
									else{
										ref = (RSMethod)Hook.LANDSCAPE.get("offsetModelVertexs");
										refM = DynaFlowAnalyzer.getMethod(ref.owner, ref.name, ref.desc);
										pattern = Assembly.find(refM.bytecodeMethod,
												Mask.ILOAD.cst(3),
												Mask.ALOAD,
												Mask.GETFIELD.describe("I")
												);
										if (pattern != null){
											FieldInsnNode fin = (FieldInsnNode)pattern.get(2);
											if(fin.owner.equals(fd.CLASS_NAME) && fin.name.equals(fd.FIELD_NAME)){
												Hook.LANDSCAPE.put(new RSField(fd.bytecodeField, "xMapSize"));
											}
											else{
												Hook.LANDSCAPE.put(new RSField(fd.bytecodeField, "yMapSize"));
											}
										}
									}
								}
							}
						}
					}
				}
				if (fn.desc.startsWith("[[[L")) {
					Hook.LANDSCAPE.put(new RSField(fn, "tiles"));
				} else if (fn.desc.startsWith("[L")) {
					Hook.LANDSCAPE.put(new RSField(fn, "tempEntityMarkers"));
				}
			} else {
				if(fn.desc.equals("[[[[Z")) {
					Hook.LANDSCAPE.put(new RSField(fn, "visibilityMap"));
				}
			}
		}
	}

	private FieldInsnNode load(final MethodNode mn, final int opcode, final int index, final Hook owner) {
		for (final AbstractInsnNode ain : mn.instructions.toArray()) {
			if (ain instanceof VarInsnNode) {
				final VarInsnNode vin = (VarInsnNode) ain;
				if (vin.var == index && vin.opcode() == opcode) {
					AbstractInsnNode dog = vin;
					for (int i = 0; i < 7; i++) {
						if (dog == null) break;
						if (dog.opcode() == PUTFIELD && ((FieldInsnNode) dog).owner.equals(owner.getInternalName())) {
							return (FieldInsnNode) dog;
						}
						dog = dog.next();
					}
				}
			}
		}
		return null;
	}
}
