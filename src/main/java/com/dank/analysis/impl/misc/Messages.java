package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.BlockVisitor;
import org.objectweb.asm.commons.cfg.FlowVisitor;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by RSynapse on 2/20/2016.
 */
public class Messages extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return (cn.fieldCount(String.class, true) == 3 && cn.fieldCount(int.class, true) == 3) ?
                new ClassSpec(Hook.MESSAGES, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (MethodNode methodNode : cn.methods) {
            if (!Modifier.isStatic(methodNode.access) && methodNode.desc.contains("Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;") &&
                    Hook.MESSAGES.get("index") == null) {
                Hook.MESSAGES.put(new RSField(load(methodNode, Opcodes.ILOAD, 1), "type"));
                Hook.MESSAGES.put(new RSField(load(methodNode, Opcodes.ALOAD, 2), "sender"));
                Hook.MESSAGES.put(new RSField(load(methodNode, Opcodes.ALOAD, 3), "channel"));
                Hook.MESSAGES.put(new RSField(load(methodNode, Opcodes.ALOAD, 4), "message"));
                hookCycle(methodNode);
                hookIndex(methodNode);
            }
        }
    }

    /**
     * Hooks the last unknown paramter.  Index is the only hook where it could be an issue to hook
     * due to not having an iload index we can base the search && it is missing a getstatic
     * like the hookCycle method
     *
     * @param methodNode
     */
    public void hookIndex(MethodNode methodNode) {
        Map<String, String> hooks = new HashMap<String, String>();

        Hook.MESSAGES.getIdentifiedSet().stream().filter(test -> test.isField()).forEach(test -> {
            hooks.put(test.owner, test.name);
        });

        for (AbstractInsnNode ain : methodNode.instructions) {
            if (ain instanceof FieldInsnNode) {
                FieldInsnNode current = (FieldInsnNode) ain;
                if (current.opcode() == Opcodes.PUTFIELD) {
                    if (!hooks.containsValue(current.name))
                        Hook.MESSAGES.put(new RSField(current, "index"));
                }
            }
        }
    }


    public void hookCycle(MethodNode methodNode) {

        Map<String, String> hooks = new HashMap<String, String>();

        Hook.MESSAGES.getIdentifiedSet().stream().filter(test -> test.isField()).forEach(test -> {
            hooks.put(test.owner, test.name);
        });

        FlowVisitor fv = new FlowVisitor();
        fv.accept(methodNode);

        for (BasicBlock block : fv.blocks) {
            block.accept(new BlockVisitor() {
                @Override
                public boolean validate() {
                    return block.count(Opcodes.GETSTATIC) == 1 && block.count(Opcodes.PUTFIELD) >= 1;

                }

                @Override
                public void visit(BasicBlock block) {
                    FieldInsnNode fin = (FieldInsnNode) block.get(Opcodes.PUTFIELD);
                    if (!hooks.containsValue(fin.name))
                        Hook.MESSAGES.put(new RSField(fin, "cycle"));
                }
            });
        }

    }

//    public Map<String>

    private FieldInsnNode load(final MethodNode mn, final int opcode, final int index) {
        for (final AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof VarInsnNode) {
                final VarInsnNode vin = (VarInsnNode) ain;
                if (vin.var == index && vin.opcode() == opcode) {
                    AbstractInsnNode dog = vin;
                    for (int i = 0; i < 7; i++) {
                        if (dog == null) break;
                        if (dog.opcode() == Opcodes.PUTFIELD && ((FieldInsnNode) dog).owner.equals(Hook.MESSAGES.getInternalName())) {
                            return (FieldInsnNode) dog;
                        }
                        dog = dog.next();
                    }
                }
            }
        }
        return null;
    }

}
