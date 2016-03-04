package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;

/**
 * Created by RSynapse on 2/20/2016.
 */
public class MessageHandler extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount(Hook.MESSAGES.getInternalArrayDesc()) == 1 && cn.fieldCount(int.class, true) == 1 ?
                new ClassSpec(Hook.MESSAGE_CHANNEL, cn) : null;
    }

    //ak.l getMessage
    @Override
    public void evaluate(ClassNode cn) {

        for (FieldNode fn : cn.fields) {
            if (fn.desc.equals(Hook.MESSAGES.getInternalArrayDesc())) {
                Hook.MESSAGE_CHANNEL.put(new RSField(fn, "messages"));
            }
            if (!Modifier.isStatic(fn.access) && fn.desc.equals("I")) {
                Hook.MESSAGE_CHANNEL.put(new RSField(fn, "index"));
            }
        }
        for(MethodNode methodNode : cn.methods) {
            if(new Wildcard("(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;?)*").matches(methodNode.desc)) {
                System.out.println(">"+ methodNode.desc);
                Hook.MESSAGE_CHANNEL.put(new RSMethod(methodNode, "createMessage"));

            }
            if(new Wildcard("(I?)*").matches(methodNode.desc)) {
                Hook.MESSAGE_CHANNEL.put(new RSMethod(methodNode, "getMessage"));

            }
        }
    }
}
