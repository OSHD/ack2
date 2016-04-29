package com.dank.analysis.impl.client.visitor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import com.dank.hook.Hook;
import com.dank.hook.RSField;

/**
 * Project: DankWise
 * Time: 17:20
 * Date: 13-02-2015
 * Created by Dogerina.
 */
public class RunescriptOpcodeHandlerVisitor extends NodeVisitor implements Opcodes {

    //should've used Map<Integer, OpcodeHook> instead, but cbf now
    private final Set<OpcodeHook> hooks = new HashSet<OpcodeHook>() {
        {
            //bounds and positioning
            add(new OpcodeHook(1100, GETFIELD, "insetX"));
            add(new OpcodeHook(1601, GETFIELD, "insetY"));
            add(new OpcodeHook(1603, GETFIELD, "viewportWidth"));
            add(new OpcodeHook(1604, GETFIELD, "viewportHeight"));
            add(new OpcodeHook(1500, GETFIELD, "relativeX"));
            add(new OpcodeHook(1501, GETFIELD, "relativeY"));
            add(new OpcodeHook(1502, GETFIELD, "width"));
            add(new OpcodeHook(1503, GETFIELD, "height"));
            //ints
            add(new OpcodeHook(1702, GETFIELD, "index"));
            add(new OpcodeHook(2505, GETFIELD, "parentId"));
            add(new OpcodeHook(2605, GETFIELD, "modelZoom"));
            add(new OpcodeHook(1606, GETFIELD, "rotationX"));
            add(new OpcodeHook(1607, GETFIELD, "rotationY"));
            add(new OpcodeHook(1608, GETFIELD, "rotationZ"));
            add(new OpcodeHook(1701, GETFIELD, "itemId"));
            add(new OpcodeHook(1701, GETFIELD, "itemQuantity").skip(1));
            //strings
            add(new OpcodeHook(1801, GETFIELD, "[Ljava/lang/String;", "actions"));
            add(new OpcodeHook(1802, GETFIELD, "Ljava/lang/String;", "name"));
            add(new OpcodeHook(1112, GETFIELD, "Ljava/lang/String;", "text"));
            add(new OpcodeHook(1306, PUTFIELD, "Ljava/lang/String;", "selectedAction"));
            //booleans
            add(new OpcodeHook(1504, GETFIELD, "Z", "hidden"));
            add(new OpcodeHook(1118, PUTFIELD, "Z", "flippedVertically"));
            add(new OpcodeHook(1119, PUTFIELD, "Z", "flippedHorizontally"));
            add(new OpcodeHook(1115, PUTFIELD, "Z", "textShadowed"));
            //other ints
            add(new OpcodeHook(1101, PUTFIELD, "textColor"));
            add(new OpcodeHook(1103, PUTFIELD, "alpha"));
            add(new OpcodeHook(1113, PUTFIELD, "fontId"));
            add(new OpcodeHook(1116, PUTFIELD, "borderThickness"));
            add(new OpcodeHook(1117, PUTFIELD, "shadowColor"));
            add(new OpcodeHook(1201, PUTFIELD, "modelType"));
            add(new OpcodeHook(1201, PUTFIELD, "modelId").skip(1));
            add(new OpcodeHook(1105, PUTFIELD, "textureId"));
            add(new OpcodeHook(1106, PUTFIELD, "spriteId")); //1105 skip 1
            //listeners
            add(new OpcodeHook(1403, PUTFIELD, "[Ljava/lang/Object;", "mouseEnterListener"));
            add(new OpcodeHook(1404, PUTFIELD, "[Ljava/lang/Object;", "mouseExitListener"));
            add(new OpcodeHook(1408, PUTFIELD, "[Ljava/lang/Object;", "renderListener"));
            add(new OpcodeHook(1412, PUTFIELD, "[Ljava/lang/Object;", "mouseHoverListener"));
            add(new OpcodeHook(1417, PUTFIELD, "[Ljava/lang/Object;", "scrollListener"));
            add(new OpcodeHook(1407, PUTFIELD, "[Ljava/lang/Object;", "varListener"));
            add(new OpcodeHook(1407, PUTFIELD, "[I", "configTriggers"));
            add(new OpcodeHook(1414, PUTFIELD, "[Ljava/lang/Object;", "tableListener"));
            add(new OpcodeHook(1414, PUTFIELD, "[I", "tableTriggers"));
            add(new OpcodeHook(1415, PUTFIELD, "[Ljava/lang/Object;", "skillListener"));
            add(new OpcodeHook(1415, PUTFIELD, "[I", "skillTriggers"));
            //local info
            add(new OpcodeHook(3321, GETSTATIC, Hook.CLIENT, "energy"));
            add(new OpcodeHook(3322, GETSTATIC, Hook.CLIENT, "weight"));
            add(new OpcodeHook(3318, GETSTATIC, Hook.CLIENT, "myWorld"));
            add(new OpcodeHook(3312, GETSTATIC, "Z", Hook.CLIENT, "membersWorld"));
            add(new OpcodeHook(3600, GETSTATIC, Hook.CLIENT, "friendListSize"));
            add(new OpcodeHook(3621, GETSTATIC, Hook.CLIENT, "ignoreListSize"));
            add(new OpcodeHook(3611, GETSTATIC, "Ljava/lang/String;", Hook.CLIENT, "clanChatOwner"));
            add(new OpcodeHook(3625, GETSTATIC, "Ljava/lang/String;", Hook.CLIENT, "clanChatName"));
            add(new OpcodeHook(3618, GETSTATIC, "B", Hook.CLIENT, "clanChatRank"));
            add(new OpcodeHook(3612, GETSTATIC, Hook.CLIENT, "clanChatSize"));
            add(new OpcodeHook(3316, GETSTATIC, Hook.CLIENT, "myRights"));
            add(new OpcodeHook(3112, PUTSTATIC, Hook.CLIENT, "hideRoofs").skip(0));//good

            add(new OpcodeHook(3306, GETSTATIC,"[I", Hook.CLIENT, "levels").skip(2));//good
            add(new OpcodeHook(3305, GETSTATIC,"[I",Hook.CLIENT, "currentLevels").skip(2));//good
            add(new OpcodeHook(3307, GETSTATIC,"[I", Hook.CLIENT, "experiences").skip(2));//good

            //add(new OpcodeHook(3300, GETSTATIC,"I", Hook.CLIENT, "engineCycle").skip(0));//good

            add(new OpcodeHook(3614, GETFIELD, "world").container(Hook.CLAN_MATE));
            add(new OpcodeHook(3615, GETFIELD, "B", "rank").container(Hook.CLAN_MATE));
            add(new OpcodeHook(3613, GETFIELD, "Ljava/lang/String;", "displayName").container(Hook.CLAN_MATE));

            add(new OpcodeHook(3601, GETFIELD, "Ljava/lang/String;", "displayName")
                    .container(Hook.FRIENDED_PLAYER));
            add(new OpcodeHook(3601, GETFIELD, "Ljava/lang/String;", "previousName")
                    .container(Hook.FRIENDED_PLAYER).skip(1));
            add(new OpcodeHook(3602, GETFIELD, "world").container(Hook.FRIENDED_PLAYER));

            add(new OpcodeHook(3622, GETFIELD, "Ljava/lang/String;", "displayName")
                    .container(Hook.IGNORED_PLAYER));
            add(new OpcodeHook(3622, GETFIELD, "Ljava/lang/String;", "previousName")
                    .container(Hook.IGNORED_PLAYER).skip(1));

            //These are properly hooked inside ExchangeOffer analyzer
            add(new OpcodeHook(3904, GETFIELD, "I", "itemId").container(Hook.EXCHANGE_OFFER));
            add(new OpcodeHook(3905, GETFIELD, "I", "price").container(Hook.EXCHANGE_OFFER));
            add(new OpcodeHook(3906, GETFIELD, "I", "itemQuantity").container(Hook.EXCHANGE_OFFER));
            add(new OpcodeHook(3907, GETFIELD, "I", "transferred").container(Hook.EXCHANGE_OFFER));
            add(new OpcodeHook(3908, GETFIELD, "I", "spent").container(Hook.EXCHANGE_OFFER));
        }
    };

    private static FieldInsnNode next(AbstractInsnNode from, final int op, final String desc, final String owner, final int skips) {
        int skipped = 0;
        while ((from = from.next()) != null) {
            if (from.opcode() == op) {
                final FieldInsnNode topkek = (FieldInsnNode) from;
                if (topkek.desc.equals(desc) && (owner == null || owner.equals(topkek.owner))) {
//                    System.out.println("Skips : (" + skipped + ")" + topkek.owner + "." + topkek.name + "(" + topkek.opname() + ")");
                    if (skipped == skips) {
//                        System.out.println("test: " + from.method().key());
                        return topkek;
                    }
					skipped++;
                }
            }
        }
        return null;
    }

    boolean write = true;

    @Override
    public void visitNumber(final NumberNode nn) {
        for (final OpcodeHook hook : hooks) {
            if (hook.number == nn.number()) {
                final String owner = hook.fieldOpcode == GETSTATIC || hook.fieldOpcode == PUTSTATIC ? null : hook.container.getInternalName();
//                System.out.println(">>" + nn.number());
                final FieldInsnNode fin = next(nn.insn(), hook.fieldOpcode, hook.fieldDesc, owner, hook.skips);
                if (fin != null) {
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
