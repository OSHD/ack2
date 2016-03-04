package com.dank.util;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.dank.hook.Hook;

/**
 * Project: DankWise
 * Date: 16-02-2015
 * Time: 15:57
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public final class TypeMap extends HashMap<String, Integer> {

    public TypeMap(final List<FieldNode> fields, final boolean ignoreStatics) {
        for (final FieldNode fn : fields) {
            if (!ignoreStatics || !Modifier.isStatic(fn.access)) {
                if (!super.containsKey(fn.desc)) {
                    super.put(fn.desc, 1);
                } else {
                    super.put(fn.desc, super.get(fn.desc) + 1);
                }
            }
        }
    }

    public TypeMap(final List<FieldNode> fields) {
        this(fields, true);
    }

    public TypeMap(final ClassNode cn, final boolean ignoreStatics) {
        this(cn.fields, ignoreStatics);
    }

    public TypeMap(final ClassNode cn) {
        this(cn, true);
    }

    public int count(final String desc) {
        return super.get(desc);
    }

    public int count() {
        return super.size();
    }

    public int count(final Hook container, final int arrayDimensions) {
        if (container.getInternalName() == null) {
            throw new IllegalArgumentException("HookContainer's internal name has not been set yet.");
        }
        return count(container.getInternalArrayDesc(arrayDimensions));
    }

    public int count(final Hook container) {
        return count(container, 0);
    }

    //TODO some sort of builder so we can do new TypeMap(fields).has("I", 5).has("Z", 2).has("J", 1).return()
}
