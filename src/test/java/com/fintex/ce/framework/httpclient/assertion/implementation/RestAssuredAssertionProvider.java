package com.fintex.ce.framework.httpclient.assertion.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintex.ce.framework.exceptions.InternalTestGeneralException;
import com.fintex.ce.framework.httpclient.assertion.AssertionProvider;
import com.fintex.ce.framework.utils.Logger;
import io.restassured.response.Response;

import java.io.IOException;

public class RestAssuredAssertionProvider implements AssertionProvider {
    private final Response response;

    public RestAssuredAssertionProvider(Response response) {
        this.response = response;
//        ((ClientConnectionManager) ((RestAssuredResponseImpl) this.response)
//                .getConnectionManager()).shutdown();
    }

    @Override
    public AssertionProvider expectedStatusCode(int statusCode) {
        response
                .then().assertThat()
                .statusCode(statusCode);
        return this;
    }

    @Override
    public AssertionProvider expectedStatusCode(int statusCode, String message) {
        synchronized (RestAssuredAssertionProvider.class) {
            Logger.info(message);
            response
                    .then().assertThat()
                    .statusCode(statusCode);
        }
        return this;
    }

    @Override
    public String getContentAsString() {
        return response.getBody().asString();
    }

    @Override
    public <T> T getBodyAs(Class<T> targetClass, ObjectMapper mapper) {
        try {
            return mapper.readValue(response.getBody().asString(), targetClass);
        } catch (IOException e) {
            throw new InternalTestGeneralException("During json parsing", e);
        }
    }
}
