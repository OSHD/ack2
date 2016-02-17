package com.dank.hook;

import com.dank.DankEngine;
import com.dank.util.MemberKey;
import org.objectweb.asm.commons.cfg.tree.node.ReferenceNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Septron
 * @since February 12, 2015
 */
public class RSField extends RSMember {

    private int multiplier = 0;

    public RSField(MemberKey key, String name) {
        super(key.owner, key.name, key.desc, name);
    }

    public RSField(final FieldNode fn, final String mnemonic) {
        super(fn.owner.name, fn.name, fn.desc, mnemonic);
    }

    public RSField(final ReferenceNode fn, final String mnemonic) {
        super(fn.owner(), fn.name(), fn.desc(), mnemonic);
    }

    public RSField(final FieldInsnNode fin, final String mnemonic) {
        super(fin.owner, fin.name, fin.desc, mnemonic);
    }

    public RSField(final String key, final String desc, final String mnemonic) {
        super(key.split("\\.")[0], key.split("\\.")[1], desc, mnemonic);
    }

    public RSField(String key, FieldInsnNode value) {
        super(value.owner, value.name, value.desc, key);
    }

    public boolean isStatic() {
        int access = DankEngine.lookupField(owner, name, desc).access;
        return Modifier.isStatic(access);
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public List<String> verify(MemberSpec spec) {

        List<String> problems = new ArrayList<>(4);

        if (!spec.isField()) {
            problems.add("Bad member type:(expected:" + spec.simpleType() + ",got:Field)");
        }

        if (!this.desc.equals(spec.getDescriptor())) {
            problems.add("Bad descriptor:(expected:" + spec.getDescriptor() + ",got:" + this.desc + ")");
        }

        if (!spec.isStatic()) { //Static members have no definite owner
            String owner_name = spec.ref.getInternalName();
            if (!owner_name.equals(this.owner)) {
                problems.add("Bad owner:(expected:" + owner_name + ",got:" + this.owner + ")");
            }
        }

        if (spec.isStatic() != this.isStatic()) { //Expected equal access (static members for static specs)
            problems.add("Bad access:(expected:" + spec.isStatic() + ",got:" + this.isStatic() + ")");
        }

        return problems;

    }

    public boolean isField() {
        return true;
    }

    public boolean isMethod() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder specbuilder = new StringBuilder();
        specbuilder.append("  |→ ").append(mnemonic);
        while (specbuilder.length() < 34) {
            specbuilder.append('.');
        }
        return specbuilder.append(owner).append('.').append(name).append("\n").toString();
    }
}
