package com.dank.analysis.impl.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.StoreNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

//All fields and methods identified as of r111
public class Varpbit extends Analyser {
    private final List<VarAssignment> vars = new ArrayList<>();
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName.equals(Hook.DUAL_NODE.getInternalName()) && cn.fieldCount() == 3 && cn.fieldCount(int.class) == 3
                ? new ClassSpec(Hook.VARPBIT, cn) : null;
    }
    @Override
    public void evaluate(ClassNode c) {
    	for(MethodNode mn : c.methods){
    		if(mn.isStatic())
    			continue;
    		MethodData md = DynaFlowAnalyzer.getMethod(c.name, mn.name, mn.desc);
    		if(md.referencedFrom.size()>0 && new Wildcard("(L"+Hook.BUFFER.getInternalName()+";?)V").matches(mn.desc)){
                Hook.VARPBIT.put(new RSMethod(mn, "unpackConfig"));
    		}
    		if(md.referencedFrom.size()>0 && new Wildcard("(L"+Hook.BUFFER.getInternalName()+";I?)V").matches(mn.desc)){
                Hook.VARPBIT.put(new RSMethod(mn, "readValues"));
    		}
    	}
        for (final ClassNode cn : super.getClassPath()) {
            for (final MethodNode mn : cn.methods) {
                if (mn.isStatic()) {
                    mn.graph().forEach(b -> {
                        b.tree().accept(new GenericVisitor(c.name));
                    });
                }
            }
        }
        vars.sort(new Comparator<VarAssignment>() {
            @Override
            public int compare(VarAssignment o1, VarAssignment o2) {
                return Integer.compare(o1.istore.var(), o2.istore.var());
            }
        });
        for (final VarAssignment var : vars) {
            if (Hook.VARPBIT.get("varp") == null) {
                Hook.VARPBIT.put(new RSField(var.getfield, "varp"));
            } else if (Hook.VARPBIT.get("lowBit") == null) {
                Hook.VARPBIT.put(new RSField(var.getfield, "lowBit"));
            } else if (Hook.VARPBIT.get("highBit") == null) {
                Hook.VARPBIT.put(new RSField(var.getfield, "highBit"));
            }
        }
    }
    private final class VarAssignment {
        private StoreNode istore;
        private FieldMemberNode getfield;
    }
    private final class GenericVisitor extends NodeVisitor {
        private final String owner;
        private GenericVisitor(String owner) {
            this.owner = owner;
        }
        @Override
        public void visitStore(StoreNode sn) {
            if (sn.opcode() == ISTORE) {
                final FieldMemberNode fmn = (FieldMemberNode) sn.layer(IMUL, GETFIELD);
                if (fmn != null && fmn.owner().equals(owner)) {
                    final VarAssignment var = new VarAssignment();
                    var.istore = sn;
                    var.getfield = fmn;
                    if (canAdd(var)) {
                        vars.add(var);
                    }
                }
            }
        }
        private boolean canAdd(VarAssignment var) {
            for (final VarAssignment var0 : vars) {
                if (var0.getfield.key().equals(var.getfield.key())) {
                    return false;
                }
            }
            return true;
        }
    }
}
