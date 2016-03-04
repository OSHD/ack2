package org.objectweb.asm.commons.cfg;

import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import com.dank.asm.ClassPath;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 17:15
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class OpaquePredicateVisitor {

    public OpaquePredicateVisitor(final ClassPath cp) {
        final AtomicInteger del = new AtomicInteger(0);
        cp.forEach((name, cn) -> cn.methods.forEach(m -> m.graph().forEach(b -> {
            for (final AbstractInsnNode ain : b.instructions) {
                if (ain.opcode() == INVOKESPECIAL) {
                    final MethodInsnNode min = (MethodInsnNode) ain;
                    if (!min.owner.contains("IllegalStateException") || min.next() == null) continue;
                    if (min.next().opcode() == ATHROW && min.previous().opcode() == DUP && min.previous().previous().opcode() == NEW) {
                        b.instructions.clear();
                        del.incrementAndGet();
                    }
                }
            }
        })));
        System.out.println("Deleted " + del.get() + " Opaque Predicates!");
    }
}
