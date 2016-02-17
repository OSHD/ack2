package dank.stresser;

import java.lang.reflect.Modifier;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Septron
 * @since January 28, 2015
 */
public class Helper implements Opcodes {

    public static int ASM_LOAD_ARGS = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

    public static ClassNode load(byte[] data) {
        ClassReader reader = new ClassReader(data);
        ClassNode cn = new ClassNode();
        reader.accept(cn, ASM_LOAD_ARGS);
        return cn;
    }

    /**
     * Turn a {@link org.objectweb.asm.tree.ClassNode} into a byte array.
     * @param cn The {@link org.objectweb.asm.tree.ClassNode} you want to turn into a byte array.
     * @return The {@link org.objectweb.asm.tree.ClassNode} provided as a byte array.
     */
    public static byte[] data(ClassNode cn) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(writer);
        return writer.toByteArray();
    }

    /**
     * Makes the supplied value's access code public.
     *
     * @param cn The {@link org.objectweb.asm.tree.ClassNode} to make public.
     */
    public static void publicize(ClassNode cn) {
        if (!Modifier.isPublic(cn.access)) {
            if (Modifier.isProtected(cn.access)) {
                cn.access = cn.access & (~ACC_PROTECTED);
            }
            if (Modifier.isPrivate(cn.access)) {
                cn.access = cn.access & (~ACC_PRIVATE);
            }
            cn.access = cn.access | ACC_PUBLIC;

            //TODO: Check if I should do this or na
            for (MethodNode mn : cn.methods) {
                if (mn.name.startsWith("<i"))
                    if (!Modifier.isPublic(mn.access))
                        publicize(mn);
            }
        }
    }

    /**
     * Makes the supplied value's access code public.
     *
     * @param fn The {@link org.objectweb.asm.tree.FieldNode} to make public.
     */
    public static void publicize(FieldNode fn) {
        if (!Modifier.isPublic(fn.access)) {
            if (Modifier.isProtected(fn.access)) {
                fn.access = fn.access & (~ACC_PROTECTED);
            }
            if (Modifier.isPrivate(fn.access)) {
                fn.access = fn.access & (~ACC_PRIVATE);
            }
            fn.access = fn.access | ACC_PUBLIC;
        }
    }

    /**
     * Makes the supplied value's access code public.
     *
     * @param mn The {@link org.objectweb.asm.tree.MethodNode} to make public.
     */
    public static void publicize(MethodNode mn) {
        if (!Modifier.isPublic(mn.access)) {
            if (Modifier.isProtected(mn.access)) {
                mn.access = mn.access & (~ACC_PROTECTED);
            }
            if (Modifier.isPrivate(mn.access)) {
                mn.access = mn.access & (~ACC_PRIVATE);
            }
            mn.access = mn.access | ACC_PUBLIC;
        }
    }
}
