package com.librarymanagement.librarymanagement.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging execution of service and repository Spring components.
 */
@Aspect
@Component
public class LoggingAspect {

    /**
     * Pointcut that matches all controllers.
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Pointcut that matches all service implementation classes.
     */
    @Pointcut("within(com.librarymanagement.librarymanagement.service..*)")
    public void servicePointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Pointcut that matches all repository implementation classes.
     */
    @Pointcut("within(com.librarymanagement.librarymanagement.repository..*)")
    public void repositoryPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Advice that logs methods throwing exceptions.
     *
     * @param joinPoint join point for advice
     * @param e exception
     */
    @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
        
        logger.error(
            "Exception in {}.{}() with cause = '{}' and exception = '{}'",
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName(),
            e.getCause() != null ? e.getCause() : "NULL",
            e.getMessage()
        );
    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint join point for advice
     * @return result
     * @throws Throwable throws Exception
     */
    @Around("controllerPointcut() || servicePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
        
        if (logger.isDebugEnabled()) {
            logger.debug("Enter: {}.{}() with arguments = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs())
            );
        }
        
        try {
            long start = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long end = System.currentTimeMillis();
            
            if (logger.isDebugEnabled()) {
                logger.debug("Exit: {}.{}() with result = {} in {} ms",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    result,
                    end - start
                );
            }
            
            return result;
        } catch (IllegalArgumentException e) {
            logger.error("Illegal argument: {} in {}.{}()",
                Arrays.toString(joinPoint.getArgs()),
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName()
            );
            throw e;
        }
    }
} 