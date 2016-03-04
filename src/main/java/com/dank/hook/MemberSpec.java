package com.dank.hook;

// Used to verify members with there expected specifications (owner,desc,access, ect.)
public class MemberSpec {

    JamHook ref; // The owner in which this member is expected from, resolved once the hook is loaded.
    String desc; // The expected description of the member. ? marks its to be resolved later

    final String name; // The external name of this member
    final int type; // The expected type of the member
    final boolean isStatic;

    public static final int TYPE_FIELD  = 1;
    public static final int TYPE_METHOD = 2;

    MemberSpec(String name, String desc, int type) {
        this(name,desc,type,false);
    }

    MemberSpec(String name, String desc, int type, boolean isStatic) {
        this.name  = name;
        this.desc  = desc;
        this.type  = type;
        this.isStatic = isStatic;
    }

    /**
     * Transform future types into types.
     *
     * EG:
     * Node = A (A being the internal rs-class name for the given revision)
     * Renderable = B
     * ObjectDef = C
     *
     * Unresolved Descriptor: (?Node;?Renderable;)?ObjectDef;
     * Resolved Descriptor: (LA;LB;)LC;
     *
     */
    public void resolve() {
        String desc = this.desc;
        char[] chars = desc.toCharArray();
        for(int i = 0; i < chars.length; i++) {
            if(chars[i] == '?') {
                int head = i;
                while (chars[i++] != ';'); // '?' + lookUp + ';'
                int tail = i;
                String lookUp = desc.substring(head+1,tail-1);
                JamHook hook = JamHook.forName(lookUp);
                if(hook == null)
                    throw new Error("Undefined target type:" + lookUp);
                String def = hook.getInternalDesc();
                desc = desc.replace(futureDesc(lookUp),def); //Replace generic type with the defined type, throughout the descriptor.
                chars = desc.toCharArray();
                i = head + def.length() - 1; // Continue at the end of the defined
            }
        }
        this.desc = desc;
    }

    public void resolveRef(JamHook ref) {
        this.ref = ref;
    }

    public static String futureDesc(String type) {
        return '?' + type + ";";
    }

    public boolean isField() {
        return type == TYPE_FIELD;
    }

    public boolean isMethod() {
        return type == TYPE_METHOD;
    }

    public boolean isStatic() {
        return ref == null;
    }

    public String getDescriptor() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public String simpleType() {
        switch (type) {
            case TYPE_FIELD: return "Field";
            case TYPE_METHOD: return "Method";
            default:
                return null;
        }
    }

}