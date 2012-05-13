
package com.coleman.http.json.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequestObject {
    String url() default "http://10.0.2.2:8080";

    String path() default "";

    String hwUrl() default "http://zb00056.u02.netjsp.com";
}
