package com.dank.analysis.visitor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.ArithmeticNode;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.NumberNode;

/**
 * Project: DankWise
 * Time: 20:43
 * Date: 13-02-2015
 * Created by Dogerina.
 */
public class MultiplierVisitor extends NodeVisitor {

    private static final Map<String, List<Integer>> decoders;
    private static final Map<String, List<Integer>> encoders;

    static {
        decoders = encoders = new HashMap<>();
    }

    public static int getDecoder(final String key) {
        int mostUsed = 0;
        int highestFreq = 0;
        final List<Integer> multipliers = MultiplierVisitor.decoders.get(key);
        if (multipliers == null) return 0;
        for (final int modulus : multipliers) {
            final int count = (int) multipliers.stream().filter(i -> i.equals(modulus)).count();
            if (count > highestFreq) {
                highestFreq = count;
                mostUsed = modulus;
            }
        }
        return mostUsed;
    }

    public static int getEncoder(final String key) {
        int mostUsed = 0;
        int highestFreq = 0;
        final List<Integer> multipliers = MultiplierVisitor.encoders.get(key);
        if (multipliers == null) return 0;
        for (final int modulus : multipliers) {
            final int count = (int) multipliers.stream().filter(i -> i.equals(modulus)).count();
            if (count > highestFreq) {
                highestFreq = count;
                mostUsed = modulus;
            }
        }
        return mostUsed;
    }

    public static int inverseDecoder(final String key) {
        try {
            final BigInteger num = BigInteger.valueOf(getDecoder(key));
            return num.modInverse(new BigInteger(String.valueOf(1L << 32))).intValue();
        } catch (final Exception e) {
            return 0;
        }
    }

    private boolean isSetting(final ArithmeticNode an) {
        return an.hasParent() && (an.parent().opcode() == PUTSTATIC || an.parent().opcode() == PUTFIELD);
    }

    @Override
    public void visitOperation(final ArithmeticNode an) {
        if (isSetting(an)) {
            final FieldMemberNode fmn = (FieldMemberNode) an.parent();
            final NumberNode nn = an.firstNumber();
            if (!fmn.desc().equals("I") || nn == null || nn.opcode() != LDC)
                return;
            final int encoder = nn.number();
            if (encoder % 2 != 0) {
                if (!encoders.containsKey(fmn.key()))
                    encoders.put(fmn.key(), new ArrayList<>());
                encoders.get(fmn.key()).add(encoder);
            }
        } else if (an.multiplying() && an.children() == 2) {
            //System.out.println(an.tree());
            final FieldMemberNode fmn = an.firstField();
            final NumberNode nn = an.firstNumber();
            if (fmn == null || !fmn.getting() || nn == null || nn.opcode() != LDC)
                return;
            final int decoder = nn.number();
            if (decoder % 2 != 0) {
                if (!decoders.containsKey(fmn.key()))
                    decoders.put(fmn.key(), new ArrayList<>());
                decoders.get(fmn.key()).add(decoder);
            }
        }
        //TODO check if encoder identification is good enough. if it is, use it to validate multipliers
        //and inverse it for fields that decoders were not identified for
    }
}
