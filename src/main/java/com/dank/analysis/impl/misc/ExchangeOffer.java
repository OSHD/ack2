package com.dank.analysis.impl.misc;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.commons.cfg.tree.NodeVisitor;
import org.objectweb.asm.commons.cfg.tree.node.FieldMemberNode;
import org.objectweb.asm.commons.cfg.tree.node.MethodMemberNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.asm.Mask;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.Assembly;
import com.marn.asm.FieldData;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;


public class ExchangeOffer extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.fieldCount() == 6 && cn.fieldCount(int.class) == 5 && cn.fieldCount(byte.class) == 1
                ? new ClassSpec(Hook.EXCHANGE_OFFER, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(MethodNode mn : cn.methods){
    		MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
    		if(new Wildcard("(?)I").matches(mn.desc)){
    			List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
    					Mask.GETFIELD.describe("B").own(cn.name),
						Mask.BIPUSH
						);
				if (pattern != null) {
					IntInsnNode num = (IntInsnNode) pattern.get(1);
					if(num.operand==8){
						Hook.EXCHANGE_OFFER.put(new RSMethod(mn, "isCompleted"));
						for(MethodData md2 : md.referencedFrom){
							if(new Wildcard("(L"+Hook.SCRIPT_EVENT.getInternalName()+";I?)V").matches(md2.METHOD_DESC)){
								List<ArrayList<AbstractInsnNode>> patterns = Assembly.findAll(md2.bytecodeMethod,
				    					Mask.SIPUSH,
				    					Mask.GETFIELD.distance(25).own(cn.name)
										);
								if(patterns==null)
									continue;
								for(ArrayList<AbstractInsnNode> pattern2 : patterns){
									IntInsnNode oper = (IntInsnNode)pattern2.get(0);
									FieldInsnNode field = (FieldInsnNode)pattern2.get(1);
									FieldData fd = DynaFlowAnalyzer.getField(field.owner, field.name);
									if(oper.operand==3904)
							            Hook.EXCHANGE_OFFER.put(new RSField(fd.bytecodeField, "itemId"));
									if(oper.operand==3905)
							            Hook.EXCHANGE_OFFER.put(new RSField(fd.bytecodeField, "price"));
									if(oper.operand==3906)
							            Hook.EXCHANGE_OFFER.put(new RSField(fd.bytecodeField, "itemQuantity"));
									if(oper.operand==3907)
							            Hook.EXCHANGE_OFFER.put(new RSField(fd.bytecodeField, "transferred"));
									if(oper.operand==3908)
							            Hook.EXCHANGE_OFFER.put(new RSField(fd.bytecodeField, "spent"));
								}
								break;
							}
						}
					}
					if(num.operand==7)
						Hook.EXCHANGE_OFFER.put(new RSMethod(mn, "getStatus"));
				}
    		}
    	}
        for (FieldNode fn : cn.fields) {
            if (Modifier.isStatic(fn.access) || !fn.desc.equals("B"))
                continue;
            Hook.EXCHANGE_OFFER.put(new RSField(fn, "status"));
        }
    }
}
