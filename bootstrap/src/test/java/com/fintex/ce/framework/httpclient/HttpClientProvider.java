package com.fintex.ce.framework.httpclient;

import com.fintex.ce.framework.model.Pair;
import com.fintex.ce.framework.httpclient.assertion.AssertionProvider;

import java.io.InputStream;

public interface HttpClientProvider {
  HttpClientProvider useSwaggerHubSchema();

  HttpClientProvider header(String name, Object value);

  HttpClientProvider param(String name, Object value);

  HttpClientProvider param(Pair<String, ?> pair);

  HttpClientProvider param(Pair<String, ?>[] pair);

  AssertionProvider post(String json, String url);

  AssertionProvider post(Object json, String url);

  AssertionProvider post(InputStream json, String url);

  AssertionProvider get(String url);

  AssertionProvider put(String url);

  AssertionProvider put(String json, String url);
}
