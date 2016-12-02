package com.dank.analysis.impl.definition.npc;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.ConversionNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import com.dank.DankEngine;
import com.dank.analysis.Analyser;
import com.dank.analysis.impl.client.visitor.EngineVarVisitor;
import com.dank.analysis.impl.misc.GStrings;
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

//All methods identified as of r113
public class NpcDefinition extends Analyser {
	@Override
	public ClassSpec specify(ClassNode cn) {
		if(cn.access!=33)
			return null;
		if(!cn.superName.equals(Hook.DUAL_NODE.getInternalName()))
			return null;
		int nonstatic=0, stringArray=0, strings=0, shortArray=0, intArray=0, bools=0;
		for(FieldNode fn : cn.fields){
			if(fn.isStatic())
				continue;
			nonstatic++;
			if(fn.desc.equals("[Ljava/lang/String;"))
				stringArray++;
			if(fn.desc.equals("Ljava/lang/String;"))
				strings++;
			if(fn.desc.equals("[S"))
				shortArray++;
			if(fn.desc.equals("[I"))
				intArray++;
			if(fn.desc.equals("Z"))
				bools++;
		}
		if(nonstatic==33 && stringArray==1 && strings==1 && shortArray==4 && intArray==3 && bools==5)
			return new ClassSpec(Hook.NPC_DEFINITION, cn);
		return null;
	}
	@Override
	public void evaluate(ClassNode cn) {
		for (final FieldNode fn : cn.fields) {
			if (!fn.isStatic()) {
				if (fn.desc.equals("Ljava/lang/String;")) {
					Hook.NPC_DEFINITION.put(new RSField(fn, "name"));
				} else if (fn.desc.equals("[Ljava/lang/String;")) {
					Hook.NPC_DEFINITION.put(new RSField(fn, "actions"));
				}
			}
		}
		for (final MethodNode mn : cn.methods) {
			MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
			if(new Wildcard("(L"+Hook.BUFFER.getInternalName()+";I?)V").matches(mn.desc)){
				Hook.NPC_DEFINITION.put(new RSMethod(mn, "unpackBuffer"));
				for(MethodData md2 : md.referencedFrom){
					if(new Wildcard("(L"+Hook.BUFFER.getInternalName()+";?)V").matches(md2.METHOD_DESC)){
						Hook.NPC_DEFINITION.put(new RSMethod(md2.bytecodeMethod, "readBuffer"));
						break;
					}
				}
			}
			if(new Wildcard("(?)Z").matches(mn.desc)){
				Hook.NPC_DEFINITION.put(new RSMethod(mn, "childDefinitionExists"));
			}
			if(new Wildcard("(?)L"+Hook.STILL_MODEL.getInternalName()+";").matches(mn.desc)){
				Hook.NPC_DEFINITION.put(new RSMethod(mn, "getBasicModel"));
				HashMap<String, FieldData> data = new HashMap<String, FieldData>();
				HashMap<String, Integer> count = new HashMap<String, Integer>();
				for(FieldData fd : md.fieldReferences){
					if(fd.bytecodeField.desc.equals("[I")){
						String s = fd.CLASS_NAME+"."+fd.FIELD_NAME;
						if(!data.containsKey(s)){
							data.put(s, fd);
							count.put(s, 0);
						}
						count.put(s, count.get(s)+1);
					}
				}
				int max=-1;
				String maxKey="";
				for(String key : data.keySet()){
					int i = count.get(key);
					if(i>max){
						max=i;
						maxKey=key;
					}
				}
				FieldData modelIndexs = data.get(maxKey);
				Hook.NPC_DEFINITION.put(new RSField(modelIndexs.bytecodeField, "headModelIds"));
			}
			if(new Wildcard("(L"+Hook.ANIMATION_SEQUENCE.getInternalName()+";IL"+Hook.ANIMATION_SEQUENCE.getInternalName()+";I?)L"+Hook.MODEL.getInternalName()+";").matches(mn.desc)){
				Hook.NPC_DEFINITION.put(new RSMethod(mn, "getAnimatedModel"));
				HashMap<String, FieldData> data = new HashMap<String, FieldData>();
				HashMap<String, Integer> count = new HashMap<String, Integer>();
				for(FieldData fd : md.fieldReferences){
					if(fd.bytecodeField.desc.equals("[I")){
						String s = fd.CLASS_NAME+"."+fd.FIELD_NAME;
						if(!data.containsKey(s)){
							data.put(s, fd);
							count.put(s, 0);
						}
						count.put(s, count.get(s)+1);
					}
				}
				int max=-1;
				String maxKey="";
				for(String key : data.keySet()){
					int i = count.get(key);
					if(i>max){
						max=i;
						maxKey=key;
					}
				}
				FieldData modelIndexs = data.get(maxKey);
				Hook.NPC_DEFINITION.put(new RSField(modelIndexs.bytecodeField, "modelIds"));
			}
			if (!Modifier.isStatic(mn.access)) {
				mn.graph().forEach(block -> block.tree().accept(new IdVisitor(block)));
				if (mn.desc.endsWith(String.format("L%s;", cn.name))) {
					for (final BasicBlock block : mn.graph()) {
						block.tree().accept(new TempTransformVisitor(block));

					}
				} 
				else if (mn.desc.endsWith(";")) {
					for (final BasicBlock block : mn.graph()) {
						block.tree().accept(new ColorVisitor());
					}
				}
			}
		}

		for (final MethodNode mn : cn.methods) {
			if (!Modifier.isStatic(mn.access)) {
				if (mn.desc.endsWith(String.format("L%s;", cn.name))) {
					for (final BasicBlock block : mn.graph()) {
						block.tree().accept(new TransformVisitor(block)); //requires the other transform to be done first..
					}
					Hook.NPC_DEFINITION.put(new RSMethod(mn, "transform"));
				}
			}
		}
		for (final ClassNode c : DankEngine.classPath.getClasses()) {
			for (final MethodNode mn : c.methods) {
				if(new Wildcard("(L"+Hook.NPC_DEFINITION.getInternalName()+";III?)V").matches(mn.desc)){
					List<AbstractInsnNode> pattern = Assembly.find(mn,
        					Mask.INVOKEVIRTUAL,
        					Mask.GETSTATIC.describe("Ljava/lang/String;"),
        					Mask.INVOKEVIRTUAL,
        					Mask.GETSTATIC.describe("Ljava/lang/String;"),
        					Mask.INVOKEVIRTUAL,
        					Mask.GETFIELD.distance(3).describe("I"),
        					Mask.IMUL.distance(3),
        					Mask.INVOKEVIRTUAL,
        					Mask.GETSTATIC.describe("Ljava/lang/String;"),
        					Mask.INVOKEVIRTUAL,
        					Mask.INVOKEVIRTUAL
        					);
            		if(pattern!=null){
            			FieldInsnNode fin = (FieldInsnNode) pattern.get(5);
    					Hook.NPC_DEFINITION.put(new RSField(fin, "combatLevel"));
            		}
				}
				if(mn.isStatic()){
					if (mn.desc.startsWith("(I") && mn.desc.endsWith("L" + cn.name + ";")) {
						Hook.CLIENT.put(new RSMethod(mn, "getNpcDefinition"));
					}
				} else if (mn.name.equals("<clinit>") && c.name.equals(Hook.CLIENT.get("tempVars").owner)) {
					mn.graph().forEach(b -> b.tree().accept(new EngineVarVisitor()));
				}
			}
		}
	}
	private final class ColorVisitor extends NodeVisitor {
		@Override
		public void visitMethod(MethodMemberNode mmn) {
			if (mmn.opcode() == INVOKEVIRTUAL) {
				final MethodNode mn = DankEngine.lookupMethod(mmn.owner(), mmn.name(), mmn.desc());
				if (mn != null && !contains(mn.instructions, IFNONNULL)) { //if it has IFNONNULL then it's textures and modifiedTextures
					mmn.tree().accept(new NodeVisitor() {
						@Override
						public void visitField(FieldMemberNode fmn) {
							if (fmn.desc().equals("[S")) {
								if (Hook.NPC_DEFINITION.get("colors") == null) {
									Hook.NPC_DEFINITION.put(new RSField(fmn, "colors"));
								} else if (Hook.NPC_DEFINITION.get("modifiedColors") == null) {
									Hook.NPC_DEFINITION.put(new RSField(fmn, "modifiedColors"));
								}
							}
						}
					});
				}
			}
		}
		private boolean contains(InsnList iList, int op) {
			for (final AbstractInsnNode ain : iList) {
				if (ain.opcode() == op) {
					return true;
				}
			}
			return false;
		}
	}
	private class TransformVisitor extends TreeVisitor {
		public TransformVisitor(BasicBlock block) {
			super(block);
		}
		@Override
		public boolean validateBlock(BasicBlock block) {
			return true;
		}
		@Override
		public void visitField(final FieldMemberNode fmn) {
			if (fmn.opcode() == GETFIELD) {
				if (fmn.desc().equals("I") && !fmn.name().equals(Hook.NPC_DEFINITION.get("varp32Index").name)) {
					Hook.NPC_DEFINITION.put(new RSField(fmn, "varpIndex"));
				} else if (fmn.desc().equals("[I")) {
					Hook.NPC_DEFINITION.put(new RSField(fmn, "transformIds"));
				}
			}
		}
	}
	private class TempTransformVisitor extends TreeVisitor {
	    public TempTransformVisitor(BasicBlock block) {
	        super(block);
	    }
	    @Override
	    public boolean validateBlock(BasicBlock block) {
	        return true;
	    }
	    @Override
	    public void visitField(final FieldMemberNode fmn) {
	        if (fmn.opcode() == GETSTATIC && fmn.desc().equals("[I") && fmn.parent().first(IMUL) != null) {
	            for (final AbstractNode an : fmn.parent().first(IMUL)) {
	                if (an.opcode() == GETFIELD) {
	                    Hook.NPC_DEFINITION.put(new RSField((FieldMemberNode) an, "varp32Index"));
	                    Hook.CLIENT.put(new RSField(fmn, "tempVars"));
	                }
	            }
	        }
	    }
	}
	private class IdVisitor extends TreeVisitor {
	    public IdVisitor(BasicBlock block) {
	        super(block);
	    }
	    @Override
	    public boolean validateBlock(BasicBlock block) {
	        return block.count(I2L) > 0;
	    }
	    @Override
	    public void visitConversion(final ConversionNode cn) {
	        if (cn.fromInt() && cn.toLong()) {
	            final FieldMemberNode fmn = (FieldMemberNode) cn.layer(IMUL, GETFIELD);
	            if (fmn != null) {
	                Hook.NPC_DEFINITION.put(new RSField(fmn, "id"));
	            }
	        }
	    }
	}
}
