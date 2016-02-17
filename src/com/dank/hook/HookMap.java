package com.dank.hook;

import org.objectweb.asm.commons.Remapper;

public class HookMap extends Remapper {

    public static HookMap INSTANCE = new HookMap();

    @Override
    public String map(String typeName) {
        if(typeName.startsWith("java")) return typeName;
        for(Hook h : Hook.values()) {
            if(h.getInternalName() == null) continue;
            if(h.getInternalName().equals(typeName)) {
                return h.getDefinedName();
            }
        }
        throw new Error("Non resolved type: " + typeName);
    }

}
