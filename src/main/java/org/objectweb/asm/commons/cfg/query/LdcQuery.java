package org.objectweb.asm.commons.cfg.query;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Created by Greg on 12/25/2015.
 */
public class LdcQuery extends InsnQuery {

    private int opcode;
    private String data;

    public LdcQuery(int opcode, String data) {
        super(opcode);
        this.opcode = opcode;
        this.data = data;
    }

    @Override
    public boolean matches(AbstractInsnNode ain) {
        if (!(ain instanceof IntInsnNode) && !(ain instanceof LdcInsnNode) && !(ain instanceof VarInsnNode))
            return false;
        if (ain instanceof LdcInsnNode) {
            return ain.toString().contains(data);
        }
        return false;
    }

}
