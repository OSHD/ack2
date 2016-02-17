package com.dank.util;

import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class MemberKey {

    public String owner;
    public String name;
    public String desc;

    public MemberKey(String owner, String name, String desc) {
        this.owner = owner;
        this.name  = name;
        this.desc  = desc;
    }

    public MemberKey(FieldInsnNode fin) {
        this(fin.owner,fin.name,fin.desc);
    }

    public MemberKey(MethodInsnNode min) {
        this(min.owner,min.name,min.desc);
    }

    public static String mkKey(String owner,String name,String desc) {
        return owner + "#" + name + "@" + desc;
    }

    public String key() {
        return mkKey(owner, name, desc);
    }

    public boolean equals(String owner, String name, String desc) {
        return this.owner.equals(owner) && this.name.equals(name) && this.desc.equals(desc);
    }

    public boolean equals(MemberKey o) {
        return equals(o.owner,o.name,o.desc);
    }

    boolean cache=false;
    int hash;

    @Override
    public int hashCode() {
        if(cache) return hash;
        hash = key().hashCode();
        cache = true;
        return hash;
    }

    @Override
    public String toString() {
        return key();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof MemberKey)) return false;
        MemberKey k = (MemberKey) o;
        return this.equals(k);
    }

}
