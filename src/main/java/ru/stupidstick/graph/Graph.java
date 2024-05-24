package ru.stupidstick.graph;

import ru.stupidstick.fx.graph.VisGraph;

import java.util.Optional;

public interface Graph<N, D, W extends Comparable<W>> {

    Vertex<N, D> insertVertex(N name, D data);

    boolean removeVertex(Vertex<N, D> vertex);

    Edge<N, D, W> insertEdge(Vertex<N, D> v1, Vertex<N, D> v2);

    boolean deleteEdge(Vertex<N, D> v1, Vertex<N, D> v2);

    Optional<Edge<N, D, W>> getEdge(Vertex<N, D> v1, Vertex<N, D> v2);

    VisGraph toVisGraph();

    Iterator<Vertex<N, D>> vertexIterator();

    Iterator<Edge<N, D, W>> edgeIterator();

    Iterator<Edge<N, D, W>> edgeIterator(Vertex<N, D> vertex);

    boolean isDirected();
}
