package com.dank.hook;

import com.dank.DankEngine;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Septron
 * @since February 12, 2015
 */
public class RSMethod extends RSMember {

    public RSMethod(final MethodNode fn, final String mnemonic) {
        super(fn.owner.name, fn.name, fn.desc, mnemonic);
    }

    public RSMethod(final MethodMemberNode fn, final String mnemonic) {
        super(fn.owner(), fn.name(), fn.desc(), mnemonic);
    }

    public RSMethod(final MethodInsnNode fin, final String mnemonic) {
        super(fin.owner, fin.name, fin.desc, mnemonic);
    }

    public boolean isStatic() {
        int access = DankEngine.lookupMethod(owner, name, desc).access;
        return Modifier.isStatic(access);
    }

    public List<String> verify(MemberSpec spec) {


        List<String> problems = new ArrayList<>(4);

        if (!spec.isMethod()) {
            problems.add("Bad member type:(expected:" + spec.simpleType() + ",got:Method)");
        }

        String desc0 = spec.getDescriptor();
        String header = desc0.substring(1, desc0.indexOf(')')); // Get the arguments
        String ret = desc0.substring(desc0.indexOf(')') + 1, desc0.length()); // Get the return
        if ( (!this.desc.startsWith(header)) || (!this.desc.endsWith(ret)) ) {
            problems.add("Bad descriptor:(expected:[args=" + header + ",ret=" + ret + "]" + ",got:" + this.desc + ")");
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
        return false;
    }

    public boolean isMethod() {
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder specbuilder = new StringBuilder();
        specbuilder.append("  |→ ").append(mnemonic);
        while (specbuilder.length() < 34) {
            specbuilder.append('.');
        }
        return specbuilder.append(owner).append('.').append(name).append(desc).append("\n").toString();
    }

    public String key() {
        return owner + "." + name + desc;
    }
}
