package com.dank.analysis.impl.client.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.asm.RIS;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 22:28
 * Date: 13-02-2015
 * Created by Dogerina.
 */
public class W2MVisitor extends TreeVisitor {

    private static final Pattern chunk0 = RIS.mkPattern(INVOKESTATIC, LDC, DMUL, D2I, BIPUSH, ISUB, LDC, IMUL, PUTSTATIC);
    private static final Pattern chunk1 = RIS.mkPattern(LDC, INVOKESTATIC, LDC, DMUL, D2I, BIPUSH, ISUB, IMUL, PUTSTATIC);
    private static final Pattern chunk2 = RIS.mkPattern(INVOKESTATIC, LDC, DMUL, D2I, LDC, ISUB, LDC, IMUL, PUTSTATIC);

    public W2MVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        //cbf to convert to proper visitor usage
        final RIS chunk_searcher = new RIS(block);
        final List<AbstractInsnNode[]> chunk_matches = new ArrayList<>();
        chunk_matches.addAll(chunk_searcher.search(chunk0));
        chunk_matches.addAll(chunk_searcher.search(chunk1));
        chunk_matches.addAll(chunk_searcher.search(chunk2));
        if (chunk_matches.size() == 0) return false;
        for (AbstractInsnNode[] chunk_match : chunk_matches) {
            final boolean p1 = chunk_match[0].opcode() == LDC;
            FieldInsnNode fin = (FieldInsnNode) chunk_match[8];
            final int random_multi = (byte) (double) ((LdcInsnNode) chunk_match[p1 ? 0 : 1]).cst;
            int sub = -1;
            AbstractInsnNode ain = chunk_match[p1 ? 5 : 4];
            if (ain instanceof IntInsnNode) sub = ((IntInsnNode) ain).operand;
            else if (ain instanceof LdcInsnNode) sub = (int) ((LdcInsnNode) ain).cst;

            //    final int sub = (byte) ((IntInsnNode) chunk_match[p1 ? 5 : 4]).operand;
            if (random_multi == 30 && sub == 20) {
                Hook.CLIENT.put(new RSField(fin, "viewRotation"));
            } else if (random_multi == 120 && sub == 60) {
                Hook.CLIENT.put(new RSField(fin, "minimapScale"));
            }
        }
        return true;
    }
}
