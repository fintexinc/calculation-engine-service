package com.fintex.ce.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

import static com.google.common.net.HttpHeaders.HOST;
import static com.google.common.net.HttpHeaders.X_FORWARDED_FOR;

@Aspect
@Component
public class LogRequestAspect {

    private static final Logger log = LoggerFactory.getLogger(LogRequestAspect.class);

    @Value("${performance-monitor-duration.info-threshold}")
    private int infoThreshold;

    @Pointcut("within(@com.fintex.ce.aop.annotation.LogRequest *)")
    public void methodsWithinBeanAnnotatedWithLogRequest() {
    }

    @Around("methodsWithinBeanAnnotatedWithLogRequest()")
    public Object logRequest(final ProceedingJoinPoint joinPoint) throws Throwable {
        var start = System.currentTimeMillis();
        var req = getRequestArgumentByType(joinPoint.getArgs(), HttpServletRequest.class);

        try {
            return joinPoint.proceed();
        } finally {
            var finish = System.currentTimeMillis();
            var executionTime = finish - start;
            if (executionTime < infoThreshold) {
                log.info("request url: {}, headers: [{}], payload: {}, execution time: {}ms", req.getRequestURI(), getHeaders(req), getPayload(req), executionTime);
            } else {
                log.warn("request url: {}, headers: [{}], payload: {}, execution time: {}ms", req.getRequestURI(), getHeaders(req), getPayload(req), executionTime);
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

    private String getHeaders(final HttpServletRequest request) {
        final String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        final String host = request.getHeader(HOST);

        return new StringBuilder()
                .append(X_FORWARDED_FOR).append(" = ").append(xForwardedFor)
                .append(", ")
                .append(HOST).append(" = ").append(host).toString();
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
