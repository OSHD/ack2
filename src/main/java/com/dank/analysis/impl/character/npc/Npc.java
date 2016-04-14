package com.dank.analysis.impl.character.npc;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

//All fields identified as of r111
public class Npc extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
		if(cn.access!=49)
			return null;
		if(!cn.superName.equals(Hook.CHARACTER.getInternalName()))
			return null;
		boolean isnpc=true;
		for(FieldNode fn : cn.fields){
			if(fn.isStatic())
				continue;
			if(fn.desc.equals("I"))
				isnpc=false;
		}
		if(isnpc)
			return new ClassSpec(Hook.NPC, cn);
		return null;
    }
    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
        	if(fn.isStatic())
        		continue;
           	if(fn.desc.equals("L"+Hook.NPC_DEFINITION.getInternalName()+";"))
           		Hook.NPC.put(new RSField(fn, "definition"));
        }
    }
}
