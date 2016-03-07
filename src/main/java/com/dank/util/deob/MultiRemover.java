package com.dank.util.deob;

import com.dank.asm.RIS;
import com.dank.hook.RSField;
import jdk.internal.org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

/**
 * Notations:
 * - Field:
 * f  = non-coded field
 * F  = encoded field
 * F' = decoded field
 * - Coder:
 * C  = encoder
 * C' = decoder
 * - Inverse function:
 * I(X) = gets the encoder if its a decoder, the decoder if its a encoder
 * ---------------------
 * Properties:
 * <p>
 * C  *  C' == 1  *Fundamental*
 * f  *  C  == F
 * f  *  C' == F'
 * F  *  C' == f
 * F' *  C  == f
 * C' == I(C)
 * C  == I(C')
 * <p>
 * Logical Properties:
 * f += v ==> f = f + v
 * f *= f ==> f = f * v
 * f /= v ==> f = f / v
 * <p>
 * Axioms:
 * <p>
 * if( f * C == F ):
 * <p>
 * f += v
 * ==  f == f + v
 * ==  f * C == f * C + v * C
 * ==  F += (v*C)
 * -----------------------------
 * f *= v
 * ==  f == f * v
 * ==  f * C == f * v * C
 * ==  F *= (v)
 * -----------------------------
 */

/**
 * Every field operation must be accounted for by some form
 * of a coded statement. If not all field operations are account
 * for then that field is not multiplied.
 */
//Goal find the constant in which to multiply all encoders/deocders which will cancel/simplify the expression ( --> C * 1 == C )
public class MultiRemover {

    private final HashMap<RSField, ActiveModulas> correct_multipliers = new HashMap<RSField, ActiveModulas>();

    private static class FieldKey {

        final String owner;
        final String name;
        final String desc;

        FieldKey(FieldInsnNode fin) {
            this(fin.owner, fin.name, fin.desc);
        }

        FieldKey(String owner, String name, String desc) {
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }

        boolean cache = false;
        int hash = 0;

        @Override
        public int hashCode() {
            if (cache) return hash;
            hash = Arrays.hashCode(new Object[]{owner, name, desc});
            cache = true;
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            assert o instanceof FieldKey;
            FieldKey key = (FieldKey) o;
            return key.owner.equals(owner)
                    && key.name.equals(name)
                    && key.desc.equals(desc);
        }

        @Override
        public String toString() {
            return owner + "#" + name + "@" + desc;
        }

    }

    private static interface CodedAssignment {
        void multiply(long num);

        LdcInsnNode getLdc();
    }

    /**
     * class.field = constant
     */
    private static class PutConstant implements CodedAssignment {

        final FieldInsnNode fin;
        final LdcInsnNode ldc;

        PutConstant(FieldInsnNode fin, LdcInsnNode ldc) {
            // if(ldc == null) its 0
            this.fin = fin;
            this.ldc = ldc;
        }


        @Override
        public void multiply(long num) {

        }

        @Override
        public LdcInsnNode getLdc() {
            return ldc;
        }
    }

    /**
     * class.field = value * encoder
     */
    private static class PutValue implements CodedAssignment {

        final FieldInsnNode fin;
        final LdcInsnNode ldc;

        PutValue(FieldInsnNode fin, LdcInsnNode ldc) {
            this.fin = fin;
            this.ldc = ldc;
        }

        @Override
        public void multiply(long num) {

        }

        @Override
        public LdcInsnNode getLdc() {
            return ldc;
        }
    }

    /**
     * int field_value = class.field * decoder
     */
    private static class GetField implements CodedAssignment {

        final FieldInsnNode fin;
        final LdcInsnNode ldc;

        GetField(FieldInsnNode fin, LdcInsnNode ldc) {
            this.fin = fin;
            this.ldc = ldc;
        }

        @Override
        public void multiply(long num) {

        }

        @Override
        public LdcInsnNode getLdc() {
            return ldc;
        }
    }

    private static class StackHound implements CodedAssignment {

        final FieldInsnNode fin;
        final LdcInsnNode ldc;

        StackHound(FieldInsnNode fin, LdcInsnNode ldc) {
            this.fin = fin;
            this.ldc = ldc;
        }

        @Override
        public void multiply(long num) {

        }

        @Override
        public LdcInsnNode getLdc() {
            return ldc;
        }
    }

    /**
     * v = encoded value
     * field += v   ==  field = field + v
     * field -= v   ==  field = field - v
     */
    private static class PutCompound implements CodedAssignment {

        FieldInsnNode A;
        FieldInsnNode B;
        LdcInsnNode ldc;

        PutCompound(FieldInsnNode A, FieldInsnNode B, LdcInsnNode ldc) {
            this.A = A;
            this.B = B;
            this.ldc = ldc;
        }

        @Override
        public void multiply(long num) {

        }

        @Override
        public LdcInsnNode getLdc() {
            return ldc;
        }

    }

    private static class Transfer implements CodedAssignment {

        FieldInsnNode A;
        FieldInsnNode B;

        Transfer(FieldInsnNode A, FieldInsnNode B) {
            this.A = A;
            this.B = B;
        }

        @Override
        public void multiply(long num) {
        }

        @Override
        public LdcInsnNode getLdc() {
            return null;
        }

    }

    public static int getInverse(int coder) {
        try {
            final BigInteger num = BigInteger.valueOf(coder);
            return num.modInverse(new BigInteger(String.valueOf(1L << 32))).intValue();
        } catch (final Exception e) {
            return 0;
        }
    }

    //Coders (encoder/decoder) will always be 0, or an ldc
    private static boolean canBeCoder(AbstractInsnNode ain) {
        if (ain.opcode() == ICONST_0) {
            return true;
        } else if (ain.opcode() == LDC) {
            Object cst = ((LdcInsnNode) ain).cst;
            if (cst instanceof Number) return true;
        } //Can't be an int instruction
        return false;
    }


    public static final RIS.Constraint L2 = match -> match.length == 2;
    public static final RIS.Constraint L4 = match -> match.length == 4;


    private static class Handle {
        CodedAssignment handler;
        HashSet<FieldInsnNode> handles;
    }

    private static class Node {

        final FieldKey key;
        HashSet<FieldInsnNode> access;
        Deque<Handle> handles;
        int num_handles = 0;

        Node(FieldKey key) {
            this.key = key;
            this.access = new HashSet<>();
            this.handles = new ArrayDeque<>();
        }

        HashSet<FieldInsnNode> handled = new HashSet<>();

        void addHandle(CodedAssignment handler, FieldInsnNode... fields) {
            for (FieldInsnNode fin : fields) {
                if (!access.contains(fin)) {
                    System.err.println("Bad handle:" + fin);
                    continue;
                }

                if (handled.contains(fin)) {
                    for (Handle handle : handles) {
                        if (handle.handles.contains(fin)) {
                            // System.err.println("Cross Handle(" + fin + "): " + handler + " | " + handle.handler);
                            return;
                        }
                    }
                    throw new Error("What?");
                }
            }

            Handle handle = new Handle();
            handle.handler = handler;
            handle.handles = new HashSet<>(Arrays.asList(fields));
            handles.add(handle);
            handled.addAll(Arrays.asList(fields));
            num_handles += fields.length;
        }

    }

    private static Node getNode(Map<FieldKey, Node> nodes, FieldInsnNode fin) {
        return nodes.get(new FieldKey(fin));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private static boolean chkEquality(FieldInsnNode A0, FieldInsnNode B0) {
        if (A0.owner.equals(A0.owner)
                && A0.name.equals(B0.name)
                && A0.desc.equals(B0.desc)) {
            return true;
        }
        return false;
    }

    private static final Pattern CASE_0 = Pattern.compile("(ICONST_1|LCONST_1) ALOAD GETFIELD (IMUL|LMUL) PUTFIELD".toLowerCase());

    private static void visit_StackSettle(MethodNode mn, HashMap<FieldKey, Node> nodes) {

        AbstractInsnNode[] stack = mn.instructions.toArray();
        for (int i = 0; i < stack.length; i++) {
            AbstractInsnNode ain = stack[i];

            if (ain instanceof FieldInsnNode) {

                FieldInsnNode fin = (FieldInsnNode) ain;
                Node node = getNode(nodes, fin);
                if (node == null || node.handled.contains(fin)) continue;

                //     if(ain.toString().equals("s#k@I"))
                if (ain.opcode() == PUTFIELD || ain.opcode() == PUTSTATIC) {
                    StatementExplorer.explore(stack, i, 0, new StatementExplorer.Observer() {
                        @Override
                        public void visitStarted(AbstractInsnNode initial) {
                            System.out.println(initial + " ...");
                        }

                        @Override
                        public void visit(AbstractInsnNode cur, int pos, int stackSize) {
                            System.out.println(cur.opname() + ":" + cur + "|=" + stackSize);
                        }

                        @Override
                        public void visitEnded(AbstractInsnNode last, int remaining) {
                            System.out.println("..." + remaining);
                        }
                    });
                }

            }
        }
    }


   /* private static void visit_StackSettle(MethodNode mn, HashMap<FieldKey, Node> nodes) {

        AbstractInsnNode[] stack = mn.instructions.toArray();
        for(int i = 0; i < stack.length; i++) {
            AbstractInsnNode ain = stack[i];

            if(ain instanceof FieldInsnNode) {

                FieldInsnNode fin = (FieldInsnNode) ain;
                Node node = getNode(nodes,fin);
                if(node == null || node.handled.contains(fin)) continue;

                AbstractInsnNode pre  = ain.previous();
                AbstractInsnNode next = ain.next();
                AbstractInsnNode next1 = next==null?null:next.next();

                boolean hit = false;

                int low = -1;
                int start = -1;
                int size = 0;
                // field = value * encoder
                // field = encoder * value
                if ((ain.opcode() == PUTFIELD || ain.opcode() == PUTSTATIC)) {
                    if (pre != null && (pre.opcode() == IMUL || pre.opcode() == LMUL)) {
                        hit = true;
                        low = i-2;
                        start = i;
                        size = 0;
                    }
                } else {
                    */
    /**
     *
     * GETFIELD    LDC
     * LDC         GETFIELD
     * ---------
     * IMUL
     *//*
                    if ((ain.opcode() == GETFIELD || ain.opcode() == GETSTATIC)) {
                        if (next != null && (next.opcode() == IMUL || next.opcode() == LMUL)) {
                            hit = true;
                            start = i+1;
                            size  = 1; //
                        } else {
                            if(node.key.toString().equals("s#k@I")) {
                               System.out.println(next.opname() + "," + next1.opname() );
                            }
                            if(next != null && next1 != null && (next.opcode() == LDC)
                                    && (next1.opcode() == IMUL || next1.opcode() == LMUL)) {
                                hit = true;
                                low = i+1;
                                start = i+2;
                                size  = 1;
                            }
                        }
                    }
                }

                if(!hit) continue;


                boolean log = false;
                if(node.key.toString().equals("s#k@I")) {
                    log = true;
                }

                LdcInsnNode ldc = null;

                for(int p = start; p > 0; p--) {
                    AbstractInsnNode cur = stack[p];

                    if(cur instanceof LabelNode || cur instanceof JumpInsnNode) break;

                    int delta = DELTAS[cur.opcode()];
                    size -= delta;
                    if(log) System.out.println("LOG<" + RIS.OPCODE_NAME_MAP.get(cur.opcode()) + ">:" + cur + "(" + delta + " |= " + size + ")");
                    if(cur.opcode() == LDC && ((p == low)||size==0)) ldc = (LdcInsnNode) cur;
                    if(size == 0) break;
                }

                if(ldc != null) {
                    mn.instructions.insert(ldc, new LdcInsnNode("^ Multi for " + fin));
                    node.addHandle(new StackHound(fin,ldc),fin);
                }

            }
        }
    }*/


    private static final Pattern TRANSFER = Pattern.compile("(ICONST_1|LCONST_1) ALOAD GETFIELD (IMUL|LMUL) PUTFIELD".toLowerCase());
    private static final Pattern TRANSFER_0 = Pattern.compile("ALOAD GETFIELD (ICONST_1|LCONST_1) (IMUL|LMUL) PUTFIELD".toLowerCase());

    private static void visit_Transfer(HashMap<FieldKey, Node> nodes, RIS searcher) {

        for (AbstractInsnNode[] match : searcher.search(TRANSFER)) {
            FieldInsnNode A = (FieldInsnNode) match[2];
            FieldInsnNode B = (FieldInsnNode) match[4];
            if (!chkEquality(A, B)) continue;
            Node node = getNode(nodes, A); // A.equals(B)
            if (node == null) continue;
            node.addHandle(new Transfer(A, B), A, B);
        }

        for (AbstractInsnNode[] match : searcher.search(TRANSFER_0)) {
            FieldInsnNode A = (FieldInsnNode) match[1];
            FieldInsnNode B = (FieldInsnNode) match[4];
            if (!chkEquality(A, B)) continue;
            Node node = getNode(nodes, A); // A.equals(B)
            if (node == null) continue;
            node.addHandle(new Transfer(A, B), A, B);
        }

    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private static final Pattern PUT_COMPOUND = Pattern.compile("GETSTATIC LDC (IADD|ISUB|LADD|LSUB) PUTSTATIC".toLowerCase());
    private static final Pattern PUT_COMPOUND_0 = Pattern.compile("DUP GETFIELD LDC (IADD|ISUB|LADD|LSUB) PUTFIELD".toLowerCase());

    private static final Pattern PUT_COMPOUND_1 = Pattern.compile("GETSTATIC LDC (ILOAD|LLOAD) (IMUL}LMUL) (IADD|ISUB|LADD|LSUB) PUTSTATIC".toLowerCase());
    private static final Pattern PUT_COMPOUND_2 = Pattern.compile("".toLowerCase());


    private static void visit_PutCompound(HashMap<FieldKey, Node> nodes, RIS searcher) {

        for (AbstractInsnNode[] match : searcher.search(PUT_COMPOUND)) {
            FieldInsnNode A = (FieldInsnNode) match[0];
            FieldInsnNode B = (FieldInsnNode) match[3];
            if (!chkEquality(A, B)) continue;
            LdcInsnNode ldc = (LdcInsnNode) match[1];
            Node node = getNode(nodes, A); // A.equals(B)
            if (node == null) continue;
            node.addHandle(new PutCompound(A, B, ldc), A, B);
        }

        for (AbstractInsnNode[] match : searcher.search(PUT_COMPOUND_0)) {
            FieldInsnNode A = (FieldInsnNode) match[1];
            FieldInsnNode B = (FieldInsnNode) match[4];
            if (!chkEquality(A, B)) continue;
            LdcInsnNode ldc = (LdcInsnNode) match[2];
            Node node = getNode(nodes, A); // A.equals(B)
            if (node == null) continue;
            node.addHandle(new PutCompound(A, B, ldc), A, B);
        }

        for (AbstractInsnNode[] match : searcher.search(PUT_COMPOUND_1)) {
            FieldInsnNode A = (FieldInsnNode) match[0];
            FieldInsnNode B = (FieldInsnNode) match[5];
            if (!chkEquality(A, B)) continue;
            LdcInsnNode ldc = (LdcInsnNode) match[1];
            Node node = getNode(nodes, A); // A.equals(B)
            if (node == null) continue;
            node.addHandle(new PutCompound(A, B, ldc), A, B);
        }


    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private static final Pattern PUT_CONSTANT = Pattern.compile("LDC (PUTSTATIC|PUTFIELD)".toLowerCase());
    private static final Pattern PUT_CONSTANT_0 = Pattern.compile("(ICONST_0|LCONST_0) (PUTSTATIC|PUTFIELD)".toLowerCase());

    private static void visit_PutConstant(HashMap<FieldKey, Node> nodes, RIS searcher) {

        for (AbstractInsnNode[] match : searcher.search(PUT_CONSTANT)) {
            FieldInsnNode fin = (FieldInsnNode) match[1];
            LdcInsnNode ldc = (LdcInsnNode) match[0];
            if (ldc.cst instanceof String) continue;
            Node node = getNode(nodes, fin);
            if (node == null) continue;
            node.addHandle(new PutConstant(fin, ldc), fin);
        }

        for (AbstractInsnNode[] match : searcher.search(PUT_CONSTANT_0, match1 -> match1.length == 2)) {
            FieldInsnNode fin = (FieldInsnNode) match[1];
            Node node = getNode(nodes, fin);
            if (node == null) continue;
            node.addHandle(new PutConstant(fin, null), fin);
        }

    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private static final Pattern GET_VALUE = Pattern.compile("(GETFIELD|GETSTATIC) LDC (IMUL|LMUL)".toLowerCase());
    private static final Pattern GET_VALUE_0 = Pattern.compile("LDC (GETFIELD|GETSTATIC) (IMUL|LMUL)".toLowerCase());

    private static final Pattern GET_VALUE_1 = Pattern.compile("LDC ALOAD GETFIELD (IMUL|LMUL)".toLowerCase());
    private static final Pattern GET_VALUE_2 = Pattern.compile("ALOAD GETFIELD LDC (IMUL|LMUL)".toLowerCase());

    private static void visit_GetValue(HashMap<FieldKey, Node> nodes, RIS searcher) {

        for (AbstractInsnNode[] match : searcher.search(GET_VALUE)) {
            FieldInsnNode fin = (FieldInsnNode) match[0];
            LdcInsnNode ldc = (LdcInsnNode) match[1];
            Node node = getNode(nodes, fin);
            if (node == null) continue;
            node.addHandle(new GetField(fin, ldc), fin);
        }

        for (AbstractInsnNode[] match : searcher.search(GET_VALUE_0)) {
            FieldInsnNode fin = (FieldInsnNode) match[1];
            LdcInsnNode ldc = (LdcInsnNode) match[0];
            Node node = getNode(nodes, fin);
            if (node == null) continue;
            node.addHandle(new GetField(fin, ldc), fin);
        }

        for (AbstractInsnNode[] match : searcher.search(GET_VALUE_1)) {
            FieldInsnNode fin = (FieldInsnNode) match[2];
            LdcInsnNode ldc = (LdcInsnNode) match[0];
            Node node = getNode(nodes, fin);
            if (node == null) continue;
            node.addHandle(new GetField(fin, ldc), fin);
        }

        for (AbstractInsnNode[] match : searcher.search(GET_VALUE_2, match1 -> match1.length == 4)) {
            FieldInsnNode fin = (FieldInsnNode) match[1];
            LdcInsnNode ldc = (LdcInsnNode) match[2];
            Node node = getNode(nodes, fin);
            if (node == null) continue;
            node.addHandle(new GetField(fin, ldc), fin);
        }

    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private static final Pattern PUT_VALUE = Pattern.compile("LDC (IMUL|LMUL) (PUTFIELD|PUTSTATIC)".toLowerCase());
    private static final Pattern PUT_VALUE_0 = Pattern.compile("LDC (ILOAD|LLOAD) (IMUL|LMUL) (PUTFIELD|PUTSTATIC)".toLowerCase());

    private static void visit_PutValue(HashMap<FieldKey, Node> nodes, RIS searcher) {

        for (AbstractInsnNode[] match : searcher.search(PUT_VALUE)) {
            FieldInsnNode fin = (FieldInsnNode) match[2];
            LdcInsnNode ldc = (LdcInsnNode) match[0];
            Node node = getNode(nodes, fin);
            if (node == null) continue;
            node.addHandle(new PutValue(fin, ldc), fin);
        }

        for (AbstractInsnNode[] match : searcher.search(PUT_VALUE_0)) {
            FieldInsnNode fin = (FieldInsnNode) match[3];
            LdcInsnNode ldc = (LdcInsnNode) match[0];
            Node node = getNode(nodes, fin);
            if (node == null) continue;
            node.addHandle(new PutValue(fin, ldc), fin);
        }


    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private static void visit(MethodNode mn, HashMap<FieldKey, Node> nodes) {

        RIS searcher = new RIS(mn.instructions);

        visit_GetValue(nodes, searcher);
        visit_PutValue(nodes, searcher);
        visit_PutConstant(nodes, searcher);
        visit_PutCompound(nodes, searcher);
        visit_Transfer(nodes, searcher);
        //   visit_StackSettle(mn,nodes);
    }


    public static void run(Map<String, ClassNode> classes) {


        HashMap<FieldKey, Node> nodes = new HashMap<>();

        for (ClassNode cn : classes.values()) {
            for (MethodNode mn : cn.methods) {

                /** Search for field access **/
                boolean do_visit = false;
                for (AbstractInsnNode ain : mn.instructions.toArray()) {
                    if (ain instanceof FieldInsnNode) {
                        FieldInsnNode fin = (FieldInsnNode) ain;
                        if (fin.desc.equals("Z")) continue;
                        Type type = Type.getType(fin.desc);
                        if (type.getSort() == Type.INT
                                || type.getSort() == Type.LONG) { //Encoded types must have 32+ bits
                            do_visit = true;
                            FieldKey key = new FieldKey(fin);
                            Node node = nodes.get(key);
                            if (node == null) {
                                node = new Node(key);
                                nodes.put(key, node);
                            }
                            node.access.add((FieldInsnNode) ain);
                        }
                    }
                }

                if (!do_visit) continue; //No field access to analyze

                visit(mn, nodes);

            }
        }

        int num = 0;
        int none = 0;

        Node[] pool = nodes.values().toArray(new Node[nodes.size()]);
        Arrays.sort(pool, (o1, o2) -> Integer.compare(o1.access.size() - o1.num_handles, o2.access.size() - o2.num_handles));

        for (Node node : pool) {

            if (node.num_handles == node.access.size()) {
                long best = 0;
                int count = 0;
                for (Handle handle : node.handles) {

                    LdcInsnNode ldc = handle.handler.getLdc();
                    if (ldc == null) continue;
                    int val = ((Number) ldc.cst).intValue();

                    int c = 0;
                    for (Handle h0 : node.handles) {

                        if (h0 == handle) continue;
                        LdcInsnNode ldc0 = h0.handler.getLdc();
                        if (ldc0 == null) continue;
                        int val0 = ((Number) ldc0.cst).intValue();

                        if (val * val0 == 1) {
                            c++;
                        }

                    }

                    if (c > best) {
                        count = c;
                        best = val;
                    }

                }
                System.out.println("**" + node.key + "*" + best + " (" + count + "/" + node.access.size() + ")");
                num++;
            } else {
                System.out.println(node.key + ":(" + node.num_handles + "/" + node.access.size() + ")");
                AbstractInsnNode ain;

                out:
                for (FieldInsnNode fin : node.access) {
                    for (Handle handle : node.handles) {
                        for (FieldInsnNode f : handle.handles) {
                            if (fin == f) continue out;
                        }
                    }
                    System.out.println("\t-> " + fin.method().owner.name + "#" + fin.method().name + "@" + fin.method().desc);
                    InsnList stack = new InsnList();
                    /*stack.add(new LdcInsnNode(fin.toString() + " ^ Failed Here"));
                    stack.add(new InsnNode(Opcodes.POP));*/
                    fin.method().instructions.insert(fin, stack);
                }

            }
        } // -1 + -2 + 1 + 1 + 1

        System.out.println(none);
        System.out.println(num + "/" + (nodes.size() - none));

    }

}
