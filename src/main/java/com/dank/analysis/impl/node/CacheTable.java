package com.dank.analysis.impl.node;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMember;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;

/**
 * @author Septron
 * @since February 12, 2015
 */
public class CacheTable extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.ownerless() &&
                cn.fieldCount(String.format("L%s;", Hook.DUAL_NODE.getInternalName())) == 1 &&
                cn.fieldCount(String.format("L%s;", Hook.HASHTABLE.getInternalName())) == 1 &&
                cn.fieldCount(int.class) == 2 ?
                new ClassSpec(Hook.CACHE_TABLE, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {

        cn.methods.stream().filter(methodNode ->
                new Wildcard("(J)" + Hook.DUAL_NODE.getInternalDesc()).matches(methodNode.desc)).forEach(methodNode -> {
            Hook.CACHE_TABLE.put(new RSMethod(methodNode, "get"));
        });
    }
}
