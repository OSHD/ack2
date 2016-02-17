package com.dank.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.commons.cfg.graph.ListDigraph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.asm.ClassPath;

public class FieldCallGraphVisitor {

	Map<ClassNode, ListDigraph<MethodNode, FieldNode>> callgraphs = new HashMap<ClassNode, ListDigraph<MethodNode, FieldNode>>();

	public FieldCallGraphVisitor(ClassPath path) {
		List<MethodNode> entries = new ArrayList<>();

		for (ClassNode cn : path.getClasses()) {
			entries.addAll(cn.methods);
			callgraphs.put(cn, new ListDigraph<MethodNode, FieldNode>());
		}

		entries.forEach(e -> search(path, e));
		
		int size = 0;
		for (ListDigraph<MethodNode, FieldNode> cg : callgraphs.values()) {
			size += cg.setSize();
		}
		System.out.println("Graphed " + size + " Field Call(s).");
	}

	private void search(ClassPath tree, MethodNode vertex) {
		ListDigraph<MethodNode, FieldNode> cg = callgraphs.get(vertex.owner);
		
		if (cg == null) {
			throw new NullPointerException("unbuilt callgraph for " + vertex.owner);
		}

		if (cg.containsVertex(vertex))
			return;
		
		cg.addVertex(vertex);
		
		outer: for (AbstractInsnNode ain : vertex.instructions.toArray()) {
			if (ain instanceof FieldInsnNode) {
				FieldInsnNode fin = (FieldInsnNode) ain;
				if (tree.getMap().containsKey(fin.owner)) {
					ClassNode cn = tree.get(fin.owner);
					FieldNode edge = cn.getField(fin.name, fin.desc, false);
					if (edge != null) {
						cg.addEdge(vertex, edge); // method is called, graph it
						continue;
					}
					// do the same for all supertypes and superinterfaces
					for (ClassNode superNode : tree.getSupers(cn)) {
						FieldNode superedge = superNode.getField(fin.name, fin.desc, false);
						if (superedge != null) {
							cg.addEdge(vertex, superedge);
							continue outer;
						}
					}
				}
			}
		}
	}
	
	public MethodNode getCaller(FieldNode field) {
		for (ListDigraph<MethodNode, FieldNode> cg : callgraphs.values()) {
			for (List<FieldNode> calls : cg.values()) {
				for (FieldNode call : calls) {
					if (call.owner.name.equals(field.owner.name)
							&& call.name.equals(field.name)
							&& call.desc.equals(field.desc)) {
						for (MethodNode caller : cg.keys()) {
							if (cg.containsEdge(caller, call)) {
								return caller;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public MethodNode getCaller(FieldNode field, int occurrences) {
		for (ListDigraph<MethodNode, FieldNode> cg : callgraphs.values()) {
			for (List<FieldNode> calls : cg.values()) {
				for (FieldNode call : calls) {
					if (call.owner.name.equals(field.owner.name)
							&& call.name.equals(field.name)
							&& call.desc.equals(field.desc)) {
						for (MethodNode caller : cg.keys()) {
							if (cg.containsEdge(caller, call)) {
								if (occourances(cg, call, caller) == occurrences)
									return caller;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	protected int occourances(ListDigraph<MethodNode, FieldNode> cg, FieldNode method, MethodNode caller) {
		int occurrences = 0;
		for (FieldNode call : cg.edgesFrom(caller)) {
			if (call.name.equals(method.name)
					&& call.desc.equals(method.desc)) {
				occurrences++;
			}
		}
		return occurrences;
	}
}