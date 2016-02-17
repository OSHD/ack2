package com.dank.analysis.impl.misc;


import com.dank.analysis.Analyser;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.NumberQuery;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.tree.*;

/**
 *
 * @author Kyle Friz
 * @since  Oct 25, 2015
 */
public class GPI extends Analyser {

	@Override
	public ClassSpec specify(ClassNode cn) {
		return cn.fieldCount(Hook.BUFFER.getInternalDesc(), false) == 1
				&& cn.fieldCount(Hook.BUFFER.getInternalArrayDesc(), false) == 1 ? new ClassSpec(Hook.GPI, cn) : null;
	}

	@Override
	public void evaluate(ClassNode cn) {
		for (final FieldNode fn : cn.fields) {
			if (fn.desc.equals(Hook.BUFFER.getInternalDesc())) {
				Hook.GPI.put(new RSField(fn, "chatBuffer"));
			} else if (fn.desc.equals(Hook.BUFFER.getInternalArrayDesc())) {
				Hook.GPI.put(new RSField(fn, "cachedAppearances"));
			}
		}

		for (ClassNode c : getClassPath().getClasses()) {
			for (MethodNode mn : c.methods) {
				mn.graph().forEach((BasicBlock b) -> {
					SkipJumpVisitor skip = new SkipJumpVisitor(b);
					b.tree().accept(skip);
					if (skip.mn != null) {
						mn.graph().forEach((BasicBlock bb) -> {
							bb.tree().accept(new ArrayJumpVisitor(bb));
						});
					}
					if (mn.name.equals("l"))
						b.tree().accept(new FlagsJumpVisitor(b));
				});
				if(mn.name.contains("init")) {
					mn.graph().forEach((BasicBlock b) -> {
						ArrayVisitor av = new ArrayVisitor(b);
						SingleInstanceVisitor siv = new SingleInstanceVisitor(b);

						b.tree().accept(av);
						b.tree().accept(siv);
					});
				}
			}
		}


		if(Hook.GPI.get("skipFlags")==null) return;
		for (final FieldNode fn : cn.fields) {
			if (fn.desc.equals("[B") && !Hook.GPI.get("skipFlags").name.equals(fn.name)) {
				Hook.GPI.put(new RSField(fn, "movementTypes"));
			}
		}
	}

	class SkipJumpVisitor extends TreeVisitor {

		MethodNode mn;

		public SkipJumpVisitor(BasicBlock block) {
			super(block);
		}

		@Override
		public boolean validateBlock(BasicBlock b) {
			return b.count(GETSTATIC) == 1 && b.count(BALOAD) == 1 && b.count(IAND) == 1;
		}

		@Override
		public void visitJump(JumpNode jn) {
			if (jn.opcode() == IFNE) {
				FieldInsnNode fin = (FieldInsnNode) block.get(GETSTATIC);
				if (fin.owner.equals(Hook.GPI.getInternalName())) {
					Hook.GPI.put(new RSField(fin, "skipFlags"));
					mn = jn.method();
				}
			}
		}
	}

	class ArrayVisitor extends TreeVisitor {

		public ArrayVisitor(BasicBlock block) {
			super(block);
		}

		@Override
		public boolean validateBlock(BasicBlock block) {
			return block.count(PUTSTATIC) == 1 && block.count(NEWARRAY) == 1 && block.count(new NumberQuery(SIPUSH, 2048)) == 1;
		}

		@Override
		public void visitField(FieldMemberNode fmn) {
			if(fmn.opcode() == PUTSTATIC) {
				BasicBlock local = block.next;

				if(fmn.desc().equals("[I") && Hook.GPI.get("localPlayerIndices") == null) {
					Hook.GPI.put(new RSField(fmn.fin(), "localPlayerIndices"));
				}
				else if(fmn.desc().equals("[I") && Hook.GPI.get("globalPlayerIndices") == null) {
					Hook.GPI.put(new RSField(fmn.fin(), "globalPlayerIndices"));
				}
				else if(fmn.desc().equals("[I") && Hook.GPI.get("pendingFlagsIndices") == null) {
					Hook.GPI.put(new RSField(fmn.fin(), "pendingFlagsIndices"));
				}
			}
		}
	}

	class SingleInstanceVisitor extends TreeVisitor {

		public SingleInstanceVisitor(BasicBlock block) {
			super(block);
		}

		@Override
		public boolean validateBlock(BasicBlock block) {
			return block.count(ICONST_0) == 1;
		}

		@Override
		public void visitField(FieldMemberNode fmn) {
			if(fmn.opcode() == PUTSTATIC) {
				BasicBlock local = block.next;

				if(fmn.desc().equals("I") && Hook.GPI.get("localPlayerCount") == null) {
					Hook.GPI.put(new RSField(fmn.fin(), "localPlayerCount"));
				}
				else if(fmn.desc().equals("I") && Hook.GPI.get("globalPlayerCount") == null) {
					Hook.GPI.put(new RSField(fmn.fin(), "globalPlayerCount"));
				}
				else if(fmn.desc().equals("I") && Hook.GPI.get("pendingFlagsCount") == null) {
					Hook.GPI.put(new RSField(fmn.fin(), "pendingFlagsCount"));
				}
			}
		}
	}


	class ArrayJumpVisitor extends TreeVisitor {

		public ArrayJumpVisitor(BasicBlock block) {
			super(block);
		}

		@Override
		public boolean validateBlock(BasicBlock b) {
			return b.count(ALOAD) == 1;
		}

		@Override
		public void visitJump(JumpNode jn) {
			if (jn.opcode() == IF_ACMPEQ) {
				BasicBlock local = block.next;

				for (AbstractInsnNode n : local.instructions) {
					if (n instanceof FieldInsnNode) {
						FieldInsnNode fin = (FieldInsnNode) n;
						if (fin.desc.equals("[I"))
							Hook.GPI.put(new RSField(fin, "localPlayerIndices"));
						else if (fin.desc.equals("I"))
							Hook.GPI.put(new RSField(fin, "localPlayerCount"));
					}
				}

				BasicBlock global = block.target;

				for (AbstractInsnNode n : global.instructions) {
					if (n instanceof FieldInsnNode) {
						FieldInsnNode fin = (FieldInsnNode) n;
						if (fin.desc.equals("[I"))
							Hook.GPI.put(new RSField(fin, "globalPlayerIndices"));
						else if (fin.desc.equals("I"))
							Hook.GPI.put(new RSField(fin, "globalPlayerCount"));

					}
				}
			}
		}
	}

	class FlagsJumpVisitor extends TreeVisitor {

		public FlagsJumpVisitor(BasicBlock block) {
			super(block);
		}

		@Override
		public boolean validateBlock(BasicBlock b) {
			return b.count(GETSTATIC) == 1 && b.count(IMUL) == 1 && b.count(ILOAD) == 1;
		}

		@Override
		public void visitJump(JumpNode jn) {
			if (jn.opcode() == IF_ICMPGE) {
				FieldInsnNode fin = (FieldInsnNode) block.get(GETSTATIC);
				if (fin.owner.equals(Hook.GPI.getInternalName())) {
					jn.method().graph().forEach((BasicBlock b) -> {
						FlagsArrayVisitor visit = new FlagsArrayVisitor(b);
						b.tree().accept(visit);

						if (visit.matches)
							Hook.GPI.put(new RSField(fin, "pendingFlagsCount"));
					});

				}
			}
		}
	}

	class FlagsArrayVisitor extends TreeVisitor {

		boolean matches = false;

		public FlagsArrayVisitor(BasicBlock block) {
			super(block);
		}

		@Override
		public boolean validateBlock(BasicBlock b) {
			return b.count(GETSTATIC) == 1 && b.count(ILOAD) == 1 && b.count(IALOAD) == 1;
		}

		@Override
		public void visitField(FieldMemberNode fmn) {
				if (fmn.owner().equals(Hook.GPI.getInternalName()) && fmn.desc().equals("[I")) {
					Hook.GPI.put(new RSField(fmn, "pendingFlagsIndices"));
					matches = true;
				}
		}
	}
}
