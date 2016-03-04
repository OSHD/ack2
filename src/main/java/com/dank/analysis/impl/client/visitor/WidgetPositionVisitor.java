package com.dank.analysis.impl.client.visitor;

import com.dank.analysis.visitor.TreeVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.tree.node.AbstractNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;

import java.util.List;

/**
 * Project: RS3Injector
 * Time: 19:25
 * Date: 10-02-2015
 * Created by Dogerina.
 */
public class WidgetPositionVisitor extends TreeVisitor {

    public WidgetPositionVisitor(BasicBlock block) {
        super(block);
    }

    @Override
    public boolean validateBlock(BasicBlock block) {
//        System.out.println("Validating block");

//        System.out.println(block.owner.owner.name + "." + block.owner.name);
        if (block.owner.desc.equals("()V") && block.count(new MemberQuery(Opcodes.GETSTATIC, "[I")) > 3 &&
                block.count(new MemberQuery(Opcodes.GETSTATIC, "[Z")) == 1 &&
                block.count(Opcodes.IALOAD) >= 4) {
            List<AbstractNode> layers = block.tree().layerAll(Opcodes.INVOKEVIRTUAL, Opcodes.IALOAD, Opcodes.GETSTATIC);
            return layers != null && layers.size() > 3;
        }
        return false;
    }

    @Override
    public void visitField(FieldMemberNode fmn) {

        List<AbstractNode> layers = block.tree().layerAll(Opcodes.INVOKEVIRTUAL, Opcodes.IALOAD, Opcodes.GETSTATIC);
//        System.out.println(layers.size());
        if (layers != null && layers.size() >= 3) {
            for (int i = 0; i < layers.size(); i++) {
                switch (i) {
                    case 0:
//                                    Hook.CLIENT.put(new RSField(cam, "camera" + "XZY".charAt(i - 2)));

                        Hook.CLIENT.put(new RSField(((FieldMemberNode) layers.get(i)).fin(), "widgetPositionsX"));
                        break;
                    case 1:
                        Hook.CLIENT.put(new RSField(((FieldMemberNode) layers.get(i)).fin(), "widgetPositionsY"));
                        break;
                    case 2:
                        Hook.CLIENT.put(new RSField(((FieldMemberNode) layers.get(i)).fin(), "widgetsWidth"));
                        break;
                    case 3:
                        Hook.CLIENT.put(new RSField(((FieldMemberNode) layers.get(i)).fin(), "widgetsHeight"));
                        break;
                }
            }
        }

//        System.out.println(">" + fmn.fin().owner + "." + fmn.fin().name);
    }

}
