package com.dank.analysis.impl.landscape;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;

/**
 * Project: DankWise
 * Time: 19:53
 * Date: 12-02-2015
 * Created by Dogerina.
 */
public class Landscape extends Analyser implements Opcodes {

    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.name.equals(Hook.LANDSCAPE.getInternalName()) ? new ClassSpec(Hook.LANDSCAPE, cn) : null;
    }

    @Override
    public void evaluate(ClassNode cn) {
        for (final FieldNode fn : cn.fields) {
            if (!Modifier.isStatic(fn.access)) {
                if (fn.desc.startsWith("[[[L")) {
                    Hook.LANDSCAPE_TILE.setInternalName(fn.type());
                    Hook.LANDSCAPE.put(new RSField(fn, "tiles"));
                } else if (fn.desc.startsWith("[L")) {
                    Hook.LANDSCAPE.put(new RSField(fn, "tempEntityMarkers"));
                }
            } else {
                if(fn.desc.equals("[[[[Z")) {
                    Hook.LANDSCAPE.put(new RSField(fn, "visibilityMap"));
                }
            }
        }
        String penis = Hook.ENTITY.getInternalName();
        if (penis == null) throw new RuntimeException("DEPENDENCY BROKE");
        penis = "L" + penis + ";";
        for (final MethodNode mn : cn.methods) {
            if (mn.desc.endsWith("Z") && mn.desc.contains("(IIIIIII") && mn.desc.contains("ZI")) {
                final FieldInsnNode confirm = load(mn, ILOAD, 1, Hook.ENTITY_MARKER);
                if (confirm != null) {
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 1, Hook.ENTITY_MARKER), "floorLevel"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 2, Hook.ENTITY_MARKER), "regionX"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 3, Hook.ENTITY_MARKER), "regionY"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 4, Hook.ENTITY_MARKER), "maxX"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 5, Hook.ENTITY_MARKER), "maxY"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 6, Hook.ENTITY_MARKER), "strictX"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 7, Hook.ENTITY_MARKER), "strictY"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 8, Hook.ENTITY_MARKER), "height"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ALOAD, 9, Hook.ENTITY_MARKER), "entity"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 10, Hook.ENTITY_MARKER), "orientation"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 12, Hook.ENTITY_MARKER), "uid"));
                    Hook.ENTITY_MARKER.put(new RSField(load(mn, ILOAD, 13, Hook.ENTITY_MARKER), "config"));
                    Hook.LANDSCAPE.put(new RSMethod(mn, "addEntityMarker"));
                }
            } else if (mn.desc.startsWith("(IIIIL") && mn.desc.length() > 10 && mn.desc.endsWith(";II)V")) {
                final FieldInsnNode confirm = load(mn, ILOAD, 2, Hook.TILE_STUB);
                if (confirm != null) {
                    Hook.TILE_STUB.put(new RSField(load(mn, ILOAD, 2, Hook.TILE_STUB), "strictX"));
                    Hook.TILE_STUB.put(new RSField(load(mn, ILOAD, 3, Hook.TILE_STUB), "strictY"));
                    Hook.TILE_STUB.put(new RSField(load(mn, ILOAD, 4, Hook.TILE_STUB), "height"));
                    Hook.TILE_STUB.put(new RSField(load(mn, ALOAD, 5, Hook.TILE_STUB), "entity"));
                    Hook.TILE_STUB.put(new RSField(load(mn, ILOAD, 6, Hook.TILE_STUB), "uid"));
                    Hook.TILE_STUB.put(new RSField(load(mn, ILOAD, 7, Hook.TILE_STUB), "config"));
                    Hook.LANDSCAPE.put(new RSMethod(mn, "addTileDecoration"));
                }
            } else if (mn.desc.contains(penis + "I" + penis + penis)) {
                final FieldInsnNode confirm = load(mn, ILOAD, 2, Hook.ITEM_PILE);
                if (confirm != null) {
                    Hook.ITEM_PILE.put(new RSField(load(mn, ILOAD, 2, Hook.ITEM_PILE), "strictX"));
                    Hook.ITEM_PILE.put(new RSField(load(mn, ILOAD, 3, Hook.ITEM_PILE), "strictY"));
                    Hook.ITEM_PILE.put(new RSField(load(mn, ILOAD, 4, Hook.ITEM_PILE), "counterHeight"));
                    Hook.ITEM_PILE.put(new RSField(load(mn, ILOAD, 6, Hook.ITEM_PILE), "uid"));
                    Hook.ITEM_PILE.put(new RSField(load(mn, ILOAD, 10, Hook.ITEM_PILE), "height"));
                    Hook.ITEM_PILE.put(new RSField(load(mn, ALOAD, 5, Hook.ITEM_PILE), "bottom"));
                    Hook.ITEM_PILE.put(new RSField(load(mn, ALOAD, 7, Hook.ITEM_PILE), "middle"));
                    Hook.ITEM_PILE.put(new RSField(load(mn, ALOAD, 8, Hook.ITEM_PILE), "top"));
                    Hook.LANDSCAPE.put(new RSMethod(mn, "addItemPile"));
                }
            } else if (mn.desc.contains("IIIL") && mn.desc.contains(";IIII") && mn.desc.length() > 10 && mn.desc.endsWith("V")) {
                final FieldInsnNode check = load(mn, ILOAD, 4, Hook.BOUNDARY_STUB);
                if (check != null) {
                    Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 2, Hook.BOUNDARY_STUB), "strictX"));
                    Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 3, Hook.BOUNDARY_STUB), "strictY"));
                    Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 4, Hook.BOUNDARY_STUB), "height"));
                    Hook.BOUNDARY_STUB.put(new RSField(load(mn, ALOAD, 5, Hook.BOUNDARY_STUB), "entityA"));
                    Hook.BOUNDARY_STUB.put(new RSField(load(mn, ALOAD, 6, Hook.BOUNDARY_STUB), "entityB"));
                    Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 7, Hook.BOUNDARY_STUB), "orientationA"));
                    Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 8, Hook.BOUNDARY_STUB), "orientationB"));
                    Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 9, Hook.BOUNDARY_STUB), "uid"));
                    Hook.BOUNDARY_STUB.put(new RSField(load(mn, ILOAD, 10, Hook.BOUNDARY_STUB), "config"));
                    Hook.LANDSCAPE.put(new RSMethod(mn, "addBoundary"));
                } else {
                    final FieldInsnNode confirm = load(mn, ILOAD, 2, Hook.BOUNDARY_DECORATION_STUB);
                    if (confirm != null) {
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 2, Hook.BOUNDARY_DECORATION_STUB), "strictX"));
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 3, Hook.BOUNDARY_DECORATION_STUB), "strictY"));
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 4, Hook.BOUNDARY_DECORATION_STUB), "height"));
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ALOAD, 5, Hook.BOUNDARY_DECORATION_STUB), "entityA"));
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ALOAD, 6, Hook.BOUNDARY_DECORATION_STUB), "entityB"));
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 7, Hook.BOUNDARY_DECORATION_STUB), "orientationA"));
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 8, Hook.BOUNDARY_DECORATION_STUB), "orientationB"));
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 9, Hook.BOUNDARY_DECORATION_STUB), "insetX"));
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 10, Hook.BOUNDARY_DECORATION_STUB), "insetY"));
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 11, Hook.BOUNDARY_DECORATION_STUB), "uid"));
                        Hook.BOUNDARY_DECORATION_STUB.put(new RSField(load(mn, ILOAD, 12, Hook.BOUNDARY_DECORATION_STUB), "config"));
                        Hook.LANDSCAPE.put(new RSMethod(mn, "addBoundaryDecoration"));
                    }
                }
            } else if(mn.desc.startsWith("(IIII") && mn.desc.endsWith("IIZ)Z")) {
                Hook.LANDSCAPE.put(new RSMethod(mn, "addTempEntity"));
            } else if(!Modifier.isStatic(mn.access) && mn.desc.startsWith("(IIIII") && mn.desc.endsWith(")V") && mn.desc.length() <= 7 + 3) {
                Hook.LANDSCAPE.put(new RSMethod(mn, "render"));
            }
        }
    }

    private FieldInsnNode load(final MethodNode mn, final int opcode, final int index, final Hook owner) {
        for (final AbstractInsnNode ain : mn.instructions.toArray()) {
            if (ain instanceof VarInsnNode) {
                final VarInsnNode vin = (VarInsnNode) ain;
                if (vin.var == index && vin.opcode() == opcode) {
                    AbstractInsnNode dog = vin;
                    for (int i = 0; i < 7; i++) {
                        if (dog == null) break;
                        if (dog.opcode() == PUTFIELD && ((FieldInsnNode) dog).owner.equals(owner.getInternalName())) {
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
