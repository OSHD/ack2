package com.marn.asm;

import com.dank.asm.Mask;

/******************************************************
 * Created by Marneus901                                *
 * � 2012-2014                                          *
 * **************************************************** *
 * Access to this source is unauthorized without prior  *
 * authorization from its appropriate author(s).        *
 * You are not permitted to release, nor distribute this* 
 * work without appropriate author(s) authorization.    *
 ********************************************************/

public class FieldSigniture {
    public String[] args;
    public String retType;
    public String bytecodePattern;
    public Mask[] mask;
    public boolean regex = false;

    public FieldSigniture(String returnType, String[] arguments, String pattern) {
        retType = returnType;
        args = arguments;
        bytecodePattern = pattern;
    }

    public FieldSigniture(String returnType, String[] arguments) {
        retType = returnType;
        args = arguments;
    }

    public FieldSigniture(String returnType, String[] arguments, Mask... masks) {
        retType = returnType;
        args = arguments;
        mask = masks;
    }

    public FieldSigniture(String returnType, String[] arguments, String pattern, boolean regex) {
        retType = returnType;
        args = arguments;
        bytecodePattern = pattern;
        this.regex = true;

    }
}

