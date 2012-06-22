package com.coleman.ojm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyName
{
    String abbr();
    
    boolean need() default true;
    
    Class<?> genericType() default Object.class;
    
    /**
     * mark the field description of the interface define document
     * 
     * @return true mark the field define may has problem, false no problem
     */
    boolean question() default false;
}
