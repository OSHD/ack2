package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.BlockVisitor;
import org.objectweb.asm.commons.cfg.FlowVisitor;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//All fields and methods identified as of r111
public class Messages extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return (cn.fieldCount(String.class, true) == 3 && cn.fieldCount(int.class, true) == 3) ?
                new ClassSpec(Hook.MESSAGES, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	MethodNode setMessage=null;
        for (MethodNode methodNode : cn.methods) {
            if (!Modifier.isStatic(methodNode.access) && methodNode.desc.contains("Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;") &&
                    !methodNode.name.equals("<init>")) {
            	Hook.MESSAGES.put(new RSField(load(methodNode, Opcodes.ILOAD, 1), "type"));
                Hook.MESSAGES.put(new RSField(load(methodNode, Opcodes.ALOAD, 2), "sender"));
                Hook.MESSAGES.put(new RSField(load(methodNode, Opcodes.ALOAD, 3), "channel"));
                Hook.MESSAGES.put(new RSField(load(methodNode, Opcodes.ALOAD, 4), "message"));
                hookCycle(methodNode);
                setMessage=methodNode;
                Hook.MESSAGES.put(new RSMethod(setMessage, "setMessage"));
            }
        }
        if(setMessage!=null){
	        for(FieldNode fn : cn.fields){
	        	if(fn.isStatic())
	        		continue;
	        	FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
	        	if(fn.desc.equals("I")){
	        		for(MethodData md : fd.referencedFrom){
	        			if(!md.bytecodeMethod.isStatic())
	        				continue;
	        			if(new Wildcard("(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;?)V").matches(md.METHOD_DESC)){
	        				Hook.MESSAGES.put(new RSField(fd.bytecodeField, "index"));
	        				break;
	        			}
	        		}
	        	}
	        }
        }
    }
    public void hookCycle(MethodNode methodNode) {
        Map<String, String> hooks = new HashMap<String, String>();
        Hook.MESSAGES.getIdentifiedSet().stream().filter(test -> test.isField()).forEach(test -> {
            hooks.put(test.owner, test.name);
        });
        FlowVisitor fv = new FlowVisitor();
        fv.accept(methodNode);
        for (BasicBlock block : fv.blocks) {
            block.accept(new BlockVisitor() {
                @Override
                public boolean validate() {
                    return block.count(Opcodes.GETSTATIC) == 1 && block.count(Opcodes.PUTFIELD) >= 1;

                }
                @Override
                public void visit(BasicBlock block) {
                    FieldInsnNode fin = (FieldInsnNode) block.get(Opcodes.PUTFIELD);
                    if (!hooks.containsValue(fin.name))
                        Hook.MESSAGES.put(new RSField(fin, "cycle"));
                }
            });
        }
    }
    private FieldInsnNode load(final MethodNode mn, final int opcode, final int index) {
        for (final AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof VarInsnNode) {
                final VarInsnNode vin = (VarInsnNode) ain;
                if (vin.var == index && vin.opcode() == opcode) {
                    AbstractInsnNode dog = vin;
                    for (int i = 0; i < 7; i++) {
                        if (dog == null) break;
                        if (dog.opcode() == Opcodes.PUTFIELD && ((FieldInsnNode) dog).owner.equals(Hook.MESSAGES.getInternalName())) {
                            return (FieldInsnNode) dog;
                        }
                        dog = dog.next();
                    }
                }
            }
        }
        return null;
    }
}
