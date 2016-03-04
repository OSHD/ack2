package org.objectweb.asm.commons.cfg;

import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.asm.Opcodes;

/**
 * @author Tyler Sedlar
 */
public abstract class BlockVisitor implements Opcodes {

    public AtomicBoolean lock = new AtomicBoolean(false);

    public abstract boolean validate();

    public abstract void visit(BasicBlock block);

    public void visitEnd() {}
}
