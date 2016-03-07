package com.dank.hook;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

public abstract class RSMember implements Comparable<RSMember> {
    
    public final String owner, name, desc, mnemonic;

    public int multiplier = 0;

    protected RSMember(String owner, String name, String desc, String mnemonic) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.mnemonic = mnemonic;
    }



    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }
    // Returns a list of incompatibility with its specifications for logging, null if no problems (or an empty list)
    public List<String> verify(MemberSpec spec) {
        return null;
    }


    public boolean matches(FieldInsnNode fin) {
        return equals(fin.owner,fin.name,fin.desc);
    }

    public boolean equals(String owner, String name, String desc) {
        return this.owner.equals(owner) && this.name.equals(name) && this.desc.equals(desc);
    }

    public boolean equals(MethodInsnNode mn) {
        return equals(mn.owner,mn.name,mn.desc);
    }

    public boolean equals(RSMember o) {
        if(o == null) return false;
        return o.owner.equals(this.owner) &&
               o.name.equals(this.name) &&
               o.desc.equals(this.desc) &&
               o.mnemonic.equals(this.mnemonic);
    }

    @Override
    public int compareTo(RSMember o) {
        return  o.owner.compareTo(owner) + o.name.compareTo(name) + o.desc.compareTo(desc) + o.mnemonic.compareTo(mnemonic);
    }

    public boolean isField() {
        return desc.startsWith("L");
    }

    public boolean isMethod() {
        return desc.startsWith("(");
    }

    public String key() {
        return owner + "." + name + desc;
    }

}
