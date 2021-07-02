package org.zhuqigong.blogservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.zhuqigong.blogservice.anotations.CacheAnnotationHandler;
import org.zhuqigong.blogservice.anotations.CleanUpCache;

@Component
@Aspect
public class AnnotationsProcessor {
    private final CacheAnnotationHandler cacheAnnotationHandler;

    public AnnotationsProcessor(CacheAnnotationHandler cacheAnnotationHandler) {
        this.cacheAnnotationHandler = cacheAnnotationHandler;
    }

    @Pointcut("execution(* org.zhuqigong.blogservice.service.PostService.*(..))")
    public void methodPointCut() {
        //pointcut method
    }

    @Around("methodPointCut()")
    public Object process(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        if (signature.getMethod().isAnnotationPresent(CleanUpCache.class)) {
            CleanUpCache annotation = signature.getMethod().getAnnotation(CleanUpCache.class);
            for (CleanUpCache.TYPE type : annotation.value()) {
                if (CleanUpCache.TYPE.ELEMENT == type) {
                    cacheAnnotationHandler.handleInvalidCacheElement();
                } else if (CleanUpCache.TYPE.ELEMENTS == type) {
                    cacheAnnotationHandler.handleInvalidCacheElements();
                }
            }
        }
        return pjp.proceed();
    }
}
