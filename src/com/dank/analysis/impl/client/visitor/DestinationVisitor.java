package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.JumpNode;
import org.objectweb.asm.tree.FieldInsnNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * 
 * @author Kyle Friz
 * @since  Dec 6, 2015
 */
public class DestinationVisitor extends TreeVisitor {

	public DestinationVisitor(BasicBlock block) {
		super(block);
	}

	@Override
	public boolean validateBlock(BasicBlock b) {
		return b.count(GETSTATIC) == 1 && b.count(PUTSTATIC) == 1 && b.count(ILOAD) == 1 && b.count(IMUL) == 1;
	}

	@Override
	public void visitOperation(ArithmeticNode an) {
		if (an.isInt() && an.subtracting()) {
			FieldMemberNode field = an.firstField();
			an.method().graph().forEach((BasicBlock block) -> {
				DestinationJumpVisitor visit = new DestinationJumpVisitor(field, block);
				block.tree().accept(visit);
				if (!visit.matches && visit.jump) {
					Hook.CLIENT.put(new RSField(field, "destinationY"));
				}
			});
		}
	}

	class DestinationJumpVisitor extends TreeVisitor {

		FieldMemberNode field;
		boolean jump = false;
		boolean matches = false;

		public DestinationJumpVisitor(FieldMemberNode f, BasicBlock block) {
			super(block);
			this.field = f;
		}

		@Override
		public boolean validateBlock(BasicBlock b) {
			return b.count(GETSTATIC) == 1 && b.count(IMUL) == 1;
		}

		@Override
		public void visitJump(JumpNode jn) {
			if (jn.opcode() == IFEQ) {
				jump = true;

				FieldInsnNode fin = (FieldInsnNode) block.get(GETSTATIC);
				if (field.owner().equals(fin.owner) && field.name().equals(fin.name)) {
					Hook.CLIENT.put(new RSField(field, "destinationX"));
					matches = true;
				}
			}
		}

	}
}
