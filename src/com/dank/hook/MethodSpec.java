package com.dank.hook;

import java.util.List;

public class MethodSpec implements MemberVerify {

    Hook owner;
    String desc;
    boolean stat;

    public MethodSpec(Hook owner, String desc) { //For non-static fields
        this.owner = owner;
        this.desc = desc;
        this.stat = false;
    }

    public MethodSpec(String desc) { //For static fields
        this.owner = null;
        this.desc = desc;
        this.stat = true;
    }

    public boolean isStatic() {
        return stat;
    }

    public Hook getOwner() {
        return owner;
    }

    @Override
    public List<String> verify(RSMember member) {
        return null;
    }

}
