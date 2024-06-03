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

    public List<Edge<N, D, W>> findCycleByLength(Vertex<N, D> baseVertex, int length) {
        if (!vertices.contains(baseVertex)) {
            throw new IllegalArgumentException("Vertex not found");
        }
        if (length <= 1) {
            throw new IllegalArgumentException("Distance must be positive");
        }

        Queue<VertexDistance> queue = new LinkedList<>();
        queue.add(new VertexDistance(baseVertex, baseVertex));

        while (!queue.isEmpty()) {
            var vertexDistance = queue.poll();
            if (vertexDistance.vertex == baseVertex && vertexDistance.distance == length) {
                return vertexDistance.visitedEdges.stream().toList();
            }
            queue.addAll(vertexDistance.next());
        }

        return Collections.emptyList();
    }

    private class VertexDistance {
        Vertex<N, D> baseVertex;
        Vertex<N, D> vertex;
        int distance = 0;
        Set<Edge<N, D, W>> visitedEdges = new LinkedHashSet<>();
        Set<Vertex<N, D>> visited = new LinkedHashSet<>();

        public VertexDistance(Vertex<N, D> vertex, Vertex<N, D> baseVertex) {
            this.vertex = vertex;
            this.baseVertex = baseVertex;
        }

        public List<VertexDistance> next() {
            List<VertexDistance> result = new ArrayList<>();
            edges.get(vertices.indexOf(vertex)).forEach(edge -> {
                if (edge != edgePlaceholder) {
                    Vertex<N, D> nextVertex = edge.to;
                    if (!visited.contains(nextVertex) || (nextVertex == baseVertex && !visitedEdges.contains(edge))) {
                        visited.add(vertex);
                        var next = new VertexDistance(nextVertex, baseVertex);
                        next.distance = distance + 1;
                        next.visited.addAll(visited);
                        next.visitedEdges.addAll(visitedEdges);
                        next.visitedEdges.add(edge);
                        result.add(next);
                    }
                }
            });

            return result;
        }
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
