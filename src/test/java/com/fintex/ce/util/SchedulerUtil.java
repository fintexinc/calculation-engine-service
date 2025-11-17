package com.fintex.ce.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SchedulerUtil {

    public static CronExpression getGenerator(final String cronExpresion, final TimeZone timezone) {
        return CronExpression.parse(cronExpresion);
    }

    public static CronExpression getGenerator(final Class<?> clazz, final String methodName) {
        final Scheduled scheduledAnnotation = getScheduledAnnotation(clazz, methodName);
        return getGenerator(scheduledAnnotation.cron(), TimeZone.getTimeZone(scheduledAnnotation.zone()));
    }

    public static Scheduled getScheduledAnnotation(final Class<?> clazz, final String methodName) {
        final Method method = ReflectionUtils.findMethod(clazz, methodName);
        assertNotNull(method, "Can't find method with name: "+methodName+" in class: "+clazz.getName());

        final Scheduled scheduledAnnotation = method.getAnnotation(Scheduled.class);
        assertNotNull(scheduledAnnotation, "Method: "+method.getName()+" is not annotated @Scheduled");

        return scheduledAnnotation;
    }

}
