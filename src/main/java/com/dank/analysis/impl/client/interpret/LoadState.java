package com.dank.analysis.impl.client.interpret;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LabelNode;

import com.dank.analysis.interpret.NumericConstantBranch;
import com.dank.util.MemberKey;

/**
 * if( constant_numer <COMPARE> field * decoder ) <- Hit
 *
 * so...
 *
 * look for all branches
 *
 *
 */

public class LoadState extends NumericConstantBranch {

    private static final Integer[] LOOK_FOR
            = new Integer[] { 20,40,45,50,60,70,80,90,110,120,130,140 };

    Map<MemberKey,Set<Number>> hits = new HashMap<>();

    @Override
    public void visitBranch(Number value, FieldInsnNode compare, int compare_type, LabelNode target) {
        if(compare.opcode()== GETSTATIC) {

            FieldInsnNode fin = compare;
            MemberKey key = new MemberKey(fin);
            Set<Number> set = hits.get(key);
            if(set == null) {
                set = new HashSet<>();
                hits.put(key,set);
            }
            set.add(value);
        }
    }

    public MemberKey get() {

        out:
        for(Map.Entry<MemberKey,Set<Number>> hit : hits.entrySet()) {
            Set<Number> hits = hit.getValue();
            for(Integer key : LOOK_FOR) {
                if(!hits.contains(key)) continue out;
            }
            return hit.getKey();
        }
        return null;
    }

    public void clear() {
        hits.clear();
    }

}
