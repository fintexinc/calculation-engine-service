package com.fintex.ce.dto.response.core;

import lombok.Data;

import java.io.Serializable;

@Data
public class Warning implements Serializable {

    private String id;
    private String message;
    private String code;

    public Warning() {
    }

    public Warning(final String id, final String message) {
        this.id = id;
        this.message = message;
    }

    public Warning(final String id, final String message, final String code) {
        this.id = id;
        this.message = message;
        this.code = code;
    }
}
