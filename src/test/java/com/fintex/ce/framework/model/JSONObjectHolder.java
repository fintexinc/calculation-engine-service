package com.fintex.ce.framework.model;

public class JSONObjectHolder {

    private String title;
    private Object data;

    public String getTitle() {
        return title;
    }

    public JSONObjectHolder setTitle(String title) {
        this.title = title;
        return this;
    }

    public Object getData() {
        return data;
    }

    public JSONObjectHolder setData(Object data) {
        this.data = data;
        return this;
    }
}
