package com.dank.analysis.impl.landscape;

import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;

//No instance methods as of r111
public class LandscapeTile extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	if(cn.access!=49)
    		return null;
    	if(!cn.superName.equals(Hook.NODE.getInternalName()))
    		return null;
    	int check=0;
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		if(fn.desc.equals("L"+Hook.ITEM_PILE.getInternalName()+";"))
    			check++;
    		if(fn.desc.equals("L"+Hook.TILE_DECORATION_STUB.getInternalName()+";"))
    			check++;
    		if(fn.desc.equals("L"+Hook.BOUNDARY_STUB.getInternalName()+";"))
    			check++;
    		if(fn.desc.equals("L"+Hook.BOUNDARY_DECORATION_STUB.getInternalName()+";"))
    			check++;
    	}
    	if(check==4)
    		return new ClassSpec(Hook.LANDSCAPE_TILE, cn);
        return null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (Modifier.isStatic(fn.access)) continue;
            if (fn.desc.equals(Hook.ENTITY_MARKER.getInternalArrayDesc())) {
                Hook.LANDSCAPE_TILE.put(new RSField(fn, "entityMarkers"));
            } else if (fn.desc.equals(Hook.TILE_DECORATION_STUB.getInternalDesc())) {
                Hook.LANDSCAPE_TILE.put(new RSField(fn, "tileDecorationStub"));
            } else if (fn.desc.equals(Hook.BOUNDARY_DECORATION_STUB.getInternalDesc())) {
                Hook.LANDSCAPE_TILE.put(new RSField(fn, "boundaryDecorationStub"));
            } else if (fn.desc.equals(Hook.BOUNDARY_STUB.getInternalDesc())) {
                Hook.LANDSCAPE_TILE.put(new RSField(fn, "boundaryStub"));
            } else if (fn.desc.equals(Hook.ITEM_PILE.getInternalDesc())) {
                Hook.LANDSCAPE_TILE.put(new RSField(fn, "itemPile"));
            }
        }
        final MethodNode mn = cn.getMethod("<init>", "(III)V");
        if (mn == null) throw new RuntimeException("errrr?");
        Hook.LANDSCAPE_TILE.put(new RSField(load(mn, Opcodes.ILOAD, 1), "floorLevel"));
        Hook.LANDSCAPE_TILE.put(new RSField(load(mn, Opcodes.ILOAD, 2), "regionX"));
        Hook.LANDSCAPE_TILE.put(new RSField(load(mn, Opcodes.ILOAD, 3), "regionY"));
    }

    private FieldInsnNode load(final MethodNode mn, final int opcode, final int index) {
        for (final AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof VarInsnNode) {
                final VarInsnNode vin = (VarInsnNode) ain;
                if (vin.var == index && vin.opcode() == opcode) {
                    AbstractInsnNode dog = vin;
                    for (int i = 0; i < 7; i++) {
                        if (dog == null) break;
                        if (dog.opcode() == Opcodes.PUTFIELD) {
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
