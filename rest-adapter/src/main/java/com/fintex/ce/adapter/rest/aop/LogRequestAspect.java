package com.fintex.ce.adapter.rest.aop;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Aspect
@Component
public class LogRequestAspect {

  private static final Logger log = LoggerFactory.getLogger(LogRequestAspect.class);

  @Value("${performance-monitor-duration.info-threshold}")
  private int infoThreshold;

  @Pointcut("within(@com.fintex.ce.adapter.rest.aop.annotation.LogRequest *)")
  public void methodsWithinBeanAnnotatedWithLogRequest() {
  }

  @Around("methodsWithinBeanAnnotatedWithLogRequest()")
  public Object logRequest(final ProceedingJoinPoint joinPoint) throws Throwable {
    var start = System.currentTimeMillis();
    var req = getRequestArgumentByType(joinPoint.getArgs(), HttpServletRequest.class);
    if (req == null) {
      req = ((org.springframework.web.context.request.ServletRequestAttributes)
          org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    var request = req;
    try {
      return joinPoint.proceed();
    } finally {
      var finish = System.currentTimeMillis();
      var executionTime = finish - start;
      // TODO rework logging at TMI-350
      if (executionTime < infoThreshold) {
        log.info("request url: {}, payload: {}, execution time: {}ms", request.getRequestURI(),
            getPayload(request), executionTime);
      } else {
        log.warn("request url: {}, payload: {}, execution time: {}ms", request.getRequestURI(),
            getPayload(request), executionTime);
      }
    }
  }

  protected String getPayload(final HttpServletRequest request) {
    if (request instanceof ContentCachingRequestWrapper) {
      return new String(((ContentCachingRequestWrapper) request).getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    final ContentCachingRequestWrapper cachingRequestWrapper = new ContentCachingRequestWrapper(request);
    cachingRequestWrapper.getParameterMap();
    return getPayload(cachingRequestWrapper);
  }

  protected <T> T getRequestArgumentByType(final Object[] args, final Class<T> clazz) {
    for (final Object arg : args) {
      if (clazz.isInstance(arg)) {
        return clazz.cast(arg);
      }
    }
    return null;
  }

}
