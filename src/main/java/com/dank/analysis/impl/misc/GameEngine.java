package com.dank.analysis.impl.misc;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import com.dank.util.Wildcard;
import com.marn.asm.MethodData;
import com.marn.dynapool.DynaFlowAnalyzer;

//All fields identified as of r112
public class GameEngine extends Analyser {
    @Override
    public ClassSpec specify(ClassNode cn) {
    	if(cn.access!=1057)
    		return null;
    	if(cn.ownerless() || !cn.superName.equals("java/applet/Applet"))
    		return null;
        return cn.fieldCount(boolean.class) == 1 ? new ClassSpec(Hook.GAME_ENGINE, cn) : null;
    }
    @Override
    public void evaluate(ClassNode cn) {
    	for(MethodNode mn : cn.methods){
    		if(mn.isStatic())
    			continue;
    		MethodData md = DynaFlowAnalyzer.getMethod(cn.name, mn.name, mn.desc);
    		if(mn.access==1028 && md.referencedFrom.size()>0 && new Wildcard("(?)V").matches(mn.desc)){
    			for(MethodData md2 : md.referencedFrom){
    				if(new Wildcard("(?)V").matches(md2.METHOD_DESC)){//render
    					for(MethodData md3 : md2.methodReferences){
    	    				if(new Wildcard("(?)Ljava/awt/Container;").matches(md3.METHOD_DESC)){//getGameContainer
    	        				Hook.GAME_ENGINE.put(new RSMethod(md.bytecodeMethod, "renderGame"));
    	        				Hook.GAME_ENGINE.put(new RSMethod(md2.bytecodeMethod, "render"));
    	        				Hook.GAME_ENGINE.put(new RSMethod(md3.bytecodeMethod, "getGameContainer"));
    	    					break;
    	    				}
    					}
    					break;
    				}
    			}
    		}
    		if(new Wildcard("(?)Z").matches(mn.desc)){
				Hook.GAME_ENGINE.put(new RSMethod(mn, "isHostValid"));
    		}
    		if(md.referencedFrom.size()>0 && new Wildcard("(Ljava/lang/String;?)V").matches(mn.desc)){
				Hook.GAME_ENGINE.put(new RSMethod(mn, "displayError"));
    		}
    	}
    	for(FieldNode fn : cn.fields){
    		if(fn.isStatic())
    			continue;
    		if(fn.desc.equals("Z"))
            	Hook.GAME_ENGINE.put(new RSField(fn, "gameEngineDumpProcessed"));
    	}
    }
}