package com.dank.analysis.impl.model;

import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.query.MemberQuery;
import org.objectweb.asm.commons.cfg.query.NumberQuery;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.analysis.impl.model.visitor.CursorUidsVisitor;
import com.dank.analysis.impl.model.visitor.VertexVisitor;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.util.Filter;

public class StillModel extends Analyser {
    @Override
    public ClassSpec specify(ClassNode node) {
    	if(node.access!=33)
    		return null;
    	if(!node.superName.equals(Hook.ENTITY.getInternalName()))
    		return null;
    	int boolcount=0, intcount=0, intarr=0, dblintarr=0, btyearr=0, shrarr=0;
    	for(FieldNode fn : node.fields){
    		if(fn.isStatic())
    			continue;
    		if(fn.desc.equals("Z"))
    			boolcount++;
    		if(fn.desc.equals("I"))
    			intcount++;
    		if(fn.desc.equals("[I"))
    			intarr++;
    		if(fn.desc.equals("[[I"))
    			dblintarr++;
    		if(fn.desc.equals("[B"))
    			btyearr++;
    		if(fn.desc.equals("[S"))
    			shrarr++;
    	}
    	if(boolcount==1 && intcount==8 && intarr==8 && dblintarr==2 && btyearr==6 && shrarr==11)
    		return new ClassSpec(Hook.STILL_MODEL, node);
    	return null;
    }

    @Override
    public void evaluate(ClassNode cn) {
    }
}
