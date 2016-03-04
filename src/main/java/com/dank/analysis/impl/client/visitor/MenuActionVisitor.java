package com.dank.analysis.impl.client.visitor;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.commons.cfg.tree.node.VariableNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;

/**
 * Project: DankWise
 * Time: 19:00
 * Date: 11-02-2015
 * Created by Dogerina.
 */
public class MenuActionVisitor extends NodeVisitor {

    @Override
    public void visitNumber(NumberNode nn) {
        if (nn.number() == 500) {
            if (nn.hasParent() && (nn.parent().opcode() == IF_ICMPGE || nn.parent().opcode() == IF_ICMPLE)) {
                final FieldMemberNode fmn = (FieldMemberNode) nn.parent().layer(IMUL, GETSTATIC);
                if (fmn != null && fmn.desc().equals("I")) {
                    Hook.CLIENT.put(new RSField(fmn, "menuItemCount"));
                }
            }
            nn.tree().accept(new NodeVisitor() {
                @Override
                public void visitVariable(VariableNode vn) {
                    final FieldMemberNode fmn = vn.parent().firstField();
                    if (fmn == null) return;
                    Hook.CLIENT.put(new RSMethod(vn.tree().method(), "addMenuRow"));
                    switch (vn.var()) {
                        case 0:
                            Hook.CLIENT.put(new RSField(fmn, "menuActions"));
                            break;
                        case 1:
                            Hook.CLIENT.put(new RSField(fmn, "menuNouns"));
                            break;
                        case 2:
                            Hook.CLIENT.put(new RSField(fmn, "menuOpcodes"));
                            break;
                        case 3:
                            Hook.CLIENT.put(new RSField(fmn, "menuArg0"));
                            break;
                        case 4:
                            Hook.CLIENT.put(new RSField(fmn, "menuArg1"));
                            break;
                        case 5:
                            Hook.CLIENT.put(new RSField(fmn, "menuArg2"));
                            break;
                        default: {
                            throw new RuntimeException("WTF");
                        }
                    }
                }
            });
        }
    }
}
