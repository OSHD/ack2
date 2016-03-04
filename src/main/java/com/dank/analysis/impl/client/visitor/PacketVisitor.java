package com.dank.analysis.impl.client.visitor;

import static org.objectweb.asm.Opcodes.GETSTATIC;

import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.DankEngine;
import com.dank.analysis.Analyser;

public class PacketVisitor extends Analyser {

    static int[] packets = new int[256];

    @Override
    public ClassSpec specify(ClassNode cn) {



        MethodNode con = cn.getMethodByName("<clinit>");
        if(con == null) return null;
        InsnList stack = con.instructions;
        if(stack.size() < (2 + 1 + 256 * 4)) return null;
        AbstractInsnNode field = stack.getLast().previous();
        if(field.opcode() != Opcodes.PUTSTATIC) return null;
        FieldInsnNode target = (FieldInsnNode) field;


        for(AbstractInsnNode ain : stack.toArray()) {
            if(ain.opcode() == Opcodes.NEWARRAY) {
                AbstractInsnNode size = ain.previous();
                if(size.opcode() == Opcodes.SIPUSH) {
                    IntInsnNode iin = (IntInsnNode) size;
                    if(iin.operand == 256) {

                        if(!printPackets(stack)) return null;

                        System.out.println("Packet Sizes:" + target.owner + "#" + target.name);

                    }
                }
            }
        }

        FieldInsnNode packetId = null;

        out:
        for(ClassNode cn0 : DankEngine.classPath.getClasses()) {
            for(MethodNode mn : cn0.methods) {
                if(!Modifier.isStatic(mn.access)) continue;
                for(AbstractInsnNode ain : mn.instructions.toArray()) {
                    if(ain.opcode() != GETSTATIC) continue;
                    FieldInsnNode fin = (FieldInsnNode) ain;
                    if(!fin.owner.equals(target.owner)) continue;
                    if(!fin.name.equals(target.name)) continue;
                    AbstractInsnNode next=ain;
                    while((next=next.next())!=null) {
                        //find the field in which requests the value of the element (packetId)
                        if(next.opcode() == GETSTATIC) {
                            packetId = (FieldInsnNode) next;
                            break out;
                        }
                    }
                }
            }
        }

        if(packetId != null) {
            System.out.println("PacketId: " + packetId.owner + "#" + packetId.name);
            findPackets(packetId);

        } else {
            System.out.println("No packet id");
        }






        return null;

    }

    static void findPackets(FieldInsnNode packId) {
        for (ClassNode cn0 : DankEngine.classPath.getClasses()) {
            for (MethodNode mn : cn0.methods) {
                if (!Modifier.isStatic(mn.access)) continue;
                InsnList stack = mn.instructions;

                out:
                for (AbstractInsnNode ain : stack.toArray()) {

                    // if(PACKET_ID == VAL)
                    //  [get ldc multi] [push]   [compare] 5 operation statement
                    if(ain.opcode() >= 154 && ain.opcode() <= 164) { // ijump
                        int base = stack.indexOf(ain);

                        if(base < 4) continue;

                        for(int i = -4; i < 0; i++) {
                            //get if the statement
                            AbstractInsnNode node = stack.get(base+i);
                            if(node.opcode() == GETSTATIC) {
                                FieldInsnNode fin = (FieldInsnNode) node;
                                if(fin.owner.equals(packId.owner) && fin.name.equals(packId.name)) {
                                    break;
                                }
                            }
                            continue out; //did not pass test
                        }

                        Number pcase = null;

                        Number num = extractNumber(stack.get(base-4)); //First of the statement
                        if(num != null && num.intValue() > 0 && num.intValue() < 256) {
                            pcase = num;
                        } else {
                            num = extractNumber(stack.get(base-1)); //Right before the compare
                            if(num != null && num.intValue() > 0 && num.intValue() < 256) {
                                pcase = num;
                            }
                        }



                        if(pcase == null) continue out;


                        System.out.println("Packet Case:" + pcase + "(" + packets[pcase.intValue()] + ")");

                    }
                }
            }

        }
    }

    public static Integer extractNumber(AbstractInsnNode ain) { //TODO longs?
        if(ain.opcode() >= Opcodes.ICONST_M1 && ain.opcode() <= Opcodes.ICONST_5) {
            return ain.opcode()-3;
        } else if(ain instanceof IntInsnNode) {
            return ((IntInsnNode) ain).operand;
        } else if(ain instanceof LdcInsnNode) {
            Object o = ((LdcInsnNode)ain).cst;
            if(o instanceof Integer) return (Integer) o;
        }
        return null;
    }

    public static boolean printPackets(InsnList stack) {

        System.out.print("    |");
        for(int i = 0; i < 16;i++) {
            String raw = String.valueOf(i);
            if(raw.length()==1) raw += " ";
            // raw += "";
            System.out.print(raw + "|");
        }
        System.out.println();

        int packets = 0;

        for(int i = 0; i < 256; i++) {

            AbstractInsnNode psize = stack.get((2 + i * 4 + 3)-1);

            if(!AbstractInsnNode.isNumber(psize.opcode())) {
                System.out.println("\n\n");
                return false;
            }

            PacketVisitor.packets[i] = extractNumber(psize);

            String raw = psize.toString();
            if(!raw.equals("0"))     packets++;
            if(raw.equals("-1")) raw = "VB"; // small
            if(raw.equals("-2")) raw = "VS"; // large
            if(raw.equals("0"))  raw = "E";  // empty
            if(raw.length()==1)  raw += " ";

            if((i%16)==0) {
                System.out.print(( (i/16)) + (((i/16)<10) ? " " : "") + ": |");
            }
            System.out.print(raw + "|");

            if((i+1)%16==0) {
                //  System.out.print((i/16) + ":");
                System.out.println();
            }


        }

        System.out.println();

        System.out.println("Packets:" + packets);



        return true;

    }

    @Override
    public void evaluate(ClassNode cn) {

    }

}
