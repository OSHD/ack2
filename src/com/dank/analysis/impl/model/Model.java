package com.dank.analysis.impl.model;

import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.query.NumberQuery;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.model.visitor.CursorUidsVisitor;
import com.dank.analysis.impl.model.visitor.VertexVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Filter;

public class Model extends Analyser {

    public static final Filter<MethodNode> METHOD_RENDER_AT_FILTER = mn -> {
        if (Modifier.isStatic(mn.access) || !mn.desc.endsWith("V") || !mn.desc.startsWith("(IIIIIIIII")) return false;
        final ClassNode r = Hook.ENTITY.resolve();
        if (mn.owner.name.equals(r.name)) return true;
        for (MethodNode rmn : r.methods) {
            if (rmn.name.equals(mn.name) && rmn.desc.equals(mn.desc)) {
                return true;
            }
        }
        return false;
    };

    @Override
    public ClassSpec specify(ClassNode node) {
        final String renderable0 = Hook.ENTITY.getInternalName();
        if (!node.superName.equals(renderable0) || Modifier.isAbstract(node.access)) return null;
        return node.fieldCount("[I") != 12 ? null : new ClassSpec(Hook.MODEL, node);

    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final MethodNode mn : cn.methods) {
            if (METHOD_RENDER_AT_FILTER.accepts(mn)) {
                if (mn.graph().complexity(25, 40)) {
                    mn.graph().forEach(b -> b.tree().accept(new VertexVisitor(b)));
                }
                findXYZMag(cn.name, mn);
                findAllowClickBounds(cn.name, mn);
                mn.graph().forEach(b -> b.tree().accept(new CursorUidsVisitor(b)));
            }
        }
    }

    private void findAllowClickBounds(final String owner, final MethodNode mn) {
        if (Hook.MODEL.get("allowClickBounds") != null) return;
        for (final BasicBlock block : mn.graph()) {
            if (block.size() >= 5 || block.last().opcode() != Opcodes.IFEQ || block.last().previous().opcode() != Opcodes.GETFIELD) {
                continue;
            }
            final FieldInsnNode fin = (FieldInsnNode) block.last().previous();
            if (!fin.desc.equals("Z")) continue;
            final BasicBlock hovered = block.next.instructions.size() > 5 ? block.next : block.target;
            if (hovered.count(Opcodes.GETSTATIC) != 2 || hovered.count(Opcodes.IADD) != 1 || hovered.count(Opcodes.PUTSTATIC) != 1) {
                continue;
            }
            Hook.MODEL.put(new RSField(fin, "allowClickBounds"));
        }
    }

    private void findXYZMag(final String owner, final MethodNode mn) {
        if (Hook.MODEL.get("XYZMag") != null) return;
        for (final BasicBlock block : mn.graph()) {
            if (block.count(Opcodes.GETFIELD) == 0 || block.count(Opcodes.IMUL) == 0
                    || block.count(Opcodes.ISHR) == 0 || block.get(new NumberQuery(Opcodes.BIPUSH, 16)) == null) {
                continue;
            }
            final FieldInsnNode fin = (FieldInsnNode) block.get(new MemberQuery(Opcodes.GETFIELD, owner, "I"));
            if (fin != null && fin.desc.equals("I") && !fin.name.equals(Hook.ENTITY.get("modelHeight").name)) {
                Hook.MODEL.put(new RSField(fin, "XYZMag"));
            }
        }
    }
}
