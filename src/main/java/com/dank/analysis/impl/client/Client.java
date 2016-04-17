package com.dank.analysis.impl.client;

import com.dank.DankEngine;
import com.dank.analysis.Analyser;
import com.dank.analysis.impl.character.visitor.HitpointsVisitor;
import com.dank.analysis.impl.character.visitor.OrientationVisitor;
import com.dank.analysis.impl.client.interpret.LoadState;
import com.dank.analysis.impl.client.visitor.*;
import com.dank.analysis.impl.widget.Widget;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.MemberKey;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.BlockVisitor;
import org.objectweb.asm.commons.cfg.FlowVisitor;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.NodeTree;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.*;
import org.objectweb.asm.commons.cfg.tree.util.TreeBuilder;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Project: RS3Injector Time: 06:50 Date: 07-02-2015 Created by Dogerina.
 */
public class Client extends Analyser {
	@Override
	public ClassSpec specify(ClassNode cn) {
		if (cn.name.equals("client")) {
			Hook.GAME_ENGINE.setInternalName(cn.superName);
			return new ClassSpec(Hook.CLIENT, cn);
		}
		return null;
	}
	@Override
	public void evaluate(ClassNode cn) {
		for(ClassNode node : getClassPath().getClasses()){
			for(FieldNode fn : node.fields){
				FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
				if(fn.access==8 && fn.desc.equals("L"+Hook.HASHTABLE.getInternalName()+";")){
					boolean found=false;
					for(MethodData md : fd.referencedFrom){
						if(new Wildcard("([L"+Hook.WIDGET.getInternalName()+";L"+Hook.WIDGET.getInternalName()+";Z?)V").matches(md.METHOD_DESC)){
							found=true;
							break;
						}
					}
					if(found)
						Hook.CLIENT.put(new RSField(fn, "huds"));
				}
			}
		}
		for (final ClassNode node : getClassPath().getClasses()) {
			for (final MethodNode mn : node.methods) {
				MethodData md = DynaFlowAnalyzer.getMethod(node.name, mn.name, mn.desc);
				if (mn.isStatic() && new Wildcard("(I?)I").matches(mn.desc)) { 
					List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
							Mask.INVOKEVIRTUAL.describe("(J)L"+Hook.DUAL_NODE.getInternalName()+";"),
							Mask.CHECKCAST.describe(Hook.VARPBIT.getInternalName())
							);
					if (pattern != null) {
						Hook.CLIENT.put(new RSMethod(mn, "getVarpbit"));
					}
				}
				if (new Wildcard("(L"+Hook.CHARACTER.getInternalName()+";IIIII?)V").matches(mn.desc)) {
					Hook.CLIENT.put(new RSMethod(mn, "updateEntity"));
					for(MethodData md2 : md.referencedFrom){
						if(new Wildcard("(IIII?)V").matches(md2.METHOD_DESC)){
							Hook.CLIENT.put(new RSMethod(md2.bytecodeMethod, "updateEntities"));
							break;
						}
					}
				}
				if (md.referencedFrom.size()>0 && new Wildcard("([" + Hook.WIDGET.getInternalDesc() + "IIIIIII?)V").matches(mn.desc)) {
					Hook.CLIENT.put(new RSMethod(mn, "buildComponentEvents"));
				}
				if (new Wildcard("([" + Hook.WIDGET.getInternalDesc() + "IIIIIIII?)V").matches(mn.desc)) {
					Hook.CLIENT.put(new RSMethod(mn, "renderComponent"));
				}
				if (new Wildcard("(III?)" + Hook.HUD.getInternalDesc()).matches(mn.desc)) {
					Hook.CLIENT.put(new RSMethod(mn, "addHUD"));
				}
				if (new Wildcard("(" + Hook.HUD.getInternalDesc() + "Z?)V").matches(mn.desc)) {
					Hook.CLIENT.put(new RSMethod(mn, "removeHUD"));
				}
				if (new Wildcard("([" + Hook.WIDGET.getInternalDesc() + Hook.WIDGET.getInternalDesc() + "Z?)V").matches(mn.desc)) {
					Hook.CLIENT.put(new RSMethod(mn, "layoutContainer"));
					TreeBuilder.build(mn).accept(new NodeVisitor() {
						@Override
						public void visitMethod(MethodMemberNode mmn) {
							if (mmn.opcode() == Opcodes.INVOKESTATIC) {
								if (new Wildcard("(IIIZ?)V").matches(mmn.desc())) {
									Hook.CLIENT.put(new RSMethod(mmn, "layoutWindow"));
								}
							}
						}
					});
				}
				if (new Wildcard("([" + Hook.WIDGET.getInternalDesc() + "IIIZ?)V").matches(mn.desc)) {
					Hook.CLIENT.put(new RSMethod(mn, "layoutContainer2"));
				}
				if (new Wildcard("(L*;II?)" + Hook.SPRITE.getInternalDesc()).matches(mn.desc)) {
					Hook.CLIENT.put(new RSMethod(mn, "loadImage"));
				}
				if (new Wildcard("(ILjava/lang/String;Ljava/lang/String;I)V").matches(mn.desc)) {
					TreeBuilder.build(mn).accept(new NodeVisitor() {
						@Override
						public void visitMethod(MethodMemberNode mmn) {
							if (mmn.opcode() == Opcodes.INVOKESTATIC) {
								Hook.CLIENT.put(new RSMethod(mmn, "addMessage"));
							}
						}
					});
				}
				if (new Wildcard("()V").matches(mn.desc)) {
					TreeBuilder.build(mn).accept(new NodeVisitor() {
						@Override
						public void visitMethod(MethodMemberNode mmn) {
							if (mmn.opcode() == Opcodes.INVOKESTATIC) {
								if (new Wildcard("(" + Hook.WIDGET.getInternalDesc() + "?)V").matches(mmn.desc())) {
									if (Hook.CLIENT.get("repaintWidget") == null) {
										Hook.CLIENT.put(new RSMethod(mmn, "repaintWidget"));
									}
									if (Hook.CLIENT.get("layoutComponent") == null &&
											!Hook.CLIENT.get("repaintWidget").name.equals(mmn.name())) {
										Hook.CLIENT.put(new RSMethod(mmn, "layoutComponent"));
									}
								}
							}
						}
					});
				}
				visitMethods(mn);
				if (Modifier.isStatic(mn.access)) {
					getWidgetPositions(mn);
					for (final BasicBlock block : mn.graph()) {
						final NodeTree tree = block.tree();
//						tree.accept(new OrientationVisitor(block));
						tree.accept(new AudioEffectCountVisitor(block));
						tree.accept(new PlayerIndexVisitor(block));
//						tree.accept(new CharacterArrayVisitor(block));
						tree.accept(new MapSpriteVisitor(block));
						tree.accept(new FloorLevelVisitor(block));
						tree.accept(new WorldLoadVisitor(block));
						tree.accept(new W2MVisitor(block));
						tree.accept(new W2M2Visitor(block));
						tree.accept(new PlayerActionsVisitor(block));
						tree.accept(new SelectionVisitor(block));
						tree.accept(new DequeObjectVisitor(block));
						tree.accept(new SelectedItemIndexVisitor(block));
						tree.accept(new CursorStateVisitor(block));
						tree.accept(new ConnectionStateVisitor());
						tree.accept(new FpsVisitor());
						tree.accept(new TileDataVisitor());
						tree.accept(new ScreenVisitor());
						tree.accept(new MessageChannelsVisitor(block));
						tree.accept(new CurrentNameVisitor(block));
					}
				} else if (Modifier.isProtected(mn.access)) {
					for (final BasicBlock block : mn.graph()) {
						final NodeTree tree = block.tree();
						tree.accept(new WidgetPositionVisitor(block));
					}
				}
			}
		}
		for (final ClassNode node : getClassPath().getClasses()) {
			for (final MethodNode mn : node.methods) {
				findRunScript(mn);
				if (Modifier.isStatic(mn.access)) {
					for (final BasicBlock block : mn.graph()) {
						final NodeTree tree = block.tree();
						// dependant on previous
						//tree.accept(new HitpointsVisitor(block));
						tree.accept(new WorldCountVisitor(block));
						tree.accept(new MenuBoundsVisitor(block));
						tree.accept(new RegionOffsetVisitor(block));
						tree.accept(new SelectedNameVisitor(block));
						tree.accept(new SpellTargetsVisitor(block));
						tree.accept(new WidgetActionContentVisitor());
						tree.accept(new WidgetBoundsIndexVisitor());
						tree.accept(new NpcDefCombatLevelVisitor(block));
						if (mn.desc.startsWith("(Ljava/lang/String;Ljava/lang/String;III")) {
							tree.accept(new MenuOpenVisitor());
						} else if (mn.desc.endsWith("V") && Type.getArgumentTypes(mn.desc).length == 3) {
							tree.accept(new DestinationVisitor(block));
						} else if (mn.desc.startsWith("([L") && mn.desc.contains(";IIIIII")) {
							tree.accept(new WidgetPaddingVisitor(block));
							tree.accept(new WidgetSpriteVisitor(block));
						} else if (Type.getArgumentTypes(mn.desc).length < 3
								&& mn.desc.endsWith(Hook.ITEM_DEFINITION.getInternalDesc())) {
							tree.accept(new ItemNotedVisitor(block));
						} else if (mn.desc.endsWith("I") && mn.desc.startsWith("(" + Hook.WIDGET.getInternalDesc())) {
							tree.accept(new WidgetConfigVisitor(mn));
						}
					}
				}
			}
			for (FieldNode fn : node.fields) {
				visitFields(fn);
			}
		}
		for (ClassNode node : getClassPath().getClasses()) {
			for (MethodNode mn : node.methods) {
				if (Modifier.isStatic(mn.access) && mn.desc.startsWith("(L" + Hook.WORLD.getInternalName() + ";")
						&& mn.desc.endsWith("V")) {
					Hook.CLIENT.put(new RSMethod(mn, "setWorld"));
				}
			}
		}
		// all the hacky ones here
		SelectionVisitor.onEnd();
		WidgetPaddingVisitor.onEnd();
		WidgetSpriteVisitor.onEnd();
		SelectedItemIndexVisitor.onEnd();
		Widget.postAnalysis();
		Hook.CLAN_MATE.setInternalName(Hook.CLAN_MATE.get("rank").owner);
		Hook.FRIENDED_PLAYER.setInternalName(Hook.FRIENDED_PLAYER.get("previousName").owner);
		Hook.IGNORED_PLAYER.setInternalName(Hook.IGNORED_PLAYER.get("previousName").owner);
		for (ClassNode node : getClassPath().getClasses()) {
			for (FieldNode fn : node.fields) {
				visitFields2(fn);
			}
		}
		MethodNode mn = getClassPath().get(cn.superName).getMethodByName("focusLost");
		if (mn != null) {
			AtomicReference<FieldInsnNode> fin = new AtomicReference<>();
			mn.graph().forEach(b -> {
				if (fin.get() == null) {
					fin.set((FieldInsnNode) b.get(Opcodes.PUTSTATIC));
				}
			});
			if (fin.get() != null) {
				Hook.CLIENT.put(new RSField(fin.get(), "focused"));
			}
		}
		interpret();
	}
	private void findRunScript(MethodNode mn) {
		if (new Wildcard("(" + Hook.SCRIPT_EVENT.getInternalDesc() + "I?)V").matches(mn.desc)) {
			Hook.CLIENT.put(new RSMethod(mn, "runScript"));
		}
	}
	void interpret() {
		LoadState ls = new LoadState();
		Analyzer<BasicValue> aa = new Analyzer<>(ls);
		for (final ClassNode node : getClassPath().getClasses()) {
			for (final MethodNode mn : node.methods) {
				try {
					aa.analyze(node.name, mn);
					MemberKey key = ls.get();
					if (key != null) {
						Hook.CLIENT.put(new RSField(key, "bootState"));
					}
					ls.clear();
				} catch (AnalyzerException ignored) {
					ignored.printStackTrace();
				}
			}
		}
	}
	private void visitMethods(final MethodNode mn) {
		findCameraVars(mn);
		findViewportBuffer(mn);
		findScreenCenters(mn);
		findKeyFocusListener(mn);
		final NodeTree tree = TreeBuilder.build(mn);
		if (mn.desc.startsWith("(Ljava/lang/String;Ljava/lang/String;IIII")) {
			tree.accept(new MenuActionVisitor());
		} else if (mn.name.equals("<clinit>")) {
		} /*else if (mn.desc.startsWith("(" + Hook.CHARACTER.getInternalDesc())) {
			tree.accept(new CharacterTargetIndexVisitor());
		}*/ else if (mn.isStatic() && mn.desc.startsWith("(I")) {
			if (mn.desc.endsWith(Hook.ITEM_DEFINITION.getInternalDesc())) {
				Hook.CLIENT.put(new RSMethod(mn, "getItemDefinition"));
			} else if (mn.desc.endsWith(Hook.OBJECT_DEFINITION.getInternalDesc())) {
				Hook.CLIENT.put(new RSMethod(mn, "getObjectDefinition"));
			}
		}
		tree.accept(new NodeVisitor() {
			@Override
			public void visitMethod(MethodMemberNode mmn) {
				if (mmn.desc().endsWith(Hook.ITEM_DEFINITION.getInternalDesc()) && Hook.WIDGET.get("itemIds") == null) {
					final FieldMemberNode fmn = (FieldMemberNode) mmn.layer(ISUB, IALOAD, GETFIELD);
					if (fmn != null && fmn.owner().equals(Hook.WIDGET.getInternalName())) {
						Hook.WIDGET.put(new RSField(fmn, "itemIds"));
					}
				} else if (mmn.isStatic() && mmn.desc().startsWith("(IIIIIZ") && Hook.WIDGET.get("itemQuantities") == null) {
					final FieldMemberNode fmn = (FieldMemberNode) mmn.layer(IALOAD, GETFIELD);
					if (fmn != null && fmn.owner().equals(Hook.WIDGET.getInternalName()) && fmn.desc().equals("[I")) {
						Hook.WIDGET.put(new RSField(fmn, "itemQuantities"));
					}
				}
			}
		});
		if (mn.name.equals("focusLost")) {
			mn.graph().forEach(b -> {
				b.tree().accept(new NodeVisitor() {
					@Override
					public void visitField(FieldMemberNode fmn) {
						if (fmn.putting() && fmn.desc().equals("Z")) {
							Hook.GAME_ENGINE.put(new RSField(fmn, "focused"));
						}
					}
				});
			});
		}
		if (mn.name.equals("init")) {
			mn.graph().forEach(b -> {
				b.tree().accept(new NodeVisitor() {
					@Override
					public void visitField(FieldMemberNode fmn) {
						if (fmn.putting() && fmn.desc().equals("Ljava/io/File;")) {
							if (b.instructions.size() == 8) {
								FieldInsnNode fin = (FieldInsnNode) b.instructions.get(1);
								Hook.CLIENT.put(new RSField(fmn, "cacheDirectory"));
								Hook.CLIENT.put(new RSField(getClassPath().get(fin.owner).getField(fin.name, fin.desc, false), "cacheLocation"));
							}
						}
					}
				});
			});
			if (Hook.CLIENT.get("cacheDirectory") == null) {
				mn.graph().forEach(b -> {
					b.tree().accept(new NodeVisitor() {
						@Override
						public void visitMethod(MethodMemberNode mmn) {
							if (mmn.desc().startsWith("(Ljava/lang/String;Ljava/lang/String;")) {
								MethodNode subMethod = DankEngine.lookupMethod(mmn.owner(), mmn.name(), mmn.desc());
								if (subMethod != null)
									subMethod.graph().forEach(b -> {
										b.tree().accept(new NodeVisitor() {
											@Override
											public void visitField(FieldMemberNode fmn) {
												if (fmn.putting() && fmn.desc().equals("Ljava/io/File;")) {
													if (b.instructions.size() == 8) {
														FieldInsnNode fin = (FieldInsnNode) b.instructions.get(1);
														Hook.CLIENT.put(new RSField(fmn, "cacheDirectory"));
														Hook.CLIENT.put(new RSField(getClassPath().get(fin.owner).getField(fin.name, fin.desc, false), "cacheLocation"));
													}
												}
											}
										});
									});
							}
						}
					});
				});
			}
		}
		if (Hook.CLIENT.get("cacheDirectory") == null) {
			if (new Wildcard("(L*;L*;???)V").matches(mn.desc)) {
				mn.graph().forEach(b -> {
					b.accept(new BlockVisitor() {
						@Override
						public boolean validate() {
							return b.count(new MemberQuery(GETSTATIC, "Ljava/io/File;")) >= 1;
						}
						@Override
						public void visit(BasicBlock block) {
							for (AbstractInsnNode ain : block.instructions) {
								if (ain instanceof FieldInsnNode) {
									FieldInsnNode fin = (FieldInsnNode) ain;
									if (fin.desc.equals("Ljava/io/File;") && fin.opcode() == GETSTATIC) {
										Hook.CLIENT.put(new RSField(fin, "cacheDirectory"));
										Hook.CLIENT.put(new RSField(getClassPath().get(fin.owner).getField(fin.name, fin.desc, false), "cacheLocation"));
									}
								}
							}
						}
					});
				});
			}
		}
		if (Hook.CLIENT.get("cacheLocation") == null) {
			if (new Wildcard("(Ljava/lang/String;?)Ljava/io/File;").matches(mn.desc) && Modifier.isStatic(mn.access)) {
				mn.graph().forEach(b -> {
					b.tree().accept(new NodeVisitor() {
						@Override
						public void visitField(FieldMemberNode fmn) {
							if (fmn.opcode() == GETSTATIC && fmn.desc().equals("Ljava/io/File;"))
								Hook.CLIENT.put(new RSField(fmn.fin(), "cacheLocation"));
						}
					});
				});
			}
		}
		if (mn.desc.endsWith("()V")) {
			for (final BasicBlock block : mn.graph()) {
				if (block.count(new MemberQuery(Opcodes.PUTSTATIC, "[S")) == 2 && block.count(new MemberQuery(Opcodes.PUTSTATIC, "[[S")) == 2 &&
						block.count(new MemberQuery(Opcodes.GETSTATIC, "[S")) == 2 && block.count(new MemberQuery(Opcodes.GETSTATIC, "[[S")) == 2) {
					FieldInsnNode fin = (FieldInsnNode) block.get(new MemberQuery(Opcodes.PUTSTATIC, "[S"));
					Hook.CLIENT.put(new RSField(fin, "colorsToFind"));
					fin = (FieldInsnNode) block.get(Opcodes.PUTSTATIC, 3);
					Hook.CLIENT.put(new RSField(fin, "colorsToReplace"));
					fin = (FieldInsnNode) block.get(Opcodes.PUTSTATIC, 4);
					Hook.CLIENT.put(new RSField(fin, "colorsToFind1"));
					fin = (FieldInsnNode) block.get(Opcodes.PUTSTATIC, 5);
					Hook.CLIENT.put(new RSField(fin, "colorsToReplace1"));
				}
			}
		}
	}
	private void visitFields(final FieldNode fn) {
		if (fn.desc.equals(String.format("[[L%s;", Hook.WIDGET.getInternalName()))) {
			Hook.CLIENT.put(new RSField(fn, "interfaces"));
		} else if (fn.desc.equals(String.format("L%s;", Hook.PLAYER.getInternalName()))) {
			Hook.CLIENT.put(new RSField(fn, "myPlayer"));
		} else if (fn.desc.equals(String.format("[[[L%s;", Hook.DEQUE.getInternalName()))) {
			Hook.CLIENT.put(new RSField(fn, "groundItemDeque"));
		} else if (fn.desc.equals(Hook.GAME_ENGINE.getInternalDesc())) {
			Hook.GAME_ENGINE.put(new RSField(fn, "shell"));
		} else if (fn.desc.equals(String.format("L%s;", "java/awt/Canvas"))) {
			Hook.CLIENT.put(new RSField(fn, "canvas"));
		}
	}
	private void visitFields2(final FieldNode fn) {
		if (fn.desc.equals(Hook.CLAN_MATE.getInternalArrayDesc())) {
			Hook.CLIENT.put(new RSField(fn, "clanMates"));
		} else if (fn.desc.equals(Hook.FRIENDED_PLAYER.getInternalArrayDesc())) {
			Hook.CLIENT.put(new RSField(fn, "friendedPlayers"));
		} else if (fn.desc.equals(Hook.IGNORED_PLAYER.getInternalArrayDesc())) {
			Hook.CLIENT.put(new RSField(fn, "ignoredPlayers"));
		} else if (fn.desc.equals(Hook.PLAYER.getInternalArrayDesc())) {
			Hook.CLIENT.put(new RSField(fn, "playerArray"));
		}
	}
	private void findViewportBuffer(final MethodNode mn) {
		if (!mn.desc.endsWith("V") || Type.getArgumentTypes(mn.desc).length > 3)
			return;
		for (final BasicBlock block : mn.graph()) {
			block.tree().accept(new NodeVisitor() {
				@Override
				public void visitMethod(final MethodMemberNode mmn) {
					if (mmn.desc().startsWith("(IIIIII")) {
						final List<AbstractNode> arrayLoadArgs = mmn.layerAll(IALOAD, GETSTATIC);
						if (arrayLoadArgs == null || arrayLoadArgs.size() < 6)
							return;
						for (final AbstractNode an : arrayLoadArgs) {
							final FieldMemberNode fmn = (FieldMemberNode) an;
							if (!fmn.desc().equals("[I"))
								return; // wrong call found
							if (Hook.CLIENT.get("xViewportBuffer") == null) {
								Hook.CLIENT.put(new RSField(fmn, "xViewportBuffer"));
							} else if (Hook.CLIENT.get("yViewportBuffer") == null
									&& !fmn.key().equals(Hook.CLIENT.get("xViewportBuffer").key())) {
								Hook.CLIENT.put(new RSField(fmn, "yViewportBuffer"));
							}
						}
					}
				}
			});
		}
	}
	private void findScreenCenters(final MethodNode mn) {
		if (Hook.CLIENT.get("screenCenterY") != null)
			return;
		final class StoreCalcNode {
			private final VarInsnNode istore;
			private final FieldInsnNode field;
			private StoreCalcNode(final VarInsnNode istore, final FieldInsnNode field) {
				this.istore = istore;
				this.field = field;
			}
		}
		for (final BasicBlock block : mn.graph()) {
			final List<StoreCalcNode> storedSubs = new ArrayList<>();
			block.tree().accept(new NodeVisitor() {
				@Override
				public void visitOperation(ArithmeticNode an) {
					if (an.opcode() == ISUB) {
						final FieldMemberNode fmn = (FieldMemberNode) an.last(GETSTATIC);
						if (fmn != null && an.hasParent() && an.parent().opcode() == ISTORE) {
							storedSubs.add(new StoreCalcNode((VarInsnNode) an.parent().insn(), fmn.fin()));
						}
					}
				}
			});
			if (storedSubs.size() != 2)
				continue;
			// just in case some fucked up shit happens
			Collections.sort(storedSubs, new Comparator<StoreCalcNode>() {
				@Override
				public int compare(StoreCalcNode o1, StoreCalcNode o2) {
					return o1.istore.var - o2.istore.var;
				}
			});
			final StoreCalcNode widthMinusX = storedSubs.get(0);
			Hook.CLIENT.put(new RSField(widthMinusX.field, "screenCenterX"));
			final StoreCalcNode heightMinusY = storedSubs.get(1);
			Hook.CLIENT.put(new RSField(heightMinusY.field, "screenCenterY"));
		}
	}
	private void findKeyFocusListener(final MethodNode methodNode) {
		if (Hook.CLIENT.get("getKeyFocusListener") != null) return;

		String hook_desc = "L" + Hook.KEY_FOCUS_LISTENER.getInternalName() + ";";

		//        if (methodNode.owner.fieldCount("L" + methodNode.owner.name + ";", false) == 1) {
		//            for (int i = 0; i < methodNode.owner.fields.size() - 1; i++) {
		//                FieldNode currField = methodNode.owner.fields.get(i);
		//                if (Modifier.isStatic(currField.access) && Modifier.isPublic(currField.access) && currField.desc.equals("L" + methodNode.owner.name + ";")) {
		//                    Hook.CLIENT.put(new RSField(currField, "getKeyFocusListener"));
		//                }
		//            }
		//        }
	}

	public void getWidgetPositions(MethodNode mn) {
		if (Hook.CLIENT.get("widgetPositionsX") != null && Hook.CLIENT.get("getWidgetBoundsHeight") != null) return;

		if (new Wildcard("()V").matches(mn.desc) && (Modifier.isStatic(mn.access) || Modifier.isProtected(mn.access))) {
			FlowVisitor fv = new FlowVisitor();
			fv.accept(mn);

			for (BasicBlock bl : fv.blocks) {
				int sCount = bl.count(new MemberQuery(Opcodes.GETSTATIC, "[I"));
				int boolCount = bl.count(new MemberQuery(Opcodes.GETSTATIC, "[Z"));
				int loadCount = bl.count(Opcodes.IALOAD);

				if (loadCount >= 4 && sCount > 3 && boolCount == 1) {
					List<AbstractNode> layers = bl.tree().layerAll(Opcodes.INVOKEVIRTUAL, Opcodes.IALOAD, Opcodes.GETSTATIC);
					if (layers != null && layers.size() >= 3) {
						for (int i = 0; i < layers.size(); i++) {
							switch (i) {
							case 0:
								Hook.CLIENT.put(new RSField(((FieldMemberNode) layers.get(i)).fin(), "widgetPositionsX"));
								break;
							case 1:
								Hook.CLIENT.put(new RSField(((FieldMemberNode) layers.get(i)).fin(), "widgetPositionsY"));
								break;
							case 2:
								Hook.CLIENT.put(new RSField(((FieldMemberNode) layers.get(i)).fin(), "getWidgetBoundsWidth"));
								break;
							case 3:
								Hook.CLIENT.put(new RSField(((FieldMemberNode) layers.get(i)).fin(), "getWidgetBoundsHeight"));
								break;
							}
						}
					}
				}
			}
			if (Hook.CLIENT.get("widgetPositionsX") != null && Hook.CLIENT.get("getWidgetBoundsHeight") != null) {
				return;
			}
		}
	}
	private void findCameraVars(final MethodNode mn) {
		if (Hook.CLIENT.get("cameraYaw") != null)
			return;
		// order XZY
		List<AbstractInsnNode> match = Mask.find(mn, Mask.INVOKESTATIC, Mask.GETSTATIC.describe("I"),
				Mask.GETSTATIC.describe("I"), Mask.GETSTATIC.describe("I"), Mask.GETSTATIC.describe("I"),
				Mask.INVOKESTATIC.own("java/lang/Math").describe("(D)D").distance(20));
		if (match != null) {
			final MethodInsnNode min = (MethodInsnNode) match.get(5);
			if (min.name.equals("sqrt")) {
				for (int i = 2; i < 5; i++) {
					final FieldInsnNode cam = (FieldInsnNode) match.get(i);
					Hook.CLIENT.put(new RSField(cam, "camera" + "XZY".charAt(i - 2)));
				}
			}
			// find usage of the cosine/sine tables
			// order pitchyaw
			match = Mask.find(mn, Mask.INVOKESTATIC.describe("(DD)D").own("java/lang/Math"), Mask.SIPUSH.operand(2047),
					Mask.PUTSTATIC.describe("I"), Mask.INVOKESTATIC.describe("(DD)D").own("java/lang/Math"),
					Mask.SIPUSH.operand(2047), Mask.PUTSTATIC.describe("I"));
			if (match != null) {
				Hook.CLIENT.put(new RSField((FieldInsnNode) match.get(2), "cameraPitch"));
				Hook.CLIENT.put(new RSField((FieldInsnNode) match.get(5), "cameraYaw"));
			}
		}
	}
	private class ScreenVisitor extends NodeVisitor {
		@Override
		public void visitNumber(NumberNode nn) {
			if (nn.number() == 85504) {
				FieldMemberNode set = (FieldMemberNode) nn.preLayer(IDIV, ISHL, IMUL, PUTSTATIC);
				if (set != null)
					Hook.CLIENT.put(new RSField(set, "screenZoom"));
			}
		}
		@Override
		public void visitField(FieldMemberNode fmn) {
			if (fmn.opcode() == PUTSTATIC) {
				List<AbstractNode> divs = fmn.layerAll(IMUL, IADD, IDIV);
				if (divs == null || divs.size() != 2)
					return;
				for (AbstractNode idiv : divs) {
					FieldMemberNode val = (FieldMemberNode) idiv.layer(IMUL, GETSTATIC);
					if (val == null)
						continue;
					if (Hook.CLIENT.get("screenWidth") == null) {
						Hook.CLIENT.put(new RSField(val, "screenWidth"));
					} else if (Hook.CLIENT.get("screenHeight") == null) {
						Hook.CLIENT.put(new RSField(val, "screenHeight"));
					}
				}
			}
		}
	}
}
