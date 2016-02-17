/**
 * Copyright (c) 2015 Kyle Friz
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dank.analysis.impl.misc;

import com.dank.analysis.Analyser;
import com.dank.hook.Hook;
import com.dank.hook.RSField;
import com.dank.hook.RSMethod;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.BasicBlock;
import org.objectweb.asm.tree.*;

/**
 * @author Kyle Friz
 * @since Oct 26, 2015
 */
public class IsaacRandom extends Analyser {

    /*
     * (non-Javadoc)
     *
     * @see com.dank.analysis.Analyser#specify(org.objectweb.asm.tree.ClassNode)
     */
    @Override
    public ClassSpec specify(ClassNode cn) {
        return cn.name.equals(Hook.ISAAC_RANDOM.getInternalName()) ? new ClassSpec(Hook.ISAAC_RANDOM, cn) : null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.dank.analysis.Analyser#evaluate(org.objectweb.asm.tree.ClassNode)
     */
    @Override
    public void evaluate(ClassNode cn) {
        MethodNode method = cn.getMethodByName("<init>");
        for (BasicBlock block : method.graph()) {
            for (AbstractInsnNode ain : block.instructions) {
                if (ain.opcode() == Opcodes.GETFIELD) {
                    FieldInsnNode fin = (FieldInsnNode) ain;
                    Hook.ISAAC_RANDOM.put(new RSField(getClassPath().get(cn.name).getField(fin.name, fin.desc), "rsl"));
                    for (FieldNode fmn : cn.fields) {
                        if (!fmn.name.equals(fin.name) && fmn.desc.equals(fin.desc))
                            Hook.ISAAC_RANDOM.put(new RSField(fmn, "mem"));
                    }
                }
                if (ain.opcode() == Opcodes.INVOKEVIRTUAL) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    if (min.owner.equals(cn.name))
                        Hook.ISAAC_RANDOM.put(new RSMethod(getClassPath().get(cn.name).getMethod(min.name, min.desc), "init"));
                }
            }
        }
        method = cn.getMethodByName(((RSMethod) Hook.ISAAC_RANDOM.get("val")).name);
        for (BasicBlock block : method.graph()) {
            for (AbstractInsnNode ain : block.instructions) {
                if (ain.opcode() == Opcodes.GETFIELD) {
                    FieldInsnNode fin = (FieldInsnNode) ain;
                    Hook.ISAAC_RANDOM.put(new RSField(getClassPath().get(cn.name).getField(fin.name, fin.desc), "count"));
                }
                if (ain.opcode() == Opcodes.INVOKEVIRTUAL) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    if (min.owner.equals(cn.name))
                        Hook.ISAAC_RANDOM.put(new RSMethod(getClassPath().get(cn.name).getMethod(min.name, min.desc), "isaac"));
                }
            }
        }

        if(Hook.ISAAC_RANDOM.get("isaac") != null) {
        method = cn.getMethodByName(((RSMethod) Hook.ISAAC_RANDOM.get("isaac")).name);
        int i = 0;
        for (BasicBlock block : method.graph()) {
            if (block.getIndex() != 0)
                continue;

            for (AbstractInsnNode ain : block.instructions) {
                if (ain.opcode() == Opcodes.GETFIELD) {
                    if (i == 0) {
                        FieldInsnNode fin = (FieldInsnNode) ain;
                        if (fin.desc.equals("I")) {
                            i++;
                            Hook.ISAAC_RANDOM.put(new RSField(getClassPath().get(cn.name).getField(fin.name, fin.desc), "b"));
                        }
                    } else if (i == 1) {
                        FieldInsnNode fin = (FieldInsnNode) ain;
                        if (fin.desc.equals("I")) {
                            i++;
                            Hook.ISAAC_RANDOM.put(new RSField(getClassPath().get(cn.name).getField(fin.name, fin.desc), "c"));
                        }
                    } else break;
                }
            }
        }
    }

    for(
    FieldNode fn
    :cn.fields)

    {
        if(Hook.ISAAC_RANDOM.get("b") != null && Hook.ISAAC_RANDOM.get("c") != null && Hook.ISAAC_RANDOM.get("count") != null)
        if (!fn.name.equals(Hook.ISAAC_RANDOM.get("b").name)
                && !fn.name.equals(Hook.ISAAC_RANDOM.get("c").name)
                && !fn.name.equals(Hook.ISAAC_RANDOM.get("count").name)
                && fn.desc.equals("I")
                && !fn.isStatic())
            Hook.ISAAC_RANDOM.put(new RSField(fn, "a"));
    }
}

}
