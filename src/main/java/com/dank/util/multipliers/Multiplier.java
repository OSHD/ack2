package com.dank.util.multipliers;


import com.dank.DankEngine;
import com.dank.asm.Assembly;
import com.dank.asm.Mask;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Greg
 * Date: 10/18/13
 * Time: 7:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Multiplier {

    public static HashMap<String, Integer> MULTIPLIERS = new HashMap<String, Integer>();
    public static HashMap<String, Integer> ALLMULTIPLIERS = new HashMap<String, Integer>();

    public static HashMap<String, Integer> getMultiples() {
        return MULTIPLIERS;
    }

    public static Integer getMultiple(String name) {
        for (Map.Entry<String, Integer> entry : getMultiples().entrySet()) {
//            System.out.println(entry.getKey());
            if (entry.getKey().equals(name))
                return entry.getValue();
        }

        for (Map.Entry<String, Integer> entry : ALLMULTIPLIERS.entrySet()) {
            if (entry.getKey().equals(name))
                return entry.getValue();
        }

        return null;
    }

    public static void loadCache_(Collection<ClassNode> classNodes) {
        HashMap<String, List<Integer>> ENCODERS = new HashMap<String, List<Integer>>();
        HashMap<String, List<Integer>> DECODERS = new HashMap<String, List<Integer>>();

        for (ClassNode classNode : classNodes) {
            for (MethodNode mn : classNode.methods) {
                for (AbstractInsnNode ain : mn.instructions.toArray()) {
                    if (ain instanceof FieldInsnNode) {
                        FieldInsnNode fin = (FieldInsnNode) ain;
                        if (!fin.desc.equals("I")) continue;

                        boolean isStatic = ((fin.opcode() == 178 || fin.opcode() == 179) ? true : false);
                        String key = null;
                        FieldNode superTest = DankEngine.classPath.getFieldFromSuper(classNode, fin.name, fin.desc, isStatic);
                        if (superTest != null) {
                            key = superTest.owner.name + "." + fin.name;
//                            System.out.println(key);
                        }
                        String key2 = fin.owner + "." + fin.name;

                        String[] keys = new String[]{(key != null ? key : ""), key2};

                        for (int i = 0; i < keys.length; i++) {
                            if (ain.opcode() == Opcodes.PUTFIELD || ain.opcode() == Opcodes.PUTSTATIC) {
                                if ((ain = ain.previous()).opcode() == Opcodes.IMUL) {
                                    if ((ain = ain.previous()).opcode() == Opcodes.LDC) {
                                        LdcInsnNode lin = (LdcInsnNode) ain;
                                        if (ENCODERS.containsKey(keys[i])) {
                                            List<Integer> mults = ENCODERS.get(keys[i]);
                                            int mult = (Integer) lin.cst;
                                            if (!mults.contains(mult)) mults.add(mult);

                                        } else {
                                            ENCODERS.put(keys[i], new ArrayList<>(Arrays.asList((Integer) lin.cst)));
                                        }
                                    }
                                }

                            } else if (ain.opcode() == Opcodes.GETFIELD || ain.opcode() == Opcodes.GETSTATIC) {
                                if ((ain = ain.next()).opcode() == Opcodes.LDC) {
                                    if (ain.next().opcode() == Opcodes.IMUL) {
                                        LdcInsnNode lin = (LdcInsnNode) ain;

                                        if (DECODERS.containsKey(keys[i])) {
                                            List<Integer> mults = DECODERS.get(keys[i]);
                                            int mult = (Integer) lin.cst;
                                            if (!mults.contains(mult)) mults.add(mult);

                                        } else {
                                            DECODERS.put(keys[i], new ArrayList<>(Arrays.asList((Integer) lin.cst)));

                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
        HashMap<String, Integer> encoders = new HashMap<String, Integer>();
        HashMap<String, Integer> decoders = new HashMap<String, Integer>();

        for (Map.Entry<String, List<Integer>> entry : DECODERS.entrySet()) {
            String key = entry.getKey();
            List<Integer> decs = entry.getValue();
            List<Integer> encs = ENCODERS.get(key);
            if (decs == null) continue;

            if (encs == null || encs.isEmpty()) {
                decoders.put(key, decs.get(0));
            } else {
                boolean found = false;
                for (int dec : decs) {
                    for (int enc : encs) {
                        if (dec * enc == 1) {
                            MULTIPLIERS.put(key, dec);
                            decoders.put(key, dec);
                            encoders.put(key, enc);
                            found = true;
                        }
                    }
                }
                if (!found) {
                    decoders.put(key, decs.get(0));
                    encoders.put(key, encs.get(0));
                    ALLMULTIPLIERS.put(key, decs.get(0));
                }
            }
        }

    }

    public static void loadMultipleCache(Collection<ClassNode> classNodes) {
        HashMap<String, List<Integer>> ENCODERS = new HashMap<String, List<Integer>>();
        HashMap<String, List<Integer>> DECODERS = new HashMap<String, List<Integer>>();

        for (ClassNode classNode : classNodes) {
            for (MethodNode mn : classNode.methods) {
                List<List<AbstractInsnNode>> encoders = Assembly.findAll(mn, Mask.GETFIELD.or(Mask.GETSTATIC).describe("I"), Mask.LDC, Mask.IMUL);
                List<List<AbstractInsnNode>> decoders = Assembly.findAll(mn, Mask.LDC, Mask.IMUL, Mask.PUTFIELD.or(Mask.PUTSTATIC).describe("I"));
                if (encoders != null)
                    for (List<AbstractInsnNode> basic : encoders) {
                        String name = ((FieldInsnNode) basic.get(0)).owner + "." + ((FieldInsnNode) basic.get(0)).name;

                        Object o = ((LdcInsnNode) basic.get(1)).cst;
                        if ((o instanceof Integer && o instanceof Double == false) || o instanceof String) {
                            Integer integer = (((LdcInsnNode) basic.get(1)).cst instanceof String ? Integer.getInteger((String) ((LdcInsnNode) basic.get(1)).cst) :
                                    (Integer) ((LdcInsnNode) basic.get(1)).cst);
                            if (!ENCODERS.containsKey(name)) {
                                List<Integer> temp = new ArrayList<Integer>();
                                temp.add(integer);
                                ENCODERS.put(name, temp);
                            } else {
                                ENCODERS.get(name).add(integer);
                            }
                        }
                    }
                if (decoders != null)

                    for (List<AbstractInsnNode> basic : decoders) {

                        Object o = ((LdcInsnNode) basic.get(0)).cst;
                        if ((o instanceof Integer && o instanceof Double == false) || o instanceof String) {
                            String name = ((FieldInsnNode) basic.get(2)).owner + "." + ((FieldInsnNode) basic.get(2)).name;
                            Integer integer = (((LdcInsnNode) basic.get(0)).cst instanceof String ? Integer.getInteger((String) ((LdcInsnNode) basic.get(0)).cst) :
                                    (Integer) ((LdcInsnNode) basic.get(0)).cst);
                            if (!DECODERS.containsKey(name)) {
                                List<Integer> temp = new ArrayList<Integer>();
                                temp.add(integer);
                                DECODERS.put(name, temp);
                            } else {
                                DECODERS.get(name).add(integer);
                            }
                        }
                    }
            }
        }
        HashMap<String, Integer> encoders = new HashMap<String, Integer>();
        HashMap<String, Integer> decoders = new HashMap<String, Integer>();

        for (Map.Entry<String, List<Integer>> entry : DECODERS.entrySet()) {

            String key = entry.getKey();
            List<Integer> decs = entry.getValue();
            List<Integer> encs = ENCODERS.get(key);
            if (decs == null) continue;

            if (encs == null || encs.isEmpty()) {
                decoders.put(key, decs.get(0));
            } else {
                boolean found = false;
                for (int dec : decs) {
                    for (int enc : encs) {
                        if (dec * enc == 1) {
                            MULTIPLIERS.put(key, dec);
                            decoders.put(key, dec);
                            encoders.put(key, enc);
                            found = true;
                        }
                    }
                }
                if (!found) {
                    decoders.put(key, decs.get(0));
                    encoders.put(key, encs.get(0));
                }
            }
        }
    }
}
