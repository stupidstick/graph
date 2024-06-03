package ru.aisd.graph;

public class Vertex<N, D> {

    private N name;
    private D data;

    public Vertex(N name, D data) {
        this.name = name;
        this.data = data;
    }

    public N getName() {
        return name;
    }

    public D getData() {
        return data;
    }

    public void setName(N name) {
        this.name = name;
    }

    public void setData(D data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return
                "name: " + name + "\n"
                        + "data: " + data;
    }
}
