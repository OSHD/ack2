package dank.tests;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.cfg.graph.Digraph;
import org.objectweb.asm.tree.ClassNode;

import com.dank.asm.ClassPath;

/**
 * Project: DankWise
 * Date: 09-03-2015
 * Time: 21:23
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class HierarchyVisitor extends ClassVisitor {

    private final ClassPath classPath;
    private final Digraph<ClassNode, ClassNode> hierarchy;

    public HierarchyVisitor(final ClassPath classPath) {
        this.classPath = classPath;
        this.hierarchy = new Digraph<>();
    }

    public void accept(final ClassNode cn) {
        if (!hierarchy.containsVertex(cn)) {
            hierarchy.addVertex(cn);
        }

        ClassNode superType = classPath.get(cn.superName);
        while (superType != null) {
            hierarchy.addEdge(cn, superType);
            superType = classPath.get(superType.superName);
        }
    }

    public Digraph<ClassNode, ClassNode> getHierarchy() {
        return hierarchy;
    }
}
