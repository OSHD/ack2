package com.dank.analysis.impl.misc;

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

import org.objectweb.asm.tree.*;

import java.util.List;

//All fields and methods identified as of r113
public class PacketBuffer extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.superName(Hook.BUFFER.getInternalName()) && cn.fieldCount() > 1
                ? new ClassSpec(Hook.PACKET_BUFFER, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
        for (FieldNode fn : cn.fields) {
        	if(fn.isStatic())
        		continue;
        	FieldData fd = DynaFlowAnalyzer.getField(cn.name, fn.name);
        	if(fn.desc.equals("I")){
                Hook.PACKET_BUFFER.put(new RSField(fn, "bitOffset"));
                for(MethodData md : fd.referencedFrom){
                	if(new Wildcard("(?)V").matches(md.METHOD_DESC)){
                		List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
            					Mask.BIPUSH.operand(7)
            					);
                		if(pattern!=null){
                            Hook.PACKET_BUFFER.put(new RSMethod(md.bytecodeMethod, "endBitAccess"));
                		}
                		else{
                            Hook.PACKET_BUFFER.put(new RSMethod(md.bytecodeMethod, "startBitAccess"));
                		}
                	}
                	if(new Wildcard("(I?)I").matches(md.METHOD_DESC)){
                		List<AbstractInsnNode> pattern = Assembly.find(md.bytecodeMethod,
            					Mask.GETFIELD.describe("[B")
            					);
                		if(pattern!=null){
                            Hook.PACKET_BUFFER.put(new RSMethod(md.bytecodeMethod, "getBits"));
                		}
                		else{
                            Hook.PACKET_BUFFER.put(new RSMethod(md.bytecodeMethod, "getBitsLeft"));
                		}
                	}
                }
        	}
        	else if(fn.desc.equals("L"+Hook.ISAAC_CIPHER.getInternalName()+";")){
                Hook.PACKET_BUFFER.put(new RSField(fn, "cipher"));
                for(MethodData md : fd.referencedFrom){
                	if(new Wildcard("(?)I").matches(md.METHOD_DESC)){
                        Hook.PACKET_BUFFER.put(new RSMethod(md.bytecodeMethod, "readHeader"));
                	}
                	if(new Wildcard("(I?)V").matches(md.METHOD_DESC)){
                        Hook.PACKET_BUFFER.put(new RSMethod(md.bytecodeMethod, "writeHeader"));
                	}
                	if(new Wildcard("([I?)V").matches(md.METHOD_DESC)){
                        Hook.PACKET_BUFFER.put(new RSMethod(md.bytecodeMethod, "initCipher"));
                	}
                }
        	}
        }
    }
}
