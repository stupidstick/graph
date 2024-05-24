package ru.stupidstick.graph;

import ru.stupidstick.fx.graph.VisEdge;
import ru.stupidstick.fx.graph.VisGraph;
import ru.stupidstick.fx.graph.VisNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class MatrixGraph<N, D, W extends Comparable<W>> implements Graph<N, D, W> {

    private final Edge<N, D, W> edgePlaceholder = new Edge<>(null, null);

    private final boolean isDirected;

    private final List<List<Edge<N, D, W>>> edges = new ArrayList<>();

    private final List<Vertex<N, D>> vertices = new ArrayList<>();

    public MatrixGraph(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public MatrixGraph(ListGraph<N, D, W> listGraph) {
        this.isDirected = listGraph.isDirected();
        var vertexIterator = listGraph.vertexIterator();
        while (vertexIterator.hasSet()) {
            var vertex = vertexIterator.get();
            insertVertex(vertex);
        }
        var edgeIterator = listGraph.edgeIterator();
        while (edgeIterator.hasSet()) {
            var edge = edgeIterator.get();
            insertEdge(edge);
            edgeIterator.next();
        }
    }

    @Override
    public Vertex<N, D> insertVertex(N name, D data) {
        Vertex<N, D> vertex = new Vertex<>(name, data);
        vertices.add(vertex);

        edges.forEach(row -> row.add(edgePlaceholder));
        edges.add(new ArrayList<>(Collections.nCopies(vertices.size(), edgePlaceholder)));

        return vertex;
    }

    @Override
    public boolean removeVertex(Vertex<N, D> vertex) {
        int index = vertices.indexOf(vertex);
        if (index == -1) {
            return false;
        }

        vertices.remove(index);
        edges.remove(index);
        edges.forEach(row -> row.remove(index));

        return true;
    }



    @Override
    public Edge<N, D, W> insertEdge(Vertex<N, D> v1, Vertex<N, D> v2) {
        int i1 = vertices.indexOf(v1);
        int i2 = vertices.indexOf(v2);
        if (i1 == -1 || i2 == -1) {
            throw new IllegalArgumentException("Vertex not found");
        }

        Edge<N, D, W> edge = new Edge<>(v1, v2);
        edges.get(i1).set(i2, edge);
        if (!isDirected) {
            edges.get(i2).set(i1, edge);
        }

        return edge;
    }

    @Override
    public boolean deleteEdge(Vertex<N, D> v1, Vertex<N, D> v2) {
        int i1 = vertices.indexOf(v1);
        int i2 = vertices.indexOf(v2);
        if (i1 == -1 || i2 == -1) {
            return false;
        }

        edges.get(i1).set(i2, edgePlaceholder);
        if (!isDirected) {
            edges.get(i2).set(i1, edgePlaceholder);
        }

        return true;
    }

    @Override
    public Optional<Edge<N, D, W>> getEdge(Vertex<N, D> v1, Vertex<N, D> v2) {
        int i1 = vertices.indexOf(v1);
        int i2 = vertices.indexOf(v2);
        if (i1 == -1 || i2 == -1) {
            return Optional.empty();
        }

        Edge<N, D, W> edge = edges.get(i1).get(i2);
        return edge == edgePlaceholder ? Optional.empty() : Optional.of(edge);
    }

    @Override
    public VisGraph toVisGraph() {
        VisNode[] nodes = vertices.stream().map(vertex -> new VisNode(new Random().nextInt(), vertex.toString())).toArray(VisNode[]::new);
        ArrayList<VisEdge> visEdges = new ArrayList<>();

        for (int i = 0; i < edges.size(); i++) {
            for (int j = 0; j < edges.get(i).size(); j++) {
                if (j < i)
                    continue;
                var edge1 = edges.get(i).get(j);
                var edge2 = edges.get(j).get(i);

                if (!isDirected) {
                    if (edge1 != edgePlaceholder) {
                        visEdges.add(new VisEdge(nodes[i], nodes[j], "", edge1.toString()));
                    }
                } else {
                    if (edge1 != edgePlaceholder) {
                        visEdges.add(new VisEdge(nodes[i], nodes[j], "to", edge1.toString()));
                    }
                    if (edge2 != edgePlaceholder) {
                        visEdges.add(new VisEdge(nodes[j], nodes[i], "to", edge2.toString()));
                    }
                }
            }
        }

        VisGraph visGraph = new VisGraph();
        visGraph.addNodes(nodes);
        visGraph.addEdges(visEdges.toArray(VisEdge[]::new));

        return visGraph;
    }

    @Override
    public Iterator<Vertex<N, D>> vertexIterator() {
        return new MatrixGraphIterator<>(vertices);
    }

    @Override
    public Iterator<Edge<N, D, W>> edgeIterator() {
        return new MatrixGraphIterator<>(edges.stream().flatMap(List::stream).toList());
    }

    @Override
    public Iterator<Edge<N, D, W>> edgeIterator(Vertex<N, D> vertex) {
        return new MatrixGraphIterator<>(edges.get(vertices.indexOf(vertex)));
    }

    @Override
    public boolean isDirected() {
        return isDirected;
    }

    private void insertVertex(Vertex<N, D> vertex) {
        vertices.add(vertex);

        edges.forEach(row -> row.add(edgePlaceholder));
        edges.add(new ArrayList<>(Collections.nCopies(vertices.size(), edgePlaceholder)));
    }


    private void insertEdge(Edge<N, D, W> edge) {
        int i1 = vertices.indexOf(edge.from);
        int i2 = vertices.indexOf(edge.to);
        if (i1 == -1 || i2 == -1) {
            throw new IllegalArgumentException("Vertex not found");
        }
        edges.get(i1).set(i2, edge);
        if (!isDirected) {
            edges.get(i2).set(i1, edge);
        }
    }

    // Method to find the minimum spanning tree with height limit
    public List<Edge<N, D, W>> findSpanningTreeWithHeightLimit(int maxHeight) {
        maxHeight--;
        if (!isDirected) {
            throw new UnsupportedOperationException("Only directed graphs are supported");
        }

        if (vertices.isEmpty()) {
            return Collections.emptyList();
        }

        List<Edge<N, D, W>> spanningTree = new ArrayList<>();
        Set<Vertex<N, D>> visited = new LinkedHashSet<>();
        Queue<Edge<N, D, W>> pq = new LinkedList<>();

        // Add all edges of the starting vertex to the priority queue
        Vertex<N, D> startVertex = vertices.get(0);
        visited.add(startVertex);
        pq.addAll(getOutgoingEdges(startVertex));

        // Prim's algorithm to create a minimum spanning tree
        while (!pq.isEmpty() && spanningTree.size() < vertices.size() - 1) {
            Edge<N, D, W> edge = pq.poll();
            Vertex<N, D> to = edge.getTo();

            if (visited.contains(to)) {
                continue;
            }

            spanningTree.add(edge);
            visited.add(to);
            pq.addAll(getOutgoingEdges(to));
        }

        // Adjust tree to fit within the height limit
        while (!isHeightWithinLimit(spanningTree, startVertex, maxHeight)) {
            adjustHeight(spanningTree, startVertex, maxHeight);
        }

        return spanningTree;
    }

    private List<Edge<N, D, W>> getOutgoingEdges(Vertex<N, D> vertex) {
        List<Edge<N, D, W>> outgoingEdges = new ArrayList<>();
        int index = vertices.indexOf(vertex);
        for (int i = 0; i < vertices.size(); i++) {
            Edge<N, D, W> edge = edges.get(index).get(i);
            if (edge != edgePlaceholder) {
                outgoingEdges.add(edge);
            }
        }
        return outgoingEdges;
    }

    private boolean isHeightWithinLimit(List<Edge<N, D, W>> spanningTree, Vertex<N, D> startVertex, int maxHeight) {
        Map<Vertex<N, D>, List<Vertex<N, D>>> tree = buildTreeFromEdges(spanningTree);
        return getHeight(tree, startVertex) <= maxHeight;
    }

    private Map<Vertex<N, D>, List<Vertex<N, D>>> buildTreeFromEdges(List<Edge<N, D, W>> edges) {
        Map<Vertex<N, D>, List<Vertex<N, D>>> tree = new LinkedHashMap<>();
        for (Edge<N, D, W> edge : edges) {
            tree.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(edge.getTo());
        }
        return tree;
    }

    private int getHeight(Map<Vertex<N, D>, List<Vertex<N, D>>> tree, Vertex<N, D> vertex) {
        if (!tree.containsKey(vertex)) {
            return 0;
        }
        int height = 0;
        for (Vertex<N, D> child : tree.get(vertex)) {
            height = Math.max(height, getHeight(tree, child) + 1);
        }
        return height;
    }

    private List<Edge<N, D, W>> adjustHeight(List<Edge<N, D, W>> spanningTree, Vertex<N, D> startVertex, int maxHeight) {
        Map<Vertex<N, D>, List<Vertex<N, D>>> tree = buildTreeFromEdges(spanningTree);
        List<Edge<N, D, W>> additionalEdges = new ArrayList<>();

        Queue<Vertex<N, D>> queue = new LinkedList<>();
        Map<Vertex<N, D>, Integer> depth = new LinkedHashMap<>();
        queue.add(startVertex);
        depth.put(startVertex, 0);

        while (!queue.isEmpty()) {
            Vertex<N, D> current = queue.poll();
            int currentDepth = depth.get(current);
            if (currentDepth < maxHeight) {
                for (Vertex<N, D> neighbor : tree.getOrDefault(current, Collections.emptyList())) {
                    depth.put(neighbor, currentDepth + 1);
                    queue.add(neighbor);
                }
            } else {
                for (Vertex<N, D> neighbor : tree.getOrDefault(current, Collections.emptyList())) {
                    Edge<N, D, W> newEdge = new Edge<>(startVertex, neighbor);
                    var oldEdge = spanningTree.stream().filter(edge -> Objects.equals(edge.getFrom(), current) && Objects.equals(edge.getTo(), neighbor)).findFirst();
                    oldEdge.ifPresent(spanningTree::remove);
                    additionalEdges.add(newEdge);
                }
            }
        }

        spanningTree.addAll(additionalEdges);
        return spanningTree;
    }

    private Edge<N, D, W> findNewEdge(List<Edge<N, D, W>> spanningTree, Vertex<N, D> from, Vertex<N, D> to) {
        return null;
    }

    private static class MatrixGraphIterator<T> implements Iterator<T> {

        private final List<T> list;
        private int index = 0;

        public MatrixGraphIterator(List<T> list) {
            this.list = list;
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
