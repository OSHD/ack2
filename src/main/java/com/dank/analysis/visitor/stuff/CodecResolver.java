package com.dank.analysis.visitor.stuff;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

/**
 * Finds multipliers by finding the most common and valid pair of discovered encoders and decoders,
 * if it's unable to successfully find the MCVP it will default to the most common encoder and decoders.
 * The more methods this interprets the higher the confidence can become.
 *
 * @see org.objectweb.asm.tree.analysis.Analyzer
 *
 * @author Brainfree
 */
public class CodecResolver extends CodecInterpret {

    Map<String,Node> nodes;

    public CodecResolver() {
        nodes = new HashMap<>();
    }

    @Override
    public void visitEncoder(String owner, String name, String desc, AbstractInsnNode encoder) {
        String key = mkMemberKey(owner, name, desc);
        Node node = nodes.get(key);
        if(node == null) {
            node = new Node(owner,name,desc);
            nodes.put(key,node);
        }
        Number num = extractNum(encoder);
        if(num == null) throw new Error("NaN:" + encoder.opname());
        if(num instanceof Float || num instanceof Double) return;
        if(num.intValue() == 0) return;
        node.encoders.add(num);
    }

    @Override
    public void visitDecoder(String owner, String name, String desc, AbstractInsnNode decoder) {
        String key = mkMemberKey(owner, name, desc);
        Node node = nodes.get(key);
        if(node == null) {
            node = new Node(owner,name,desc);
            nodes.put(key,node);
        }
        Number num = extractNum(decoder);
        if(num == null) throw new Error("NaN:" + decoder.opname());
        if(num instanceof Float || num instanceof Double) return;
        if(num.intValue() == 0) return;
        node.decoders.add(num);
    }

    private static Number extractNum(AbstractInsnNode ain) {
        final int op = ain.opcode();
        if(op == LDC) return (Number) ((LdcInsnNode)ain).cst;
        if(op >= 2 && op <= 8) return op - 3; //ICONSTS
        switch (op) {
            case LCONST_0: return 0L;
            case LCONST_1: return 1L;
            case FCONST_0: return 0F;
            case FCONST_1: return 1F;
            case DCONST_0: return 0D;
            case DCONST_1: return 1D;
        }
        if(ain instanceof IntInsnNode) {
            return ((IntInsnNode)ain).operand;
        }
        return null;
    }

    private static String mkMemberKey(String owner, String name, String desc) {
        return owner + "#" + name + "@" + desc;
    }

    private static boolean isEncodableType(String desc) {
        Type type = Type.getType(desc);
        int sort = type.getSort();
        return sort == Type.INT || sort == Type.LONG;
    }

    public Number getEncoder(String owner, String name, String desc) {
        if(!isEncodableType(desc)) return null;
        String key = mkMemberKey(owner,name,desc);
        Node node = nodes.get(key);
        if(node == null) return null;
        return node.getEncoder();
    }

    public Number getDecoder(String owner, String name, String desc) {
        if(!isEncodableType(desc)) return null;
        String key = mkMemberKey(owner,name,desc);
        Node node = nodes.get(key);
        if(node == null) return null;
        return node.getDecoder();
    }

    private static final class Node {

        String owner;
        String name;
        String desc;

        Set<Number> encoders;
        Set<Number> decoders;

        Node(String owner,String name,String desc) {
            this.owner = owner;
            this.name  = name;
            this.desc  = desc;
            this.encoders = new HashSet<>();
            this.decoders = new HashSet<>();
        }

        boolean computed=false;
        Number encoder;
        Number decoder;

        Number getEncoder() {
            if(computed) return encoder;
            compute();
            return encoder;
        }

        Number getDecoder() {
            if(computed) return decoder;
            compute();
            return decoder;
        }

        private static class Pair {

            final Number enc;
            final Number dec;

            public Pair(Number enc, Number dec) {
                this.enc = enc;
                this.dec = dec;
            }

            boolean comp;
            int result;

            int compute() {
                if(comp) return result;
                BigInteger a = BigInteger.valueOf(enc.longValue());
                BigInteger b = BigInteger.valueOf(dec.longValue());
                BigInteger c = a.multiply(b);
                result = c.intValue();
                comp = true;
                return result;
            }

            boolean isValid() {
                return compute() == 1;
            }

            @Override
            public int hashCode() {
                return compute();
            }

            @Override
            public boolean equals(Object o) {
                Pair p = (Pair) o;
                // A * B == B * A
                return enc.equals(p.enc) && dec.equals(p.dec)
                    || enc.equals(p.dec) && dec.equals(p.enc);
            }

            public String toString() {
                return enc + " x " + dec + " == " + (enc.intValue()*dec.intValue());
            }
        }

        private void compute() {



/*
            if(encoders.isEmpty() || decoders.isEmpty()) { //Nothing to compute
                computed = true;
                return;
            }
*/

            // Try to find the most common, valid, pair...

            HashMap<Pair,AtomicInteger> results = new HashMap<>(
                    Math.max(encoders.size(),decoders.size()));


            for(Number enc : encoders) {
                for(Number dec : decoders) {
                    Pair p = new Pair(enc,dec);
                    if(p.isValid()) {
                        AtomicInteger n = results.get(p);
                        if(n == null) {
                            results.put(p,new AtomicInteger(1));
                        } else {
                            n.incrementAndGet();
                        }
                    }
                }
            }

            Pair most_common   = null;
            Pair most_common_2 = null;
            int most = Integer.MIN_VALUE;

            for(Map.Entry<Pair,AtomicInteger> result : results.entrySet()) {

                Pair p = result.getKey();
                AtomicInteger valid_hits = result.getValue();
                int hits = valid_hits.get();

                if(hits > most) {
                    most_common   = p;
                    most_common_2 = null;
                    most = hits;

                } else if(hits == most) {
                    most_common_2 = p;
                }

            }


           /* if(most_common != null && most_common_2 != null) {
                // Their was a tie, can't decide...
            }*/


            if(most_common != null && most_common_2 == null) {

                encoder = most_common.enc;
                decoder = most_common.dec;

                //System.out.println("Strong:" + most_common);

            } else { //No higher confidence hits, default to the most frequent...

                //TODO find the most common decoder or encoder, then its inverse?

                //TODO -1 wildcards : If a pair is equal in mag, but opposite in sign, lean towards the most common (positive
                // 142675037 x 1346447349 == 1
                // -142675037 x 1346447349 == -1   < Does not know if the encoder or decoder sign is flipped

                //TODO Reduction cases: If only a single encoder or decoder exists, use its inverse?
                // 833772376 x 1923240128 == 512
                // 833772376 x 925508876 == 32
                // 833772376 x -1916106429 == 8

               /* System.out.println(owner + "#" + name + "@" + desc);
                System.out.println("Enc: " + encoders);
                System.out.println("Dec: " + decoders);
                for(Number enc : encoders) {
                    for (Number dec : decoders) {
                        Pair p = new Pair(enc, dec);
                        System.out.println(p);
                    }
                }
                System.out.println("..................................");*/


                if(!encoders.isEmpty()) encoder = Collections.max(encoders, COMP);
                if(!decoders.isEmpty()) decoder = Collections.max(decoders, COMP);

                //System.out.println("Weak: [" + encoder + "x" + decoder + "]");

            }

            computed = true;

        }


        private static final Comparator<Number> COMP = new Comparator<Number>() {
            @Override
            public int compare(Number o1, Number o2) {
                return Long.compare(o1.longValue(),o2.longValue());
            }
        };

    }



}
