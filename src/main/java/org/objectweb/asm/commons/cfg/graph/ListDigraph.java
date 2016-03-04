package org.objectweb.asm.commons.cfg.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Tyler Sedlar
 */
public class ListDigraph<V, E> implements Iterable<V> {

    protected final Map<V, List<E>> graph = new HashMap<>();

    @SuppressWarnings("unchecked")
    public List<E> edgeAt(int index) {
        return (List<E>) graph.values().toArray()[index];
    }

    public int size() {
        return graph.size();
    }
    
    public int setSize() {
    	int size = 0;
    	for (List<E> e : graph.values()) {
    		size += e.size();
    	}
    	return size;
    }

    public boolean containsVertex(V vertex) {
        return graph.containsKey(vertex);
    }

    public boolean containsEdge(V vertex, E edge) {
        return graph.containsKey(vertex) && graph.get(vertex).contains(edge);
    }

    public boolean addVertex(V vertex) {
        if (graph.containsKey(vertex)) return false;
        graph.put(vertex, new ArrayList<E>());
        return true;
    }

    public void addEdge(V start, E dest) {
        if (!graph.containsKey(start)) return;
        graph.get(start).add(dest);
    }

    public void removeEdge(V start, E dest) {
        if (!graph.containsKey(start)) return;
        graph.get(start).remove(dest);
    }

    public List<E> edgesFrom(V node) {
        return Collections.unmodifiableList(graph.get(node));
    }

    public void graph(ListDigraph<V, E> graph) {
        this.graph.putAll(graph.graph);
    }
    
    public Set<V> keys() {
    	return graph.keySet();
    }
    
    public Collection<List<E>> values() {
    	return graph.values();
    }

    @Override
    public final Iterator<V> iterator() {
        return graph.keySet().iterator();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (V v : graph.keySet()) {
            builder.append("\n    ").append(v).append(" -> ").append(graph.get(v));
        }
        return builder.toString();
    }

    public void flush() {
        graph.clear();
    }

    public final int getCyclomaticComplexity() {
        int outgoing = 0, neighbours = 0;
        for (final List<E> edges : graph.values()) {
            outgoing += edges.size();
            if (!edges.isEmpty()) { //edges shud nvr b empty in FlowGraph's, but check just in case this gets used for other purposes
                neighbours++;
            }
        }
        return outgoing - size() + neighbours;
    }

    public final boolean complexity(final int left, final int right) {
        final int complexity = getCyclomaticComplexity();
        return complexity >= left && complexity <= right;
    }
}