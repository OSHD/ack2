package org.objectweb.asm.commons.util;

import com.dank.asm.ClassPath;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class CallVisitor {
    private void check(MethodWrapper wrap,List<MethodWrapper> used){
        if(!wrap.owner.contains("java") && !used.contains(wrap)){
            used.add(wrap);
        }
    }
    private boolean hasMethod(ClassNode node, String name, String desc){
        ListIterator<MethodNode> mnIt = node.methods.listIterator();
        while(mnIt.hasNext()){
            MethodNode mn = mnIt.next();
            if(mn.name.equals(name) && mn.desc.equals(desc)){
                return true;
            }

        }
        return false;
    }
    public List<MethodWrapper> getRedundantMethods(ClassPath cp){
        List<MethodWrapper> used = new ArrayList<MethodWrapper>();
        List<MethodWrapper> all = new ArrayList<MethodWrapper>();
        for(ClassNode node : cp.getClasses()){
            Iterator<MethodNode> mnIt;
            mnIt = node.methods.listIterator();
            while(mnIt.hasNext()) {
                MethodNode mn = mnIt.next();
                MethodWrapper wrap = new MethodWrapper(node.name, mn.name, mn.desc);
                if (!all.contains(wrap))
                    all.add(wrap);

                if (wrap.name.contains("init"))
                    check(wrap, used);
                if (Modifier.isAbstract(mn.access))
                    check(wrap, used);
                String classSuper = node.superName;
                while (!classSuper.equals("java/lang/Object")) {
                    ClassNode superNode;
                    if (classSuper.contains("java")) {
                        superNode = new ClassNode();
                        try {
                            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                            node.accept(cw);
                            byte[] b = cw.toByteArray();
                            ClassReader cr = new ClassReader(b);
                            cr.accept(superNode, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        superNode = cp.get(classSuper);
                    }
                    if (hasMethod(superNode, mn.name, mn.desc)) {
                        check(new MethodWrapper(classSuper, wrap.name, wrap.desc), used);
                        check(wrap, used);
                        break;
                    }
                    classSuper = superNode.superName;

                }

                Iterator<AbstractInsnNode> instIt = mn.instructions.iterator();
                while (instIt.hasNext()) {
                    AbstractInsnNode insn = (AbstractInsnNode) instIt.next();
                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode mns = (MethodInsnNode) insn;
                        if (!mns.owner.contains("java") && !mns.name.contains("init")) {
                            if (hasMethod(cp.get(mns.owner), mns.name, mns.desc)) {
                                check(new MethodWrapper(mns.owner, mns.name, mns.desc), used);
                            } else {
                                classSuper = cp.get(mns.owner).superName;
                                while (!classSuper.contains("java")) {
                                    ClassNode cn = cp.get(classSuper);
                                    if (hasMethod(cn, mns.name, mns.desc)) {
                                        check(new MethodWrapper(cn.name, mns.name, mns.desc), used);
                                        break;
                                    }
                                    classSuper = cn.superName;

                                }
                            }

                        }
                    }
                }
            }
            if(node.interfaces.size() > 0){
                Iterator<String> itIt = node.interfaces.listIterator();
                while(itIt.hasNext()){
                    String name = (String)itIt.next();
                    ClassNode itNode;
                    if(name.contains("java")){
                        itNode = new ClassNode();
                        try {
                            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                            node.accept(cw);
                            byte[] b = cw.toByteArray();
                            ClassReader cr = new ClassReader(b);
                            cr.accept(itNode,0);
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        itNode = cp.get(name);
                    }


                    mnIt = itNode.methods.listIterator();
                    while(mnIt.hasNext()){
                        MethodNode mn = mnIt.next();
                        check(new MethodWrapper(node.name, mn.name, mn.desc), used);
                    }

                }
            }
            try{Thread.sleep(1);}catch(Exception e){}
        }
        List<MethodWrapper> toRemove = new ArrayList<MethodWrapper>();
        for(MethodWrapper wrap : all){
            if(!used.contains(wrap)) {

                toRemove.add(wrap);
            }
        }
        System.out.println("Removed "+Integer.toString(toRemove.size())+"/"+Integer.toString(all.size())+" redundant methods!");
        return toRemove;
    }
    public ClassPath refactor(ClassPath cp){
        List<MethodWrapper>remove = getRedundantMethods(cp);
        List<MethodNode> toRemove = new ArrayList<MethodNode>();
        for(MethodWrapper wrap : remove){
            for(ClassNode node : cp.getClasses()){
                if(!wrap.owner.equals(node.name))
                    continue;
                for (MethodNode mn : (Iterable<MethodNode>) node.methods) {
                    if(mn.name.equals(wrap.name) && mn.desc.equals(wrap.desc))
                        toRemove.add(mn);
                }
                for(MethodNode mn : toRemove)
                    node.methods.remove(mn);
            }
        }
        return cp;
    }
    public CallVisitor(final ClassPath cp) {
    	refactor(cp);
    }
}
