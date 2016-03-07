package com.dank.analysis.impl.client.visitor;

import com.dank.analysis.impl.misc.GStrings;
import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Wildcard;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @author Septron
 * @since February, 27
 */
public class MessageChannelsVisitor extends TreeVisitor {

    public MessageChannelsVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
        return Modifier.isStatic(block.owner.access) &&
                new Wildcard("(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;?)V").matches(block.owner.desc);
    }

    @Override
    public void visitField(FieldMemberNode fmn) {
        if(fmn.desc().equals(Type.getDescriptor(Map.class))) {
            Hook.CLIENT.put(new RSField(fmn, "messageChannels"));
        }
    }
}
