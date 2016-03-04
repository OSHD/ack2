package com.dank.util.deob;


import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

/**
 *
 * Filter:
 * 1. The predicate is the final argument
 * 2. The predicate is always of an int type
 * 3. The predicate value passed by a caller is a constant
 * 4. The comparator is the same for both error and return types
 * 5. Return type predicates only occur within void methods
 *
 * - Predicates are seemingly inserted only after a jump instructions,
 * though this property is not enforced.
 *
 * Process:
 * 1. Find all callers of the function, ensure the predicate is constant.
 * 1. Search for the error types.
 * 2. Ensure the comparison patten is the same for all cases.
 * 3. Search for return types with the header of the comparator
 * 4. Remove the predicate block from the root method, and the constant
 * from any caller methods. Adjust the methods descriptor, and the target
 * method descriptor of all callers.
 *
 * * We use error types to justify the return type.
 *
 * ------------------------------------------------------------------------
 *
 * Root Comparable: The constant value in which serves as the lower
 * bound of values in which the predicate may be, the maximum bound is
 * finally determines by the comparison, respectively based on the
 * comparison type.
 *
 * Valid Predicate Values: {C,COMPARISON} ==> VALID_DOMAIN
 * {C,IF_ICMPEQ} ==> [C,C]
 * {C,IF_ICMPNE} ==> [MIN,C) U (C,MAX]
 * {C,IF_ICMPLT} ==> [MIN,C)
 *
 * Comparison Header + Comparator:
 * > ILOAD: Load the predicate local parameter (always of an int-type)
 *   Valid-Range Load ////////////////////////////////////////////////////////////////
 * > LOAD ROOT COMPARABLE: This constant defines the lower bound of the valid range, respectively
 * > LOAD COMPARATOR:      This determine the upper bound of the valid-range, respectively
 *
 * Post-Header: The type is determined by the byproduct if the provided predicate did not fall within the valid-range:
 *
 * Type Error: It'll throw an IllegalStateException
 * new IllegalStateException
 * dup
 * invokespecial void IllegalStateException.<init>()V
 * athrow
 *
 * Type Return: It returns, this only occurs in void-type methods
 *
 *
 * @author Brainfree
 */
public class OpPredicateRemover {

    private static final Predicate<MethodInsnNode> PRED_FILTER = new Predicate<MethodInsnNode>() {
        @Override
        public boolean test(MethodInsnNode min) {
            Type[] args = Type.getArgumentTypes(min.desc);
            if(args.length == 0) return false;
            final int sort = args[args.length-1].getSort();
            if(sort == Type.BYTE || sort == Type.SHORT || sort == Type.INT) { //Of an int-type
                //Verifying the passed value is constant and directly before the method call
                assert min.previous() != null;
                final int pop = min.previous().opcode();
                if(pop >= 2 && pop <= 8) return true; // -1 <= C <= 5
                if(pop == BIPUSH)        return true;
                if(pop == SIPUSH)        return true;
                if(pop == LDC) {
                    LdcInsnNode lin = (LdcInsnNode) min.previous();
                    if(lin.cst.getClass() == Integer.class) return true;
                }
            }
            return false;
        }
    };

    public static final String ERROR_DESC = Type.getInternalName(IllegalStateException.class);

    private static MethodKey ensureKey(Map<String,ClassNode> classes, String owner, String name, String desc) {
        ClassNode cur = classes.get(owner);
        while (cur != null) {
            for(MethodNode mn : cur.methods) {
                if(mn.name.equals(name) && mn.desc.equals(desc)) {
                    return new MethodKey(cur.name,name,desc);
                }
            }
            cur = classes.get(cur.superName);
        }
        return null;
    }

    private static boolean isAbstract(Map<String,ClassNode> classes, String owner, String name, String desc) {
        MethodKey mk = ensureKey(classes,owner,name,desc);
        if(mk == null) return false;
        ClassNode cn = classes.get(mk.owner);
        if(Modifier.isInterface(cn.access)) return true;
        MethodNode mn = lookup(classes,mk);
        if(Modifier.isAbstract(mn.access)) return true;
        return false;
    }


    private static Map<MethodKey,Domain> domainMap = new HashMap<>();

    public static void run(Map<String,ClassNode> classes) {

        HashMap<MethodKey,Deque<Caller>> call_tree = new HashMap<>();

        /** Find all possible 'predicaticated' methods and their callers **/
        for(ClassNode cn : classes.values()) {

            for(MethodNode mn : cn.methods) {
                for(AbstractInsnNode ain : mn.instructions.toArray()) {
                    if(ain instanceof MethodInsnNode) {
                        MethodInsnNode min = (MethodInsnNode) ain;
                        if(min.itf || isAbstract(classes,min.owner,min.name,min.desc) ) continue;//TODO
                        if(!PRED_FILTER.test(min)) continue;

                        MethodKey key = ensureKey(classes,min.owner,min.name,min.desc);

                        if(key == null) {
                     //       System.err.println("Unknown method source:" + min.owner + "#" + min.name + "@" + min.desc);
                            continue;
                        }

                        if(!min.owner.equals(key.owner)) {
                      //      System.out.println(min.owner + "#" + min.name + "@" + min.desc + " is actually referring to " + key.owner);
                        }

                        Deque<Caller> callers = call_tree.get(key);
                        if(callers == null) {
                            callers = new ArrayDeque<>();
                            call_tree.put(key,callers);
                        }

                        callers.add(new Caller(min,mn));

                    }
                }
            }
        }

       // System.out.println("Possible Predicate Methods:" + call_tree.size());

        int pass = 0;
        int unused = 0;
        int undetermined = 0;
        int num_pred = 0;

        NEXT_METHOD:
        for(MethodKey mk : call_tree.keySet()) {


            MethodNode mn = lookup(classes,mk);
            if(mn == null) continue;

            Type[] args = Type.getArgumentTypes(mn.desc);
            int last_var = args.length-1;
            if(!Modifier.isStatic(mn.access)) last_var++;

            Domain prev_domain = null;

            List<OpPredicate> preds = new ArrayList<>();

            AbstractInsnNode[] stack = mn.instructions.toArray();
            int pos = 0;
            boolean used = false; // is the variable used?

            Type d = args[args.length-1];
            /** Find and verify domains **/
            while(pos < stack.length-1) {

                AbstractInsnNode ain = stack[pos++];
                Domain domain = pullDomain(d,ain,last_var);

                if(domain == null) {
                    if(ain.opcode() == ILOAD) {
                        VarInsnNode vin = (VarInsnNode) ain;
                        if(vin.var == last_var)
                            continue NEXT_METHOD; //Its referring to the final argument for local usage
                    }
                    continue;
                }

                if(prev_domain != null) {
                    if(prev_domain.base != domain.base
                    || prev_domain.comp != domain.comp)
                        continue NEXT_METHOD; //Domain is not constant throughout the method
                }

                used = true;
                prev_domain = domain;

                pos += 2; //Skip over the header

                /** Extract the predicate type **/
                AbstractInsnNode trigger = stack[pos]; //The 'trigger' instruction directly after the domain
                AbstractInsnNode head = stack[pos-2];

                if(trigger.opcode() == NEW) {
                    TypeInsnNode tin = (TypeInsnNode) trigger;
                    if(!tin.desc.equals(ERROR_DESC)) continue;
                    preds.add(new OpPredicate(head,stack[pos+3]));
                    pos += 4;
                } else if(trigger.opcode() == RETURN) {
                    preds.add(new OpPredicate(head,trigger));
                    pos += 1;
                }

            }

            /** Remove the predicates from the method **/
            if(!preds.isEmpty() || !used) {

                //... If it makes it here, it means that the final argument is a op-predicate

                domainMap.put(mk, prev_domain);

                final boolean do_remove = false;

                if(do_remove) {

                    if(!used) unused++;
                    pass++;
                    num_pred += preds.size();
                    System.out.println("Removing " + preds.size() + " predicates from " + mk.owner + "#" + mk.name + "@" + mk.desc);

                    /** Remove the predicates from the method **/
                    InsnList structs = mn.instructions;
                    for(OpPredicate pred : preds) {
                        pred.remove(structs);
                    }

                    /** Remove the predicate from the callers, and fix the method descriptor **/
                    String dec0 = Type.getMethodDescriptor(Type.getReturnType(mn.desc),
                            Arrays.copyOf(args, args.length - 1));
                    mn.desc = dec0;


                    Deque<Caller> callers = call_tree.get(mk);
                    for(Caller caller : callers) {
                        caller.call.desc = dec0;
                        caller.caller.instructions.remove(caller.call.previous()); //Remove the passed constant
                    }

                    callers.clear();
                    preds.clear();

                }

            } else {
                undetermined++;
            }

        }

        /*System.out.println(pass + "/" + call_tree.size() + " methods transformed");
        System.out.println(unused + " unused predicates");
        System.out.println(undetermined + " undetermined");
        System.out.println(num_pred + " predicates removed");*/

        call_tree.clear();

    }


    public static boolean hasPred(String owner, String name, String desc) {
        MethodKey key = new MethodKey(owner,name,desc);
        Domain domain = domainMap.get(key);
        return domain != null;
    }
    public static String getDomain(String owner, String name, String desc) {
        MethodKey key = new MethodKey(owner,name,desc);
        Domain domain = domainMap.get(key);
        if(domain==null) return null;
        return domain.toString();
    }

    public static Number getValueValue(String owner, String name, String desc) {
        MethodKey key = new MethodKey(owner,name,desc);
        Domain domain = domainMap.get(key);
        if(domain==null) return null;
        return domain.getValidValue();
    }



    private static class MethodKey {
        final String owner;
        final String name;
        final String desc;
        MethodKey(String owner,String name,String desc) {
            this.owner = owner;
            this.name  = name;
            this.desc  = desc;
        }

        boolean cache=false;
        int hash=0;
        public int hashCode() {
            if(cache) return hash;
            hash = Arrays.hashCode(new Object[]{owner,name,desc});
            cache = true;
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            assert o instanceof MethodKey;
            MethodKey key = (MethodKey) o;
            return key.owner.equals(owner)
                    && key.name.equals(name)
                    && key.desc.equals(desc);
        }

    }

    private static class Caller {
        final MethodInsnNode call;
        final MethodNode caller;
        Caller(MethodInsnNode call, MethodNode caller) {
            this.call   = call;
            this.caller = caller;
        }
        public String toString() {
            return "?#" + caller.name + "@" + caller.desc;
        }
    }

    private static class OpPredicate {
        final AbstractInsnNode head;
        final AbstractInsnNode tail;
        OpPredicate(AbstractInsnNode root,
                    AbstractInsnNode tail) { //Root being the header comparison
            this.head = root;
            this.tail = tail;
        }

        static int k;

        //   H              T
        //  PREV          PREV
        //  NEXT          NEXT
        void remove(InsnList src) {



            JumpInsnNode jump = (JumpInsnNode) this.head.next();
            jump.setOpcode(GOTO);



            AbstractInsnNode head = this.head.previous();
            if(head.opcode() != ILOAD) throw new Error();

            AbstractInsnNode next = head;

           do {
                AbstractInsnNode next0 = next.next();
                if(next != jump) src.remove(next);
                next = next0;
            } while (next!=tail);
            src.remove(tail);

        }

    }


    private static class Domain {

        final Type type;
        final int base;
        final int comp;

        public Domain(Type type, int base, int comp) {
            this.type = type;
            this.base = base;
            this.comp = comp;
        }

        int min() {
            switch (type.getSort()) {
                case Type.BYTE: return Byte.MIN_VALUE;
                case Type.SHORT: return Short.MIN_VALUE;
                case Type.INT: return Integer.MIN_VALUE;
                default: throw new Error();
            }
        }

        int max() {
            switch (type.getSort()) {
                case Type.BYTE: return Byte.MAX_VALUE;
                case Type.SHORT: return Short.MAX_VALUE;
                case Type.INT: return Integer.MAX_VALUE;
                default: throw new Error();
            }
        }

        @Override
        public String toString() {
            final int min = min();
            final int max = max();
            final String MAX = String.valueOf(max);
            final String MIN = String.valueOf(min);
            switch (comp) {
                case IF_ICMPEQ: // X == base
                    return String.valueOf(comp);
                case IF_ICMPNE: // X != base
                    return "(" + type + ")[" + MIN + "," + base + ") U (" + base + "," + MAX + "]~> " + (base+1);
                case IF_ICMPLT: // X < base
                    return "(" + type + ")[" + MIN + "," + base + ")~> " + num(min,base);
                case IF_ICMPGE: // X >= base
                    return "(" + type + ")[" + base + "," + MAX + "]~> " + num(base,max);
                case IF_ICMPGT: // X > base
                    return "(" + type + ")(" + base + "," + MAX + "]~> " + num(base,max);
                case IF_ICMPLE: // X <= base
                    return "(" + type + ")[" + MIN + "," + base + "]~> " + num(min,base);
            }
            return "[...](" + type + "," + base + "," + comp + ")";
        }

        public Number getValidValue() {
            final int min = min();
            final int max = max();
            final String MAX = String.valueOf(max);
            final String MIN = String.valueOf(min);
            switch (comp) {
                case IF_ICMPEQ: // X == base
                    return comp;
                case IF_ICMPNE: // X != base
                    return base+1;
                case IF_ICMPLT: // X < base
                    return num(min,base);
                case IF_ICMPGE: // X >= base
                    return num(base,max);
                case IF_ICMPGT: // X > base
                    return num(base,max);
                case IF_ICMPLE: // X <= base
                    return num(min,base);
            }
            return null;
        }
    }

    private static int num(int a, int b) {
        return (int) (((long)a+b)/2);
    }

    private static Domain pullDomain(Type dummy,AbstractInsnNode head, int var) {

        AbstractInsnNode pred = head;
        if(pred == null) return null;
        AbstractInsnNode base = pred.next();
        if(base == null) return null;
        AbstractInsnNode comp = base.next();
        if(comp == null) return null;

        /** Verify it's comparing two ints **/
        if(comp.opcode() < 159
        || comp.opcode() > 164) return null; //Not a integer comparison

        /** Verify its comparing the final argument **/
        if(pred.opcode() == ILOAD) { //its an int-type
            VarInsnNode vin = (VarInsnNode) pred;
            if(vin.var != var) return null; //It's not comparing the final var
            // ... It's comparing the final argument, in which is an int
        } else return null;

        /** Verify it's comparing to a constant value **/
        Integer base_value = extract(base);
        if(base_value == null) return null; //Not an int


        return new Domain(dummy,base_value,comp.opcode());

    }

    private static Integer extract(AbstractInsnNode ain) {
        if(ain.opcode() >= 2 && ain.opcode() <= 8) {
            return ain.opcode() - 3;
        } else if(ain.opcode() == LDC) {
            Object cst = ((LdcInsnNode) ain).cst;
            if(cst instanceof Integer) return (Integer) cst;
        } else if(ain instanceof IntInsnNode) {
            return ((IntInsnNode)ain).operand;
        }
        return null;
    }

    private static MethodNode lookup(Map<String,ClassNode> classes, MethodKey key) {
        ClassNode cn = classes.get(key.owner);
        if(cn == null) return null; //Could be a jdk class
        for(MethodNode mn : cn.methods) {
            if(mn.name.equals(key.name)
            && mn.desc.equals(key.desc)) {
                return mn;
            }
        }
        return null;
    }

}
