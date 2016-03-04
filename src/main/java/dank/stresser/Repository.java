package dank.stresser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author Septron
 * @since January 26, 2015
 */
public class Repository {

    private final Map<String, ClassNode> classes = new HashMap<>();

    public Collection<ClassNode> classes() {
        return classes.values();
    }

    public void load(File location) throws IOException {
        if (!location.getPath().endsWith(".jar"))
            throw new RuntimeException("Unsupported file type!");
        JarFile jar = new JarFile(location);
        Enumeration<JarEntry> enumeration = jar.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry entry = enumeration.nextElement();
            if (!entry.getName().endsWith(".class"))
                continue;
            ClassNode cn = new ClassNode();
            try (InputStream stream = new BufferedInputStream(jar.getInputStream(entry))) {
                new ClassReader(stream).accept(cn, Helper.ASM_LOAD_ARGS);
            }
            classes.put(cn.name, cn);
        }
    }

    public void dump(File location) throws IOException {
        if (!location.getPath().endsWith(".jar"))
            throw new RuntimeException("Unsupported file type!");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(location))) {
            for (ClassNode cn : classes.values()) {
                jos.putNextEntry(new JarEntry(cn.name.replaceAll("\\.", "/") + ".class"));

                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                cn.accept(writer);

                jos.write(writer.toByteArray());
                jos.closeEntry();
            }
        }
    }
}
