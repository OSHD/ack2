package com.dank.analysis.impl.character.player.config;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Project: DankWise
 * Date: 27-02-2015
 * Time: 18:20
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class PlayerConfig extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(int[].class) == 2 && cn.fieldCount(long.class) == 2 && cn.ownerless()
                && cn.fieldCount(boolean.class) == 1 && cn.fieldCount() == 6
                ? new ClassSpec(Hook.PLAYER_CONFIG, cn) : null;
    }
//103
//    ✓ colorsToFind..................fz.u
//    ✕ colorsToFind1.................ft.e
//    ✓ colorsToReplace...............fz.a
//    ✕ colorsToReplace1..............fl.b
// 102
//    ✓ colorsToFind..................bh.m [S
//    ✓ colorsToFind1.................ap.u [S
//    ✓ colorsToReplace...............fl.p [[S
//    ✓ colorsToReplace1..............r.c  [[S

    @Override
    public void evaluate(ClassNode cn) {
        for (final MethodNode mn : cn.methods) {
//            if (mn.desc.endsWith("()V")) {
//                for (final BasicBlock block : mn.graph()) {
//                    if (block.count(new MemberQuery(Opcodes.PUTSTATIC, "[S")) == 2 && block.count(new MemberQuery(Opcodes.PUTSTATIC, "[[S")) == 2 &&
//                            block.count(new MemberQuery(Opcodes.GETSTATIC, "[S")) == 2 && block.count(new MemberQuery(Opcodes.GETSTATIC, "[[S")) == 2) {
//                            System.out.println(cn.name + "." + mn.name);
//                            FieldInsnNode fin = (FieldInsnNode) block.get(new MemberQuery(Opcodes.PUTSTATIC, "[S"));
//                            Hook.CLIENT.put(new RSField(fin, "colorsToFind"));
//
//                            fin = (FieldInsnNode) block.get(Opcodes.PUTSTATIC, 1);
//                            Hook.CLIENT.put(new RSField(fin, "colorsToReplace"));
//
//                            fin = (FieldInsnNode) block.get(Opcodes.PUTSTATIC, 2);
//                            Hook.CLIENT.put(new RSField(fin, "colorsToFind1"));
//
//                            fin = (FieldInsnNode) block.get(Opcodes.PUTSTATIC, 3);
//                            Hook.CLIENT.put(new RSField(fin, "colorsToReplace1"));
//
//                    }
//                }
//            }
            if (!mn.isStatic() && !mn.desc.endsWith("V")) {
                for (final BasicBlock block : mn.graph()) {
                    if (block.count(Opcodes.ILOAD) == 3 && block.count(FieldInsnNode.class) == 3) {
                        final FieldInsnNode fin = (FieldInsnNode) block.get(new MemberQuery(Opcodes.GETFIELD, "[I"));
                        if (fin == null) continue;
                        Hook.PLAYER_CONFIG.put(new RSField(fin, "appearanceColors"));
                    }
                }
            }
        }
        for (final FieldNode fn : cn.fields) {
            if (!fn.isStatic()) {
                if (fn.desc.equals("Z")) {
                    Hook.PLAYER_CONFIG.put(new RSField(fn, "female"));
                } else if (fn.desc.equals("I")) {
                    Hook.PLAYER_CONFIG.put(new RSField(fn, "npcId"));
                } else if (fn.desc.equals("[I") && !fn.name.equals(Hook.PLAYER_CONFIG.get("appearanceColors").name)) {
                    Hook.PLAYER_CONFIG.put(new RSField(fn, "appearance"));
                }
            }
        }
    }
}
