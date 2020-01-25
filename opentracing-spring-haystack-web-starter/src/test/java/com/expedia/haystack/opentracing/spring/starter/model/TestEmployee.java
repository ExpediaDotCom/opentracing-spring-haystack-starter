package com.expedia.haystack.opentracing.spring.starter.model;

public class TestEmployee {
    private String name;
    private int id;

    public TestEmployee() { this(""); }

    public TestEmployee(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
