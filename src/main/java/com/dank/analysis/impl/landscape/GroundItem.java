package com.dank.analysis.impl.landscape;

import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Date: 27-02-2015
 * Time: 18:54
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class GroundItem extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount() == 2 && cn.fieldCount(int.class) == 2 && cn.superName(Hook.ENTITY.getInternalName())
                ? new ClassSpec(Hook.GROUND_ITEM, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final MethodNode mn : cn.methods) {
            if (Modifier.isStatic(mn.access) || !Modifier.isProtected(mn.access) || !mn.desc.startsWith("()L")) {
                continue;
            }
            for (final BasicBlock block : mn.graph()) {
                final MemberQuery query = new MemberQuery(Opcodes.GETFIELD, cn.name, "I");
                FieldInsnNode fin = (FieldInsnNode) block.get(query);
                if (fin == null) continue;
                Hook.GROUND_ITEM.put(new RSField(fin, "id"));
                fin = (FieldInsnNode) block.getLast(query);
                if (fin == null) continue;
                Hook.GROUND_ITEM.put(new RSField(fin, "quantity"));
            }
        }
    }
}
