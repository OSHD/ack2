package com.dank.analysis.impl.misc;

import com.dank.DankEngine;
import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;

/**
 * Project: DankWise
 * Date: 17-02-2015
 * Time: 03:42
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class PacketBuffer extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName(Hook.BUFFER.getInternalName()) && cn.fieldCount() > 1
                ? new ClassSpec(Hook.PACKET_BUFFER, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        FieldNode random = null;
        for (FieldNode fn : cn.fields) {
            if (fn.desc.equals("I") && !Modifier.isStatic(fn.access)) {
                Hook.PACKET_BUFFER.put(new RSField(fn, "bitCaret"));
            } else if (fn.desc.equals("[I") && Modifier.isStatic(fn.access)) {
                Hook.PACKET_BUFFER.put(new RSField(fn, "bitMasks"));
                MethodNode method = DankEngine.fGraph.getCaller(fn, 3);
                if (method != null)
                    Hook.PACKET_BUFFER.put(new RSMethod(method, "readBits"));
            } else if (!fn.desc.equals("I") && !Modifier.isStatic(fn.access)) {
                random = fn;
                Hook.PACKET_BUFFER.put(new RSField(fn, "random"));
                break;
            }
        }

        for (MethodNode mn : cn.methods) {
            if (mn.desc.endsWith("V") && mn.desc.startsWith("(I") && !Modifier.isStatic(mn.access)) {
                for (BasicBlock block : mn.graph()) {
                    for (AbstractInsnNode ain : block.instructions) {
                        if (ain.opcode() != Opcodes.GETFIELD) {
                            continue;
                        }
                        FieldInsnNode fin = (FieldInsnNode) ain;
                        if (fin.owner.equals(cn.name) && fin.name.equals(random.name) && fin.desc.equals(random.desc)) {
                            Hook.PACKET_BUFFER.put(new RSMethod(mn, "writeHeader"));
                        }
                    }
                }
            } else if (mn.desc.endsWith("I")) {
                for (BasicBlock block : mn.graph()) {
                    for (AbstractInsnNode ain : block.instructions) {
                        if (ain.opcode() != Opcodes.GETFIELD) {
                            continue;
                        }
                        FieldInsnNode fin = (FieldInsnNode) ain;
                        if (fin.owner.equals(cn.name) && fin.name.equals(random.name) && fin.desc.equals(random.desc)) {
                            Hook.PACKET_BUFFER.put(new RSMethod(mn, "readHeader"));

                            MethodNode frames = DankEngine.mGraph.getCaller(mn, 3);
                            if (frames != null) Hook.CLIENT.put(new RSMethod(frames, "processFrames"));

                            MethodNode login = DankEngine.mGraph.getCaller(mn, 5);
                            if (login != null) Hook.CLIENT.put(new RSMethod(login, "processLogin"));

                            //MethodNode logic = DankEngine.mGraph.getCaller(login);
                           // if (logic != null) Hook.CLIENT.put(new RSMethod(logic, "processLogic"));
                        }
                    }
                }
            }
        }
    }
}
