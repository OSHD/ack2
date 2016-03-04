package com.dank.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.cfg.graph.ListDigraph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.dank.asm.ClassPath;

public class MethodCallGraphVisitor {

	Map<ClassNode, ListDigraph<MethodNode, MethodNode>> callgraphs = new HashMap<ClassNode, ListDigraph<MethodNode, MethodNode>>();
	
	public MethodCallGraphVisitor(ClassPath path) {
		List<MethodNode> entries = new ArrayList<>();
		// find methods we want to keep and build callgraphs for all of the nodes
		for (ClassNode cn : path.getClasses()) {
			entries.addAll(findRealCandidates(path, cn));
			callgraphs.put(cn, new ListDigraph<MethodNode, MethodNode>());
		}
		// follow calls and graph called methods appending them to the reserved list.
		entries.forEach(e -> search(path, e));
		
		int size = 0;
		for (ListDigraph<MethodNode, MethodNode> cg : callgraphs.values()) {
			size += cg.setSize();
		}
		System.out.println("Graphed " + size + " Method Call(s).");
	}

	private void search(ClassPath path, MethodNode vertex) {
		ListDigraph<MethodNode, MethodNode> cg = callgraphs.get(vertex.owner);
		if (cg == null) {
			throw new NullPointerException("unbuilt callgraph for " + vertex.owner);
		}

		if (cg.containsVertex(vertex))
			return;
		
		cg.addVertex(vertex);
		
		outer: for (AbstractInsnNode ain : vertex.instructions.toArray()) {
			if (ain instanceof MethodInsnNode) {
				MethodInsnNode min = (MethodInsnNode) ain;
				if (path.getMap().containsKey(min.owner)) {
					ClassNode cn = path.get(min.owner);
					MethodNode edge = cn.getMethod(min.name, min.desc);
					if (edge != null) {
						cg.addEdge(vertex, edge); // method is called, graph it
						search(path, edge); // search outgoing calls from that method
						continue;
					}
					// do the same for all supertypes and superinterfaces
					for (ClassNode superNode : path.getSupers(cn)) {
						MethodNode superedge = superNode.getMethod(min.name, min.desc);
						if (superedge != null) {
							cg.addEdge(vertex, superedge);
							search(path, superedge);
							continue outer;
						}
					}
				}
			}
		}
	}

	public List<MethodNode> findRealCandidates(ClassPath path, ClassNode cn) {
		List<MethodNode> methods = new ArrayList<MethodNode>();
		for (MethodNode mn : cn.methods) {
			if (protectedMethod(path, mn)) {
				methods.add(mn);
			}
		}
		return methods;
	}
	
	public MethodNode getCaller(MethodNode method) {
		for (ListDigraph<MethodNode, MethodNode> cg : callgraphs.values()) {
			for (List<MethodNode> calls : cg.values()) {
				for (MethodNode call : calls) {
					if (call.owner.name.equals(method.owner.name)
							&& call.name.equals(method.name)
							&& call.desc.equals(method.desc)) {
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
	
	public MethodNode getCaller(MethodNode method, int occurrences) {
		for (ListDigraph<MethodNode, MethodNode> cg : callgraphs.values()) {
			for (List<MethodNode> calls : cg.values()) {
				for (MethodNode call : calls) {
					if (call.owner.name.equals(method.owner.name)
							&& call.name.equals(method.name)
							&& call.desc.equals(method.desc)) {
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
	
	protected int occourances(ListDigraph<MethodNode, MethodNode> cg, MethodNode method, MethodNode caller) {
		int occurrences = 0;
		for (MethodNode call : cg.edgesFrom(caller)) {
			if (call.name.equals(method.name)
					&& call.desc.equals(method.desc)) {
				occurrences++;
			}
		}
		return occurrences;
	}

	protected boolean protectedMethod(ClassPath path, MethodNode mn) {
		return mn.name.length() > 2 || isInherited(path, mn.owner, mn);
	}
	
	private MethodNode getMethodFromSuper(ClassPath path, ClassNode cn, String name, String desc, boolean isStatic) {
		for (ClassNode super_ : path.getSupers(cn)) {
			for (MethodNode mn : super_.methods) {
				if (mn.name.equals(name) && mn.desc.equals(desc) && ((mn.access & Opcodes.ACC_STATIC) != 0) == isStatic) {
					return mn;
				}
			}
		}
		return null;
	}
	
	private boolean isInherited(ClassPath path, ClassNode cn, String name, String desc, boolean isStatic) {
		return getMethodFromSuper(path, cn, name, desc, isStatic) != null;
	}

	private boolean isInherited(ClassPath path, ClassNode owner, MethodNode mn) {
		if(owner == null) {
			throw new NullPointerException();
		}
		return mn.owner.name.equals(owner.name) && isInherited(path, owner, mn.name, mn.desc, (mn.access & Opcodes.ACC_STATIC) != 0);
	}
}