package com.dank.analysis.impl.misc;

import java.io.RandomAccessFile;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

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

public class FileOnDisk extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	if(cn.access!=49)
    		return null;
    	if(!cn.ownerless())
    		return null;
        return cn.fieldCount(RandomAccessFile.class) == 1 && cn.fieldCount(long.class) == 2
                ? new ClassSpec(Hook.FILE_ON_DISK, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(MethodNode mn : cn.methods){
    		if(mn.isStatic())
    			continue;
    		if(new Wildcard("([BII?)V").matches(mn.desc)){
				Hook.FILE_ON_DISK.put(new RSMethod(mn, "write"));
    		}
    		else if(new Wildcard("([BII?)I").matches(mn.desc)){
				Hook.FILE_ON_DISK.put(new RSMethod(mn, "read"));
    		}
    		else if(new Wildcard("(J)V").matches(mn.desc)){
				Hook.FILE_ON_DISK.put(new RSMethod(mn, "seek"));
    		}
    		else if(new Wildcard("(?)J").matches(mn.desc)){
				Hook.FILE_ON_DISK.put(new RSMethod(mn, "getLength"));
    		}
    		else if(new Wildcard("(?)V").matches(mn.desc)){
				Hook.FILE_ON_DISK.put(new RSMethod(mn, "close"));
    		}
    	}
        for (FieldNode fn : cn.fields) {
        	FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
            if (Modifier.isStatic(fn.access))
                continue;
            if(fn.desc.equals("Ljava/io/RandomAccessFile;")){
            	Hook.FILE_ON_DISK.put(new RSField(fn, "file"));
            }
            if(fn.desc.equals("J")){
            	boolean found=false;
            	for(MethodData md : fd.referencedFrom){
            		if(new Wildcard("(J)V").matches(md.METHOD_DESC)){
            			found=true;
            		}
            	}
            	if(found)
                	Hook.FILE_ON_DISK.put(new RSField(fn, "position"));
            	else
            		Hook.FILE_ON_DISK.put(new RSField(fn, "length"));
            }
        }
    }
}
