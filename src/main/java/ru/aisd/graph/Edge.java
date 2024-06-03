package ru.aisd.graph;

public class Edge<N, D, W extends Comparable<W>> {

    Vertex<N, D> from;

    Vertex<N, D> to;

    W weight;

    D data;

    public Edge(Vertex<N, D> from, Vertex<N, D> to) {
        this.from = from;
        this.to = to;
    }

    public Edge(Vertex<N, D> from, Vertex<N, D> to, W weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public Edge(Vertex<N, D> from, Vertex<N, D> to, W weight, D data) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.data = data;
    }

    public Vertex<N, D> getFrom() {
        return from;
    }

    public void setFrom(Vertex<N, D> from) {
        this.from = from;
    }

    public Vertex<N, D> getTo() {
        return to;
    }

    public void setTo(Vertex<N, D> to) {
        this.to = to;
    }

    public W getWeight() {
        return weight;
    }

    public void setWeight(W weight) {
        this.weight = weight;
    }

    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (data != null) {
            builder.append("data: ").append(data).append("\n");
        }
        if (weight != null) {
            builder.append("weight: ").append(weight);
        }
        return builder.toString();
    }
}
