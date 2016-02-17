package dank.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.commons.cfg.graph.Digraph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.asm.ClassPath;

/**
 * Project: DankWise
 * Date: 09-03-2015
 * Time: 20:59
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class CallGraphVisitor extends Digraph<MethodNode, MethodInsnNode> {

    private final ClassPath classPath;
    private final HierarchyVisitor hierarchyVisitor;

    public CallGraphVisitor(final ClassPath classPath, final HierarchyVisitor hierarchyVisitor) {
        this.classPath = classPath;
        this.hierarchyVisitor = hierarchyVisitor;
        final List<MethodNode> entryPoints = new ArrayList<>();
        classPath.forEach((name, cn) -> cn.methods.forEach(mn -> {
            if (mn.name.length() > 2) {
                entryPoints.add(mn);
            }
        }));
        entryPoints.forEach(this::visit);
    }

    private void visit(final MethodNode vertex) {
        if (containsVertex(vertex)) {
            return;
        }
        addVertex(vertex);
        for (final AbstractInsnNode ain : vertex.instructions.toArray()) {
            if (ain instanceof MethodInsnNode) {
                final MethodInsnNode edge = (MethodInsnNode) ain;
                if (edge.owner.equals("dz")
    					&& edge.name.equals("i")
    					&& edge.desc.equals("(I)Z"))
                	System.out.println(vertex.owner.name + "" + vertex.name + vertex.desc);
                if (!containsEdge(vertex, edge)) {
                    addEdge(vertex, edge);
                    if (classPath.get(edge.owner) != null) {
                        visitMethodHierarchy(edge);
                        final ClassNode cn = classPath.get(edge.owner);
                        if (cn.getMethod(edge.name, edge.desc) != null) {
                            addEdge(vertex, edge);
                            visitMethodHierarchy(edge);
                        }
                    }
                }
            }
        }
    }

    private void visitMethodHierarchy(final MethodInsnNode edge) {
        final Set<ClassNode> preds = hierarchyVisitor.getHierarchy().edgesFrom(classPath.get(edge.owner));
        for (final ClassNode cn : preds) {
            final MethodNode mn = getMethod(cn.name, edge.name, edge.desc);
            if (mn != null) {
                visit(mn);
            }
        }
    }

    private MethodNode getMethod(final String owner, final String name, final String desc) {
        if (classPath.get(owner) != null) {
            for (final MethodNode method : classPath.get(owner).methods) {
                if (method.name.equals(name) && method.desc.equals(desc)) {
                    return method;
                }
            }
        }
        return null;
    }
}
