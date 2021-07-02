package org.zhuqigong.blogservice.anotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CleanUpCache {
    TYPE[] value() default {TYPE.ELEMENTS, TYPE.ELEMENT};

    enum TYPE {
        ELEMENTS, ELEMENT
    }
}
