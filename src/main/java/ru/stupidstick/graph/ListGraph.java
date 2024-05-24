package ru.stupidstick.graph;

import ru.stupidstick.fx.graph.VisEdge;
import ru.stupidstick.fx.graph.VisGraph;
import ru.stupidstick.fx.graph.VisNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ListGraph<N, D, W extends Comparable<W>> implements Graph<N, D, W> {

    private static final Random random = new Random();

    private final boolean isDirected;

    private final Map<Vertex<N, D>, List<Edge<N, D, W>>> vertices = new LinkedHashMap<>();

    public ListGraph(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public ListGraph(MatrixGraph<N, D, W> matrixGraph) {
        this.isDirected = matrixGraph.isDirected();
        var vertexIterator = matrixGraph.vertexIterator();
        while (vertexIterator.hasSet()) {
            var vertex = vertexIterator.get();
            var edgeIterator = matrixGraph.edgeIterator(vertex);
            List<Edge<N, D, W>> edges = new ArrayList<>();
            while (edgeIterator.hasSet()) {
                edges.add(edgeIterator.get());
                edgeIterator.next();
            }
            vertices.put(vertex, edges);
            vertexIterator.next();
        }
    }

    @Override
    public Vertex<N, D> insertVertex(N name, D data) {
        Vertex<N, D> vertex = new Vertex<>(name, data);
        vertices.put(vertex, new ArrayList<>());
        return vertex;
    }

    @Override
    public boolean removeVertex(Vertex<N, D> vertex) {
        if (!vertices.containsKey(vertex)) {
            return false;
        }
        vertices.remove(vertex);
        vertices.values()
                .forEach(edges -> edges.removeIf(edge -> edge.getFrom().equals(vertex) || edge.getTo().equals(vertex)));
        return true;
    }

    @Override
    public Edge<N, D, W> insertEdge(Vertex<N, D> v1, Vertex<N, D> v2) {
        if (!vertices.containsKey(v1) || !vertices.containsKey(v2)) {
            throw new IllegalArgumentException("Vertex not found");
        }
        Edge<N, D, W> edge = new Edge<>(v1, v2);
        vertices.get(v1).add(edge);
        if (!isDirected) {
            vertices.get(v2).add(edge);
        }
        return edge;
    }

    @Override
    public boolean deleteEdge(Vertex<N, D> v1, Vertex<N, D> v2) {
        if (!vertices.containsKey(v1) || !vertices.containsKey(v2)) {
            return false;
        }

        vertices.get(v1).removeIf(edge -> edge.getTo().equals(v2));
        if (!isDirected) {
            vertices.get(v2).removeIf(edge -> edge.getTo().equals(v1));
        }
        return true;
    }

    @Override
    public Optional<Edge<N, D, W>> getEdge(Vertex<N, D> v1, Vertex<N, D> v2) {
        if (!vertices.containsKey(v1) || !vertices.containsKey(v2)) {
            return Optional.empty();
        }
        return vertices.get(v1).stream()
                .filter(edge -> edge.getTo().equals(v2))
                .findFirst();
    }

    @Override
    public VisGraph toVisGraph() {
        Map<Vertex<?, ?>, VisNode> nodes = vertices.keySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                vertex -> vertex,
                                vertex -> new VisNode(random.nextInt(), vertex.toString())
                        )
                );
        ArrayList<VisEdge> visEdges = new ArrayList<>();

        Set<Edge<N, D, W>> converted = new LinkedHashSet<>();

        for (var edges : vertices.values()) {
            for (var e : edges) {
                if (!converted.contains(e)) {
                    if (!isDirected) {
                        visEdges.add(new VisEdge(nodes.get(e.getFrom()), nodes.get(e.getTo()), "", e.toString()));
                    } else {
                        visEdges.add(new VisEdge(nodes.get(e.getFrom()), nodes.get(e.getTo()), "to", e.toString()));
                    }
                    converted.add(e);
                }
            }
        }


        VisGraph visGraph = new VisGraph();
        visGraph.addNodes(nodes.values().toArray(VisNode[]::new));
        visGraph.addEdges(visEdges.toArray(VisEdge[]::new));

        return visGraph;
    }

    @Override
    public boolean isDirected() {
        return isDirected;
    }

    @Override
    public Iterator<Vertex<N, D>> vertexIterator() {
        return new GraphIterator<>(new ArrayList<>(vertices.keySet()));
    }

    @Override
    public Iterator<Edge<N, D, W>> edgeIterator() {
        return new GraphIterator<>(vertices.values().stream().flatMap(List::stream).toList());
    }

    @Override
    public Iterator<Edge<N, D, W>> edgeIterator(Vertex<N, D> vertex) {
        return new GraphIterator<>(vertices.get(vertex));
    }

    public static class GraphIterator<T> implements Iterator<T> {

        private final List<T> list;
        private int index;

        public GraphIterator(List<T> list) {
            this.list = list;
            this.index = 0;
        }

        @Override
        public void next() {
            index++;
        }

        @Override
        public boolean hasSet() {
            return index < list.size();
        }

        @Override
        public T get() {
            return list.get(index);
        }
    }
}
