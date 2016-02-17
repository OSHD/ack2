package org.objectweb.asm.commons.cfg.query;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Project: DankWise
 * Date: 23-02-2015
 * Time: 14:23
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class TypeQuery extends InsnQuery {

    private int opcode;
    private String desc;

    public TypeQuery(final int opcode, final String desc) {
        super(opcode);
        this.desc = desc;
    }

    public TypeQuery(final int opcode) {
        this(opcode, null);
    }

    public TypeQuery(final String desc) {
        this(-1, desc);
    }

    public TypeQuery() {
        this(-1, null);
    }

    public TypeQuery opcode(final int opcode) {
        this.opcode = opcode;
        return this;
    }

    public TypeQuery desc(final String desc) {
        this.desc = desc;
        return this;
    }

    @Override
    public boolean matches(AbstractInsnNode ain) {
        if (ain instanceof TypeInsnNode) {
            TypeInsnNode type = (TypeInsnNode) ain;
            if ((this.opcode == -1 || this.opcode == type.opcode())) {
                if ((this.desc == null || this.desc.equals(type.desc))) {
                    return true;
                }
            }
        }
        return false;
    }
}
