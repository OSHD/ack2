package com.dank.analysis.impl.misc;

import java.lang.reflect.Modifier;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;

/**
 * Project: DankWise
 * Date: 26-02-2015
 * Time: 15:27
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class ExchangeOffer extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount() == 6 && cn.fieldCount(int.class) == 5 && cn.fieldCount(byte.class) == 1
                ? new ClassSpec(Hook.EXCHANGE_OFFER, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (FieldNode fn : cn.fields) {
            if (Modifier.isStatic(fn.access) || !fn.desc.equals("B"))
                continue;
            Hook.EXCHANGE_OFFER.put(new RSField(fn, "status"));
        }
        for (ClassNode crystal : super.getClassPath()) {
            for (MethodNode meth : crystal.methods) {
                if (meth.instructions.size() > 10000) {
                    for (BasicBlock dank : meth.graph()) {
                        dank.tree().accept(new BlackNigger());
                    }
                }
            }
        }
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("<init>") && mn.desc.startsWith("(L")) {
                for (BasicBlock block : mn.graph()) {
                    block.tree().accept(new WhiteNigger());
                }
            }
        }
    }

    private final class BlackNigger extends NodeVisitor {

        @Override
        public void visitField(FieldMemberNode fmn) {
            if (fmn.desc().equals(Hook.EXCHANGE_OFFER.getInternalArrayDesc())) {
                FieldMemberNode granpa = (FieldMemberNode) fmn.preLayer(AALOAD, GETFIELD);
                if (granpa != null) {
                    Hook.CLIENT.put(new RSField(fmn, "localExchangeOffers"));
                }
            }
        }
    }

    private final class WhiteNigger extends NodeVisitor {

        @Override
        public void visitMethod(MethodMemberNode mmn) {
            if (mmn.owner().equals(Hook.BUFFER.getInternalName())) {
//                System.out.println(mmn.tree());
                FieldMemberNode byteSet = (FieldMemberNode) mmn.preLayer(PUTFIELD);
                FieldMemberNode intSet = (FieldMemberNode) mmn.preLayer(IMUL, PUTFIELD);
                if (intSet != null) {
                    if (intSet.key().equals(Hook.EXCHANGE_OFFER.get("itemId").key())) {
                        Hook.BUFFER.put(new RSMethod(mmn, "readUShort"));
                    } else if (intSet.key().equals(Hook.EXCHANGE_OFFER.get("transferred").key())) {
                        Hook.BUFFER.put(new RSMethod(mmn, "readInt"));
                    }
                }
                if (byteSet != null && byteSet.desc().equals("B")) { //just incase
                    Hook.EXCHANGE_OFFER.put(new RSField(byteSet, "status"));
                    Hook.BUFFER.put(new RSMethod(mmn, "readByte"));

                }
            }
        }
    }
}
