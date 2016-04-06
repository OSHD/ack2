package com.marn.asm;

import java.util.ArrayList;

import org.objectweb.asm.tree.ClassNode;

public class ClassData {
    public String CLASS_NAME;
    public String REFACTORED_NAME;
    public ClassNode bytecodeClass;
    public ArrayList<MethodData> methods = new ArrayList<MethodData>();
    public ArrayList<MethodData> checkcastReferences = new ArrayList<MethodData>();
    public ArrayList<FieldData> fields = new ArrayList<FieldData>();
    public ArrayList<FieldData> instances = new ArrayList<FieldData>();
    public ClassData(ClassNode clazz){
    	bytecodeClass=clazz;
        REFACTORED_NAME = clazz.name;
        CLASS_NAME = clazz.name;
    }
    public void addMethod(MethodData md){
    	MethodData remove=null;
    	for(MethodData temp : methods){
    		if(temp.CLASS_NAME.equals(md.CLASS_NAME) && temp.METHOD_NAME.equals(md.METHOD_NAME) && temp.METHOD_DESC.equals(md.METHOD_DESC)){
    			remove=temp;
    			break;
    		}
    	}
    	if(remove!=null)
    		methods.remove(remove);
    	methods.add(md);
    }
    public void addField(FieldData fd){
    	FieldData remove=null;
    	for(FieldData temp : fields){
    		if(temp.CLASS_NAME.equals(fd.CLASS_NAME) && temp.FIELD_NAME.equals(fd.FIELD_NAME)){
    			remove=temp;
    			break;
    		}
    	}
    	if(remove!=null)
    		fields.remove(remove);
    	fields.add(fd);
    }
    public void addCheckcastReference(MethodData md){
    	checkcastReferences.add(md);
    }
    public void addInstanceReference(FieldData fd){
    	instances.add(fd);
    }
    public boolean containsMethod(MethodData md){
    	return methods.contains(md);
    }
    public boolean containsField(FieldData fd){
    	return fields.contains(fd);
    }
    public boolean isInstance(FieldData fd){
    	return instances.contains(fd);
    }
    public void setRefactoredName(String s){
    	REFACTORED_NAME=s;
    }
}
