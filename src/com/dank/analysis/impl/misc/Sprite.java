package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import jdk.nashorn.internal.codegen.types.Type;
import org.objectweb.asm.tree.*;

import java.awt.*;

import static org.objectweb.asm.Opcodes.*;

public class Sprite extends Analyser {

    @Override
    public ClassSpec specify(ClassNode cn) {
        if(cn.fieldCount("I") != 6) return null;
        if(cn.fieldCount("[I") != 1) return null;
        boolean type_a = false;
        boolean type_b = false;

        for(MethodNode mn : cn.methods) {
            if(!mn.name.equals("<init>")) continue;
            final String desc = mn.desc;
            if(desc.equals(Type.getMethodDescriptor(Void.TYPE,int.class,int.class))) {

                type_a = true;
            } else if(desc.equals(Type.getMethodDescriptor(Void.TYPE,byte[].class, Component.class))) {

                type_b = true;
            }
            if(type_a && type_b) {
                return new ClassSpec(Hook.SPRITE,cn);
            }
        }
        return null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for(MethodNode mn : cn.methods) {
            if(!mn.name.equals("<init>")) continue;
            if(!mn.desc.equals(Type.getMethodDescriptor(Void.TYPE, byte[].class,Component.class))) continue;;

            RSField width = null;
            RSField height = null;

            for(FieldNode fn : cn.fields) {
                if(fn.desc.equals("[I")) {
                    Hook.SPRITE.put(new RSField(fn, "pixels"));
                    break;
                }
            }

            //Find the width/height
            for(AbstractInsnNode ain : mn.instructions.toArray()) {
                if(ain.opcode() == INVOKEVIRTUAL) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    if(min.owner.equals(Type.getInternalName(Image.class))) {
                        if(min.name.equals("getWidth")) {
                            AbstractInsnNode next = min.next();
                            if(next.opcode() != PUTFIELD) continue;
                            FieldInsnNode fin = (FieldInsnNode) next;
                            Hook.SPRITE.put(width = new RSField(fin,"width"));
                        } else if(min.name.equals("getHeight")) {
                            AbstractInsnNode next = min.next();
                            if(next.opcode() != PUTFIELD) continue;
                            FieldInsnNode fin = (FieldInsnNode) next;
                            Hook.SPRITE.put(height = new RSField(fin,"height"));
                        }
                    }
                }
            }

            if(width == null && height == null) return;

            for(AbstractInsnNode ain : mn.instructions.toArray()) {
                if(ain.opcode() == GETFIELD) {

                    AbstractInsnNode next = ain.next();
                    if(next.opcode() != PUTFIELD) continue;
                    FieldInsnNode fin = (FieldInsnNode) next;

                    if(width != null && width.matches((FieldInsnNode)ain)) {
                        Hook.SPRITE.put(new RSField(fin,"maxX"));
                    } else if(height != null && height.matches((FieldInsnNode) ain)) {
                        Hook.SPRITE.put(new RSField(fin,"maxY"));
                    }

                } else if(ain.opcode() == ICONST_0) {

                    AbstractInsnNode next = ain.next();
                    if(next.opcode() != PUTFIELD) continue;
                    FieldInsnNode fin = (FieldInsnNode) next;

                    if(Hook.SPRITE.get("paddingX") == null) {
                        Hook.SPRITE.put(new RSField(fin,"paddingX"));
                    } else if(Hook.SPRITE.get("paddingY") == null) {
                        Hook.SPRITE.put(new RSField(fin,"paddingY"));
                    }

                }

            }

        }
    }
}
