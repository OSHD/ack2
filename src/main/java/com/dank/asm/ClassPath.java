package com.dank.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Project: RS3Injector
 * Time: 00:03
 * Date: 04-02-2015
 * Created by Dogerina.
 */
public final class ClassPath implements Iterable<ClassNode> {

    private final Map<String, ClassNode> classes = new HashMap<>();


    public static ClassNode mkClass(final byte[] bytez) {
        final ClassReader cr = new ClassReader(bytez);
        final ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return cn;
    }

    /**
     * @param name
     * @return a ClassInfo from the map matching name
     */
    public ClassNode get(final String name) {		
        for (final ClassNode ci : classes.values()) {
            if (ci.name.equals(name)) {
                return ci;
            }
        }
		
        return null;
    }

    public Collection<ClassNode> getClasses() {
        return classes.values();
    }

    /**
     * Adds a class to the map
     *
     * @param clazz
     */
    public void addClass(final ClassNode clazz) {
        classes.put(clazz.name, clazz);
    }

    /**
     * Adds a class from file
     *
     * @param name
     */
    public void addClass(final String name) {
        try {
            final ClassReader cr = new ClassReader(name);
            final ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            classes.put(cn.name, cn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addClass(final byte[] bytez) {
        addClass(mkClass(bytez));
    }

    public void addClasses(final Map<String, byte[]> jar) {
        final Map<String, ClassNode> classes = new HashMap<>();
        for (final Map.Entry<String, byte[]> clazz : jar.entrySet()) {
            classes.put(clazz.getKey(), mkClass(clazz.getValue()));
        }
        classes.values().forEach(this::addClass);
    }

    public Map<String, byte[]> toByteMap() {
        final Map<String, byte[]> classes = new HashMap<>();
        this.classes.values().forEach(cn -> {
            final ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);
            classes.put(cn.name, cw.toByteArray());
        });
        return classes;
    }

    public Manifest addJar(final String jar) {
        try (final JarInputStream jis = new JarInputStream(new FileInputStream(jar))) {
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    final ClassReader cr = new ClassReader(jis);
                    final ClassNode cn = new ClassNode();
                    cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    classes.put(cn.name, cn);
                }
            }
            if (jis.getManifest() != null) {
                return jis.getManifest();
            }
        } catch (final IOException e) {}
        return null;
    }

    /**
     * Saves the class map to a jar
     *
     * @param name
     */
    public void dump(final String name) {
        final File file = new File(name);
        try (final JarOutputStream jos = new JarOutputStream(new FileOutputStream(file))) {
            for (final ClassNode clazz : classes.values()) {
                jos.putNextEntry(new JarEntry(clazz.name + ".class"));
                final ClassWriter cw = new ClassWriter(0);
                clazz.accept(cw);
                jos.write(cw.toByteArray());
                jos.closeEntry();
            }
            jos.flush();
            jos.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void forEach(final BiConsumer<? super String, ? super ClassNode> consumer) {
        classes.forEach(consumer);
    }

    @Override
    public Iterator<ClassNode> iterator() {
        return getClasses().iterator();
    }
    
    public void clear() {
    	classes.clear();
    }

    public Map<String, ClassNode> getMap() {
        return classes;
    }

    public List<ClassNode> getSupers(ClassNode node) {
		List<ClassNode> supers = new ArrayList<>();
		
		ClassNode super_ = get(node.superName);
		while (super_ != null) {
			supers.add(0, super_);
			
			super_ = get(super_.superName);
		}
		
		return supers;
	}
    
    public FieldNode getFieldFromSuper(ClassNode cn, String name, String desc, boolean isStatic) {
		for (ClassNode super_ : getSupers(cn)) {
			for (FieldNode fn : super_.fields) {
				if (fn.name.equals(name) && fn.desc.equals(desc) && ((fn.access & Opcodes.ACC_STATIC) != 0) == isStatic) {
					return fn;
				}
			}
		}
		return null;
	}
    
    public MethodNode getMethodFromSuper(ClassNode cn, String name, String desc, boolean isStatic) {
		for (ClassNode super_ : getSupers(cn)) {
			for (MethodNode mn : super_.methods) {
				if (mn.name.equals(name) && mn.desc.equals(desc) && ((mn.access & Opcodes.ACC_STATIC) != 0) == isStatic) {
					return mn;
				}
			}
		}
		return null;
	}
}
