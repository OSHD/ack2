package com.dank.analysis.impl.node;

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

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

//All fields and methods identified as of r111
public class MemCache extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.ownerless() &&
                cn.fieldCount(String.format("L%s;", Hook.DUAL_NODE.getInternalName())) == 1 &&
                cn.fieldCount(String.format("L%s;", Hook.HASHTABLE.getInternalName())) == 1 &&
                cn.fieldCount(int.class) == 2 ?
                new ClassSpec(Hook.MEMCACHE, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(MethodNode mn : cn.methods){
    		MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
    		if(md.referencedFrom.size()>0 && new Wildcard("()V").matches(mn.desc)){
        		Hook.MEMCACHE.put(new RSMethod(mn, "clear"));
    		}
    		if(md.referencedFrom.size()>0 && new Wildcard("(L"+Hook.DUAL_NODE.getInternalName()+";J)V").matches(mn.desc)){
        		Hook.MEMCACHE.put(new RSMethod(mn, "put"));
    		}
    		if(md.referencedFrom.size()>0 && new Wildcard("(J)L"+Hook.DUAL_NODE.getInternalName()+";").matches(mn.desc)){
        		Hook.MEMCACHE.put(new RSMethod(mn, "get"));
    		}
    		if(md.referencedFrom.size()>0 && new Wildcard("(J)V").matches(mn.desc)){
        		Hook.MEMCACHE.put(new RSMethod(mn, "remove"));
    		}
    	}
    	RSMethod clear = (RSMethod)Hook.MEMCACHE.get("clear");
    	if(clear!=null){
    		MethodData md = DynaFlowAnalyzer.getMethod(cn.name, clear.name, clear.desc);
    		List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
					Mask.GETFIELD.describe("I").own(cn.name),
					Mask.PUTFIELD.describe("I").own(cn.name)
					);
			if (pattern != null) {
				FieldInsnNode size = (FieldInsnNode)pattern.get(0);
				FieldInsnNode remaining = (FieldInsnNode)pattern.get(1);
				FieldData sizeData = DynaFlowAnalyzer.getField(size.owner, size.name);
				FieldData remainData = DynaFlowAnalyzer.getField(remaining.owner, remaining.name);
                Hook.MEMCACHE.put(new RSField(sizeData.bytecodeField, "size"));
                Hook.MEMCACHE.put(new RSField(remainData.bytecodeField, "remaining"));
			}
    	}
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		if(fn.desc.equals("L"+Hook.HASHTABLE.getInternalName()+";")){
                Hook.MEMCACHE.put(new RSField(fn, "table"));
    		}
    		if(fn.desc.equals("L"+Hook.QUEUE.getInternalName()+";")){
                Hook.MEMCACHE.put(new RSField(fn, "queue"));
    		}
    		if(fn.desc.equals("L"+Hook.DUAL_NODE.getInternalName()+";")){
                Hook.MEMCACHE.put(new RSField(fn, "head"));
    		}
    	}
    }
}
