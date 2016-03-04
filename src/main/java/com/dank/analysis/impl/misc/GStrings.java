package com.dank.analysis.impl.misc;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ConstantNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;

/**
 * Project: DankWise
 * Time: 17:23
 * Date: 15-02-2015
 * Created by Dogerina.
 */
public class GStrings extends Analyser {

    private static final Map<String, String> keys = new HashMap<>();

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(String.class, false) > 200 ? new ClassSpec(Hook.GAME_STRINGS, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        final MethodNode mn = cn.getStaticConstructor();
        mn.graph().forEach(b -> b.tree().accept(new NodeVisitor() {
            @Override
            public void visitField(FieldMemberNode fmn) {
                if (fmn.opcode() == PUTSTATIC && fmn.desc().equals("Ljava/lang/String;") && fmn.children() == 1) {
                    final ConstantNode cn = fmn.firstConstant();
                    if (cn != null) {
                        keys.put(fmn.key(), (String) cn.cst());
                    }
                }
            }
        }));
    }

    /**
     * @param key owner.name
     * @param check the string to check against
     * @return true if it matches, otherwise false
     */
    public static boolean compare(final String key, final String check) {
        final String val = keys.get(key);
        return val != null && val.equals(check);
    }

    public static boolean compareIgnoreCase(final String key, final String check) {
        final String val = keys.get(key);
        return val != null && val.equalsIgnoreCase(check);
    }

    /**
     * @param key
     * @return The string that the field was assigned to if valid, otherwise null
     */
    public static String get(final String key) {
        return keys.get(key);
    }
}
