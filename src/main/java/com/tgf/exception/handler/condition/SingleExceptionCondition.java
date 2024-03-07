package com.tgf.exception.handler.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 *  Check all code which implements SingleException interface.
 *  If condition fails don't initialize single exception logic and stop spring context initialization.
 */
public class SingleExceptionCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            Thread.currentThread().getContextClassLoader()
                    .loadClass("com.tgf.exception.handler.exception.SingleException");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Please implement com.tgf.exception.handler.exception.SingleException interface in global exception class");
        }
        return true;
    }
}
