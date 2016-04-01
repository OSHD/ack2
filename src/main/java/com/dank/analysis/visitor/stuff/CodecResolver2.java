package com.dank.analysis.visitor.stuff;

import org.objectweb.asm.tree.FieldInsnNode;

import java.math.BigInteger;
import java.util.*;

/**
 * @author Brainfree
 */
public class CodecResolver2 extends CodecInterpreter2 {

    private HashMap<String,Node> nodes = new HashMap<>();
    public CodecResolver2() {
    }



    @Override
    protected void guaranteedDecoded(FieldInsnNode field, Number decoder) {

        String key = key(field);
        Node node = nodes.get(key);
        if(node == null) {
            node = new Node(field.owner,field.name,field.desc);
            nodes.put(key,node);
        }

        node.add(decoder);

    }



    private static String key(FieldInsnNode fin) {
        return key(fin.owner,fin.name,fin.desc);
    }
    private static String key(String owner,String name,String desc) {
        return owner + "#" + name + "@" + desc;
    }

    private static final class Node implements Comparable<Node> {

        String owner;
        String name;
        String desc;
        String key;

        Set<Number> decoders;
        Map<Number,Integer> hits;

        Node(String owner, String name, String desc) {
            this.owner = owner;
            this.name = name;
            this.desc = desc;
            this.key = key(owner, name, desc);
            this.decoders = new HashSet<>();
            this.hits = new HashMap<>();
        }

        void add(Number decoder) {
            decoders.add(decoder);
            Integer hits = this.hits.get(decoder);
            int n=0;
            if(hits == null) {
                n = 1;
            } else {
                n = hits + 1;
            }
            this.hits.put(decoder,n);
        }

        Number getBest() {
            Number best = null;
            int best_hits = 0;
            for(Number n : decoders) {
                if(n instanceof Integer && !hasInverse(n.intValue())) {
                    // The real decoder would have a inverse (the encoder)
                    continue;
                }
                int hits = this.hits.get(n);
                if(hits > best_hits) {
                    best = n;
                    best_hits = hits;
                }
            }
            return best;
        }

        @Override
        public int compareTo(Node o) {
            return key.compareTo(o.key);
        }
    }

    public void print() {

        Map<String,Set<Node>> results = new TreeMap<>();
        for(Node result : nodes.values()) {
            Set<Node> set = results.get(result.owner);
            if(set == null) {
                set = new TreeSet<>();
                results.put(result.owner,set);
            }
            set.add(result);
        }

        for(Map.Entry<String,Set<Node>> result : results.entrySet()) {

         //   System.out.println(result.getKey() + ":");
            for(Node field : result.getValue()) {
                Number best = field.getBest();
                System.out.println(field.owner + "." + field.name + " x " + best);

                /*
                Set<Number> decs = field.decoders;

                if(decs.size() == 1) {
                    Number n = decs.toArray(new Number[1])[0];
                    System.out.println("\t" + (field.name + " x " + n) + "(" + field.hits.get(n) + ")");
                } else {

                    System.out.println("\t" + (field.name + ":"));

                    TreeSet<Number> sorted_dec = new TreeSet<>(field.decoders);
                    boolean isInt = field.desc.equals("I");

                    for(Number dec : sorted_dec) {

                        int num_hits = field.hits.get(dec);

                        if(isInt) {
                            System.out.println("\t\t" + ("x " + dec + "(" + num_hits + ") " + normalizeInts(dec,sorted_dec)) );
                        } else {
                            System.out.println("\t\t" + ("x " + dec + "(" + num_hits + ")" ));
                        }

                    }

                    System.out.println("\t\t" + ("Best: " + field.getBest()));

                }*/

            }

        }

    }

    private static String normalizeInts(Number n, Set<Number> others) {

        int inverse = getInverse(n.intValue());
        if(inverse == 0) return "()";

        StringBuilder b = new StringBuilder();
        for(Number o : others) {
            if(Objects.equals(n, o)) continue;
            int m = o.intValue() * inverse;
            String s = String.valueOf(m);
         //   if(s.length() > 5) s = s.substring(0,5);
            b.append(s).append(",");
        }
        return "(" + b.substring(0,Math.max(0,b.length()-1)) + ")";
    }
    // if a number does not have a inverse then it can't possibly be a decoder, since no encoder (the inverse) exists.
    private static boolean hasInverse(int decoder) {
        try {
            final BigInteger num = BigInteger.valueOf(decoder);
            num.modInverse(new BigInteger(String.valueOf(1L << 32)));
            return true;
        } catch (final Exception e) {
            return false;
        }
    }
    private static int getInverse(int coder) {
        try {
            final BigInteger num = BigInteger.valueOf(coder);
            return num.modInverse(new BigInteger(String.valueOf(1L << 32))).intValue();
        } catch (final Exception e) {
            return 0;
        }
    }

}
