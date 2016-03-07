package com.dank.analysis.impl.widget.visitor;

import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.HashSet;
import java.util.Set;

/**
 * Project: DankWise
 * Time: 17:20
 * Date: 13-02-2015
 * Created by Dogerina.
 */
public class RuneScriptVisitor extends NodeVisitor implements Opcodes {

    //should've used Map<Integer, OpcodeHook> instead, but cbf now
    private final Set<OpcodeHook> hooks = new HashSet<OpcodeHook>() {
        {
            add(new OpcodeHook(40, GETFIELD, "stringStackCount").skip(10));//good

        }
    };

    private static FieldInsnNode next(AbstractInsnNode from, final int op, final String desc, final String owner, final int skips) {
        int skipped = 0;
        while ((from = from.next()) != null) {
            if (from.opcode() == op) {
                final FieldInsnNode topkek = (FieldInsnNode) from;
                if (topkek.desc.equals(desc) && (owner == null || owner.equals(topkek.owner))) {
                    System.out.println("Skips : (" + skipped + ")" + topkek.owner + "." + topkek.name + "(" + topkek.opname() + ")");
                    if (skipped == skips) {
                        return topkek;
                    } else {
                        skipped++;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void visitNumber(final NumberNode nn) {
        for (final OpcodeHook hook : hooks) {
            if (hook.number == nn.number()) {
                final String owner = hook.fieldOpcode == GETSTATIC || hook.fieldOpcode == PUTSTATIC ? null : hook.container.getInternalName();
                final FieldInsnNode fin = next(nn.insn(), hook.fieldOpcode, hook.fieldDesc,
                        owner, hook.skips);

                System.out.println(">" + nn.number());
                if (fin != null && fin.owner.equals(Hook.RUNESCRIPT.getInternalName())) {
                    hook.container.put(new RSField(fin, hook.mnemonic));
                }
            }
        }
    }

    private final class OpcodeHook {

        private final int number;
        private final int fieldOpcode;
        private final String fieldDesc;
        private final String mnemonic;
        private Hook container = null;
        private int skips = 0;

        /**
         * @param number      the operand of the value
         * @param fieldOpcode the opcode of the field
         * @param fieldDesc   the desc of the field
         * @param container   the target container - where to store the hook
         * @param mnemonic    the defined hook name
         */
        private OpcodeHook(final int number, final int fieldOpcode, final String fieldDesc, final Hook container, final String mnemonic) {
            this.number = number;
            this.fieldOpcode = fieldOpcode;
            this.fieldDesc = fieldDesc;
            this.container = container;
            this.mnemonic = mnemonic;
        }

        //convenience below
        private OpcodeHook(final int number, final int fieldOpcode, final Hook container, final String mnemonic) {
            this(number, fieldOpcode, "I", container, mnemonic);
        }

        private OpcodeHook(final int number, final int fieldOpcode, final String fieldDesc, final String mnemonic) {
            this(number, fieldOpcode, fieldDesc, Hook.WIDGET, mnemonic);
        }

        private OpcodeHook(final int number, final int fieldOpcode, final String mnemonic) {
            this(number, fieldOpcode, "I", Hook.WIDGET, mnemonic);
        }

        /**
         * Set a number of fields to skip when identifying this OpcodeHook
         *
         * @param skips
         * @return this instance
         */
        private OpcodeHook skip(final int skips) {
            this.skips = skips;
            return this;
        }

        private OpcodeHook container(Hook target) {
            this.container = target;
            return this;
        }
    }
}
