package com.fintex.ce.framework.security;

import com.fintex.ce.framework.model.Pair;

public interface SessionHeaderProvider {

  Pair<String, Object> getSessionToken();

}
