package com.dank.analysis.impl.client.visitor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.asm.RIS;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 00:01
 * Date: 14-02-2015
 * Created by Dogerina.
 */
public class W2M2Visitor extends TreeVisitor {

    private static final Pattern angle_pattern0 = RIS.mkPattern(INVOKESTATIC, LDC, DMUL, D2I, BIPUSH, ISUB, SIPUSH, IAND, LDC, IMUL, PUTSTATIC);
    private static final Pattern angle_pattern1 = RIS.mkPattern(LDC, INVOKESTATIC, LDC, DMUL, D2I, BIPUSH, ISUB, SIPUSH, IAND, IMUL, PUTSTATIC);

    public W2M2Visitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        if (!Modifier.isStatic(block.owner.access) || !block.owner.desc.endsWith("V")) return false;
        RIS angle_searcher = new RIS(block);
        List<AbstractInsnNode[]> matches = new ArrayList<>();
        matches.addAll(angle_searcher.search(angle_pattern0));
        matches.addAll(angle_searcher.search(angle_pattern1));
        if (matches.size() == 0) return false;
        for (AbstractInsnNode[] match : matches) {
            AbstractInsnNode invoke0 = match[0].opcode() == LDC ? match[1] : match[0];
            MethodInsnNode invoke = (MethodInsnNode) invoke0;
            if (!invoke.owner.equals("java/lang/Math")) continue;
            if (!invoke.name.equals("random")) continue;
            if (!invoke.desc.equals("()D")) continue;
            FieldInsnNode angle_fin = (FieldInsnNode) match[10];
            Hook.CLIENT.put(new RSField(angle_fin, "minimapRotation"));
            return true;
        }
        return true;
    }
}
