package com.fintex.ce.framework.httpclient.assertion;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface AssertionProvider {

    AssertionProvider expectedStatusCode(int statusCode);

    AssertionProvider expectedStatusCode(int statusCode, String message);

    String getContentAsString();

    <T> T getBodyAs(Class<T> targetClass, ObjectMapper mapper);

}
